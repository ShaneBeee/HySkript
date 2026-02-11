package com.github.skriptdev.skript.plugin.elements.expressions.player;

import com.github.skriptdev.skript.api.hytale.utils.EntityUtils;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.permissions.provider.PermissionProvider;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class ExprPlayerPermissionGroup implements Expression<String> {

    public static void register(SkriptRegistration reg) {
        reg.newExpression(ExprPlayerPermissionGroup.class, String.class, false,
                "permission group[s] of %players/playerrefs/uuids%")
            .name("Player Permission Groups")
            .description("Returns the permission groups of a player.")
            .examples("set {_groups::*} to permission groups of player",
                "add \"some.group\" to permission groups of player",
                "remove \"some.group\" from permission groups of player")
            .since("1.0.0")
            .register();
    }

    private Expression<?> permissables;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.permissables = expressions[0];
        return true;
    }

    @Override
    public String[] getValues(@NotNull TriggerContext ctx) {
        List<String> groups = new ArrayList<>();

        PermissionProvider provider = PermissionsModule.get().getFirstPermissionProvider();
        for (Object o : this.permissables.getArray(ctx)) {
            if (o instanceof UUID u) {
                groups.addAll(provider.getGroupsForUser(u));
            } else if (o instanceof Player player) {
                UUID uuid = EntityUtils.getUUID(player);
                if (uuid == null) continue;
                groups.addAll(provider.getGroupsForUser(uuid));
            } else if (o instanceof PlayerRef ref) {
                groups.addAll(provider.getGroupsForUser(ref.getUuid()));
            }
        }

        return groups.toArray(new String[0]);
    }

    @Override
    public Optional<Class<?>[]> acceptsChange(@NotNull ChangeMode mode) {
        if (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE) return Optional.of(new Class<?>[]{String[].class});
        return Optional.empty();
    }

    @SuppressWarnings({"ConstantValue"})
    @Override
    public void change(@NotNull TriggerContext ctx, @NotNull ChangeMode changeMode, Object @NotNull [] changeWith) {
        if (changeWith == null) return;

        Set<String> permissions = Set.of((String[]) changeWith[0]);

        PermissionProvider provider = PermissionsModule.get().getFirstPermissionProvider();
        for (Object permissable : permissables.getValues(ctx)) {
            if (permissable instanceof UUID uuid) {
                permChange(changeMode, provider, uuid, permissions);
            } else if (permissable instanceof PlayerRef ref) {
                permChange(changeMode, provider, ref.getUuid(), permissions);
            } else if (permissable instanceof Player player) {
                UUID uuid = EntityUtils.getUUID(player);
                if (uuid == null) continue;
                permChange(changeMode, provider, uuid, permissions);
            }
        }
    }

    private void permChange(ChangeMode mode, PermissionProvider provider, UUID uuid, Set<String> permissions) {
        if (mode == ChangeMode.ADD) {
            permissions.forEach(p -> provider.addUserToGroup(uuid, p));
        } else if (mode == ChangeMode.REMOVE) {
            permissions.forEach(p -> provider.removeUserFromGroup(uuid, p));
        }
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "permission groups of " + this.permissables.toString(ctx, debug);
    }

}
