package com.github.skriptdev.skript.plugin.elements.expressions.block;

import com.github.skriptdev.skript.api.hytale.Block;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.api.utils.Utils;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import io.github.syst3ms.skriptparser.config.Config;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ExprTargetBlockOfPlayer implements Expression<Block> {

    private static int MAX_DISTANCE = 160; // Default from config

    public static void register(SkriptRegistration reg) {
        Config skriptConfig = reg.getSkript().getSkriptConfig();
        int anInt = skriptConfig.getInt("max-target-block-distance");
        if (anInt > 0) {
            MAX_DISTANCE = anInt;
        } else {
            Utils.debug("'max-target-block-distance' from config.sk is not set or invalid, using default value: " + MAX_DISTANCE);
        }

        reg.newExpression(ExprTargetBlockOfPlayer.class, Block.class, true,
                "target block of %player%")
            .name("Target Block of Player")
            .description("Returns the block the player is looking at.",
                "The default max distance is 160 blocks.",
                "You can change this in config.sk under 'max-target-block-distance'.")
            .examples("set {_block} to target block of player")
            .since("1.0.0")
            .register();
    }

    private Expression<Player> player;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.player = (Expression<Player>) expressions[0];
        return true;
    }

    @Override
    public Block[] getValues(@NotNull TriggerContext ctx) {
        Optional<? extends Player> single = this.player.getSingle(ctx);
        if (single.isEmpty()) return null;

        Player player = single.get();
        Ref<EntityStore> ref = player.getReference();
        World world = player.getWorld();
        if (world == null || ref == null) return null;

        Store<EntityStore> store = world.getEntityStore().getStore();

        Vector3i targetBlock = TargetUtil.getTargetBlock(ref, MAX_DISTANCE, store);
        if (targetBlock == null) return null;
        return new Block[]{new Block(world, targetBlock)};
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "target block of " + this.player.toString(ctx, debug);
    }

}
