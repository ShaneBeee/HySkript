package com.github.skriptdev.skript.api.hytale;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a block in a world.
 * Hytale doesn't appear to have a representation of a block in a world.
 * This class provides a wrapper around Hytale's block system, allowing for easy interaction with blocks in a world.
 * This may be changed/removed in the future.
 */
@SuppressWarnings("unused")
public class Block {

    private final @NotNull World world;
    private @NotNull BlockType type;
    private final @NotNull Vector3i pos;

    public Block(@NotNull World world, @NotNull Vector3i pos) {
        this.world = world;
        this.pos = pos;
        this.type = Objects.requireNonNull(world.getBlockType(pos));
    }

    public Block(@NotNull World world, @NotNull Vector3i pos, @NotNull BlockType type) {
        this.world = world;
        this.pos = pos;
        this.type = type;
    }

    public Block(@NotNull Location location) {
        World world = Universe.get().getWorld(location.getWorld());
        if (world == null) {
            throw new IllegalArgumentException("World '" + location.getWorld() + "' not found.");
        }
        BlockType blockType = world.getBlockType(location.getPosition().toVector3i());
        assert blockType != null;
        this(world, location.getPosition().toVector3i(), blockType);
    }

    public @NotNull BlockType getType() {
        return this.type;
    }

    public void setType(@NotNull BlockType type) {
        this.type = type;
        this.world.setBlock(this.pos.getX(), this.pos.getY(), this.pos.getZ(), type.getId());
    }

    public byte getFluidLevel() {
        long index = ChunkUtil.indexChunkFromBlock(this.pos.getX(), this.pos.getZ());
        Ref<ChunkStore> columnRef = this.world.getChunk(index).getReference();
        Store<ChunkStore> store = columnRef.getStore();
        ChunkColumn column = store.getComponent(columnRef, ChunkColumn.getComponentType());
        Ref<ChunkStore> section = column.getSection(ChunkUtil.chunkCoordinate(this.pos.getY()));
        if (section == null) {
            return 0;
        } else {
            FluidSection fluidSection = store.getComponent(section, FluidSection.getComponentType());
            return fluidSection.getFluidLevel(this.pos.getX(), this.pos.getY(), this.pos.getZ());
        }
    }

    public void setFluidLevel(byte level) {
        long index = ChunkUtil.indexChunkFromBlock(this.pos.getX(), this.pos.getZ());
        this.world.getChunkAsync(index).thenApply((chunk) -> {
            Ref<ChunkStore> columnRef = chunk.getReference();
            Store<ChunkStore> store = columnRef.getStore();
            ChunkColumn column = store.getComponent(columnRef, ChunkColumn.getComponentType());
            if (column == null) return null;

            Ref<ChunkStore> section = column.getSection(ChunkUtil.chunkCoordinate(this.pos.getY()));
            if (section == null) {
                return null;
            } else {
                FluidSection fluidSection = store.getComponent(section, FluidSection.getComponentType());
                if (fluidSection == null) {
                    return null;
                }


                Fluid fluid = fluidSection.getFluid(this.pos.getX(), this.pos.getY(), this.pos.getZ());
                if (fluid == null) return null;
                fluidSection.setFluid(this.pos.getX(), this.pos.getY(), this.pos.getZ(), fluid, level);
            }
            return chunk;
        });
    }

    public Fluid getFluid() {
        int fluidId = this.world.getFluidId(this.pos.getX(), this.pos.getY(), this.pos.getZ());
        if (fluidId == -1) {
            return null;
        }
        return Fluid.getAssetMap().getAsset(fluidId);
    }

    public void setFluid(@NotNull Fluid fluid) {
        long index = ChunkUtil.indexChunkFromBlock(this.pos.getX(), this.pos.getZ());
        this.world.getChunkAsync(index).thenApply((chunk) -> {
            Ref<ChunkStore> columnRef = chunk.getReference();
            Store<ChunkStore> store = columnRef.getStore();
            ChunkColumn column = store.getComponent(columnRef, ChunkColumn.getComponentType());
            if (column == null) return null;

            Ref<ChunkStore> section = column.getSection(ChunkUtil.chunkCoordinate(this.pos.getY()));
            if (section == null) {
                return null;
            } else {
                FluidSection fluidSection = store.getComponent(section, FluidSection.getComponentType());
                if (fluidSection == null) {
                    return null;
                }


                byte level = fluidSection.getFluidLevel(this.pos.getX(), this.pos.getY(), this.pos.getZ());
                if (level <= 0) level = 8;
                fluidSection.setFluid(this.pos.getX(), this.pos.getY(), this.pos.getZ(), fluid, level);
            }
            return chunk;
        });
    }

    public void breakBlock() {
        int setting = 0; // TODO not sure what to actually use here
        this.world.breakBlock(this.pos.getX(), this.pos.getY(), this.pos.getZ(), setting);
    }

    public @NotNull World getWorld() {
        return this.world;
    }

    public @NotNull Vector3i getPos() {
        return this.pos;
    }

    public @NotNull Location getLocation() {
        return new Location(this.world.getName(), this.pos.getX(), this.pos.getY(), this.pos.getZ());
    }

    public String toTypeString() {
        return String.format("[%s] block at (%s,%s,%s) in '%s'",
            this.type.getId(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.world.getName());
    }

    @Override
    public String toString() {
        return "Block{" +
            "world=" + world.getName() +
            ", type=" + type +
            ", pos=" + pos +
            '}';
    }

}
