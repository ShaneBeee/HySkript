package com.github.skriptdev.skript.plugin.elements.conditions;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.ConditionalExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CondHasPermission extends ConditionalExpression {

    public static void register(SkriptRegistration registration) {
        registration.newExpression(CondHasPermission.class, Boolean.class, true,
                "%players/playerrefs/uuid% (has|have) permission %strings%",
                "%players/playerrefs/uuid% (don't|do not|doesn't|does not) (has|have) permission %strings%")
            .name("Permission")
            .description("Checks if the specified players/playerrefs/uuids have/don't have the specified permissions.")
            .examples("if player has permission \"hytale.admin\":")
            .since("1.0.0")
            .register();
    }

    private Expression<?> players;
    private Expression<String> permission;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.players = expressions[0];
        this.permission = (Expression<String>) expressions[1];
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(@NotNull TriggerContext ctx) {
        PermissionsModule permissionsModule = PermissionsModule.get();
        this.permission.check(ctx, string -> {
            this.players.check(ctx, p -> {
                if (p instanceof Player player) {
                    return player.hasPermission(string);
                } else if (p instanceof PlayerRef playerRef) {
                    return permissionsModule.hasPermission(playerRef.getUuid(), string);
                } else if (p instanceof UUID uuid) {
                    return permissionsModule.hasPermission(uuid, string);
                }
                return false;
            }, isNegated());
            return false;
        }, isNegated());
        return false;
    }


    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return this.players.toString(ctx, debug) + " has permission " + this.permission.toString(ctx, debug);
    }

}
