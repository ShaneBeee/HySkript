package com.github.skriptdev.skript.plugin.elements.expressions.block;

import com.github.skriptdev.skript.api.hytale.Block;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ExprTargetBlockOfPlayer implements Expression<Block> {

    public static void register(SkriptRegistration reg) {
        reg.newExpression(ExprTargetBlockOfPlayer.class, Block.class, true,
                "target block of %player%")
            .name("Target Block of Player")
            .description("Returns the block the player is looking at.")
            .examples("set {_block} to target block of player")
            .since("INSERT VERSION")
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

        // TODO configurable maxDistance
        Vector3i targetBlock = TargetUtil.getTargetBlock(ref, 50, store);
        if (targetBlock == null) return null;
        return new Block[]{new Block(world, targetBlock)};
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "target block of " + this.player.toString(ctx, debug);
    }

}
