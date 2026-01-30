package com.github.skriptdev.skript.plugin.elements.expressions.block;

import com.github.skriptdev.skript.api.hytale.Block;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ExprBlockAt implements Expression<Block> {

    public static void register(SkriptRegistration reg) {
        reg.newExpression(ExprBlockAt.class, Block.class, true,
            "block[s] at %locations%")
            .name("Block At")
            .description("Returns the block at a location.")
            .examples("set {_block} to block at player's location")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<Location> locations;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.locations = (Expression<Location>) expressions[0];
        return true;
    }

    @Override
    public Block[] getValues(@NotNull TriggerContext ctx) {
        List<Block> blocks = new ArrayList<>();

        for (Location location : this.locations.getArray(ctx)) {
            String worldName = location.getWorld();
            if (worldName == null) continue;
            World world = Universe.get().getWorld(worldName);
            if (world == null) continue;

            Vector3i pos = location.getPosition().toVector3i();
            BlockType blockType = world.getBlockType(pos);
            if (blockType == null) continue;

            Block block = new Block(world, pos, blockType);
            blocks.add(block);
        }

        return blocks.toArray(new Block[0]);
    }

    @Override
    public boolean isSingle() {
        return this.locations.isSingle();
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String plural = this.locations.isSingle() ? "" : "s";
        return String.format("block%s at %s", plural, this.locations.toString(ctx, debug));
    }

}
