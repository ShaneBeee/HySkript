package com.github.skriptdev.skript.api.hytale;

import com.github.skriptdev.skript.api.hytale.utils.StoreUtils;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.blockhealth.BlockHealth;
import com.hypixel.hytale.server.core.modules.blockhealth.BlockHealthChunk;
import com.hypixel.hytale.server.core.modules.blockhealth.BlockHealthModule;
import com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Represents a block in a world.
 * Hytale doesn't appear to have a representation of a block in the world.
 * This class provides a wrapper around Hytale's block system, allowing for easy interaction with blocks in a world.
 * This may be changed/removed in the future.
 */
@SuppressWarnings({"unused", "deprecation"})
public class Block {

    private final @NotNull World world;
    private final @NotNull Vector3i pos;

    public Block(@NotNull World world, @NotNull Vector3i pos) {
        this.world = world;
        this.pos = pos;
    }

    public Block(@NotNull Location location) {
        World world = Universe.get().getWorld(location.getWorld());
        if (world == null) {
            throw new IllegalArgumentException("World '" + location.getWorld() + "' not found.");
        }
        this(world, location.getPosition().toVector3i());
    }

    public WorldChunk getChunk() {
        return this.world.getChunk(ChunkUtil.indexChunkFromBlock(this.pos.getX(), this.pos.getZ()));
    }

    public @NotNull BlockType getType() {
        BlockType blockType = this.world.getBlockType(this.pos);
        return blockType != null ? blockType : BlockType.EMPTY;
    }

    public void setType(@NotNull BlockType type, int settings) {
        Runnable r = () -> Block.this.world.setBlock(Block.this.pos.getX(), Block.this.pos.getY(), Block.this.pos.getZ(), type.getId(), settings);
        if (this.world.isInThread()) {
            r.run();
        } else {
            this.world.execute(r);
        }
    }

    /**
     * Set the rotation of this block.
     *
     * @param rotation Rotation of block represented by a Vector3i(yaw, pitch, roll).
     */
    public void setRotation(Vector3i rotation) {
        int blockId = BlockType.getAssetMap().getIndex(getType().getId());
        WorldChunk chunk = getChunk();
        BlockChunk blockChunk = chunk.getBlockChunk();
        if (blockChunk == null) return;

        Rotation pitch = getRotationFromInt(rotation.getX());
        Rotation yaw = getRotationFromInt(rotation.getY());
        Rotation roll = getRotationFromInt(rotation.getZ());
        int rotationIndex = RotationTuple.of(yaw, pitch, roll).index();
        blockChunk.setBlock(this.pos.getX(), this.pos.getY(), this.pos.getZ(),
            blockId, rotationIndex, 0);
    }

    /**
     * Get the rotation of this block.
     *
     * @return Rotation of block represented as a Vector3i(yaw, pitch, roll).
     */
    public Vector3i getRotation() {
        int blockRotationIndex = getWorld().getBlockRotationIndex(this.pos.getX(), this.pos.getY(), this.pos.getZ());
        RotationTuple rotationTuple = RotationTuple.get(blockRotationIndex);

        int yaw = getIntFromRotation(rotationTuple.yaw());
        int pitch = getIntFromRotation(rotationTuple.pitch());
        int roll = getIntFromRotation(rotationTuple.roll());
        return new Vector3i(pitch, yaw, roll);
    }

    private Rotation getRotationFromInt(int v) {
        if (v < 90) return Rotation.None;
        else if (v < 180) return Rotation.Ninety;
        else if (v < 270) return Rotation.OneEighty;
        else return Rotation.TwoSeventy;
    }

    private int getIntFromRotation(Rotation rotation) {
        return switch (rotation) {
            case None -> 0;
            case Ninety -> 90;
            case OneEighty -> 180;
            case TwoSeventy -> 270;
        };
    }

    public byte getFluidLevel() {
        long index = ChunkUtil.indexChunkFromBlock(this.pos.getX(), this.pos.getZ());
        WorldChunk chunk = this.world.getChunk(index);
        if (chunk == null) return 0;

        Ref<ChunkStore> columnRef = chunk.getReference();
        Store<ChunkStore> store = columnRef.getStore();
        ChunkColumn column = store.getComponent(columnRef, ChunkColumn.getComponentType());
        if (column == null) return 0;

        Ref<ChunkStore> section = column.getSection(ChunkUtil.chunkCoordinate(this.pos.getY()));
        if (section == null) {
            return 0;
        } else {
            FluidSection fluidSection = store.getComponent(section, FluidSection.getComponentType());
            if (fluidSection == null) return 0;

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
                byte fluidLevel = (byte) Math.clamp((int) level, 0, fluid.getMaxFluidLevel());
                fluidSection.setFluid(this.pos.getX(), this.pos.getY(), this.pos.getZ(), fluid, fluidLevel);
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

    public void setFluid(@NotNull Fluid fluid, @Nullable Integer level) {
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


                byte fluidLevel;
                if (level != null) {
                    fluidLevel = level.byteValue();
                } else {
                    fluidLevel = fluidSection.getFluidLevel(this.pos.getX(), this.pos.getY(), this.pos.getZ());
                    if (fluidLevel <= 0) fluidLevel = (byte) fluid.getMaxFluidLevel();
                }
                fluidLevel = (byte) Math.clamp((int) fluidLevel, 0, fluid.getMaxFluidLevel());
                fluidSection.setFluid(this.pos.getX(), this.pos.getY(), this.pos.getZ(), fluid, fluidLevel);
            }
            return chunk;
        });
    }

    public void breakBlock(int settings) {
        this.world.breakBlock(this.pos.getX(), this.pos.getY(), this.pos.getZ(), settings);
    }

    public void damage(@Nullable LivingEntity performer, @Nullable ItemStack itemStack, float damage) {
        WorldChunk chunk = this.world.getChunk(ChunkUtil.indexChunkFromBlock(this.pos.getX(), this.pos.getZ()));
        if (chunk == null) return;

        Ref<ChunkStore> ref = chunk.getReference();
        Store<ChunkStore> chunkStore = this.world.getChunkStore().getStore();
        CommandBuffer<EntityStore> commandBuffer = StoreUtils.getCommandBuffer(this.world.getEntityStore().getStore());

        if (performer == null) {
            BlockHarvestUtils.performBlockDamage(
                this.pos,
                null,
                null,
                damage,
                0,
                ref,
                commandBuffer,
                chunkStore);
        } else {
            BlockHarvestUtils.performBlockDamage(
                performer,
                performer.getReference(),
                this.pos,
                itemStack,
                null,
                null, // TODO figure out how to get this
                false,
                damage,
                0,
                ref,
                commandBuffer,
                chunkStore);
        }
    }

    public float getBlockHealth() {
        WorldChunk chunk = this.world.getChunk(ChunkUtil.indexChunkFromBlock(this.pos.getX(), this.pos.getZ()));
        if (chunk == null) return 0;

        Ref<ChunkStore> ref = chunk.getReference();
        Store<ChunkStore> chunkStore = this.world.getChunkStore().getStore();

        BlockHealthChunk component = chunkStore.getComponent(ref, BlockHealthModule.get().getBlockHealthChunkComponentType());
        if (component == null) return 0;

        return component.getBlockHealth(this.pos);
    }

    public void setBlockHealth(float health) {
        WorldChunk chunk = this.world.getChunk(ChunkUtil.indexChunkFromBlock(this.pos.getX(), this.pos.getZ()));
        if (chunk == null) return;

        Ref<ChunkStore> ref = chunk.getReference();
        Store<ChunkStore> chunkStore = this.world.getChunkStore().getStore();
        BlockHealthChunk component = chunkStore.getComponent(ref, BlockHealthModule.get().getBlockHealthChunkComponentType());
        if (component == null) return;

        Map<Vector3i, BlockHealth> blockHealthMap = component.getBlockHealthMap();
        BlockHealth blockHealth = blockHealthMap.getOrDefault(this.pos, new BlockHealth());
        blockHealth.setHealth(health);
        blockHealthMap.put(this.pos, blockHealth);

        if (!blockHealth.isDestroyed()) {
            Predicate<PlayerRef> filter = (player) -> true;
            world.getNotificationHandler().updateBlockDamage(this.pos.getX(), this.pos.getY(),
                this.pos.getZ(), blockHealth.getHealth(), health, filter);
        }
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
            this.getType().getId(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.world.getName());
    }

    public String toVariableNameString() {
        return String.format("%s_%s_%s_%s_%s", this.world.getName(), this.getType().getId(), this.pos.getX(), this.pos.getY(), this.pos.getZ());
    }

    @Override
    public String toString() {
        return "Block{" +
            "world=" + this.world.getName() +
            ", type=" + this.getType() +
            ", pos=" + this.pos +
            '}';
    }

}
