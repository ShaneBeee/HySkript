package com.github.skriptdev.skript.plugin.elements.conditions;

import com.github.skriptdev.skript.api.hytale.utils.EntityUtils;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.ConditionalExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

public class CondPlayerIsCrouching extends ConditionalExpression {

    public static void register(SkriptRegistration reg) {
        reg.newExpression(CondPlayerIsCrouching.class, Boolean.class, true,
                "%players% (is|are) crouching",
                "%players% (isn't|is not|aren't|are not) crouching")
            .name("Player is Crouching")
            .description("Checks if the player is crouching.")
            .examples("if player is crouching:",
                "\tmessage \"You are crouching!\"")
            .since("1.0.0")
            .register();
    }

    private Expression<Player> players;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        setNegated(matchedPattern == 1);
        this.players = (Expression<Player>) expressions[0];
        return true;
    }

    @Override
    public boolean check(@NotNull TriggerContext ctx) {
        return this.players.check(ctx, player -> {
            MovementStatesComponent component = EntityUtils.getMovementStatesComponent(player);
            if (component == null) return false;

            return component.getMovementStates().crouching;
        }, isNegated());
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        boolean single = this.players.isSingle();
        String s = isNegated() ? (single ? "isn't" : "aren't") : (single ? "is" : "are");
        return this.players.toString(ctx, debug) + " " + s + " crouching";
    }

}
