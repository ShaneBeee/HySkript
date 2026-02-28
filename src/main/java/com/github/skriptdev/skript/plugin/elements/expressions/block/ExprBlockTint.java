package com.github.skriptdev.skript.plugin.elements.expressions.block;

import com.github.skriptdev.skript.api.hytale.Block;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import io.github.syst3ms.skriptparser.util.CollectionUtils;
import io.github.syst3ms.skriptparser.util.color.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ExprBlockTint extends PropertyExpression<Block, Color> {

    public static void register(SkriptRegistration reg) {
        reg.newPropertyExpression(ExprBlockTint.class, Color.class, "block tint", "blocks")
            .name("Block Tint")
            .description("Get/set the tint of a block.",
                "Do note this is for the entire block column, not actually per block.")
            .examples("set {_color} to block tint of target block of player",
                "set block tint of block at player's location to red",
                "set block tint of blocks in radius 3 around player to color from hex \"#3caba9\"")
            .since("1.2.0")
            .register();
    }

    @Override
    public @Nullable Color getProperty(@NotNull Block block) {
        return block.getTint();
    }

    @Override
    public Optional<Class<?>[]> acceptsChange(@NotNull ChangeMode mode) {
        if (mode == ChangeMode.SET) return CollectionUtils.optionalArrayOf(Color.class);
        return Optional.empty();
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public void change(@NotNull TriggerContext ctx, @NotNull ChangeMode changeMode, Object @NotNull [] changeWith) {
        if (changeWith == null || changeWith.length == 0 || !(changeWith[0] instanceof Color color)) return;

        Map<World, List<Long>> chunkIndexes = new HashMap<>();
        for (Block block : getOwner().getArray(ctx)) {
            block.setTint(color, false);
            List<Long> longs = chunkIndexes.computeIfAbsent(block.getWorld(), _ -> new ArrayList<>());
            if (!longs.contains(block.getChunkIndex())) {
                longs.add(block.getChunkIndex());
            }
        }
        // Update all chunks at once rather than for each block
        chunkIndexes.forEach((world, indexes) -> {
            for (Long index : indexes) {
                world.getNotificationHandler().updateChunk(index);
            }
        });
    }

}
