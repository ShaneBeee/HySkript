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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class ExprPlayerPermissions implements Expression<String> {

    public static void register(SkriptRegistration reg) {
        reg.newExpression(ExprPlayerPermissions.class, String.class, false,
                "[all] permissions of %players/playerrefs/uuids%",
                "[all] permissions of group[s] %strings%")
            .name("Player Permissions")
            .description("Get/add/remove all permissions of a Player, PlayerRef, UUID or String (a group).")
            .examples("loop all permissions of player:",
                "remove \"some.perm\" from permissions of player",
                "add \"some.perm\" to permissions of player",
                "add \"some.perm\" to permssions of group \"some.group\"")
            .since("1.0.0")
            .register();
    }

    private boolean group;
    private Expression<?> permissables;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.group = matchedPattern == 1;
        this.permissables = expressions[0];
        return true;
    }

    @Override
    public String[] getValues(@NotNull TriggerContext ctx) {
        List<String> permissions = new ArrayList<>();

        PermissionProvider provider = PermissionsModule.get().getFirstPermissionProvider();
        for (Object o : this.permissables.getArray(ctx)) {
            if (o instanceof Player player) {
                UUID uuid = EntityUtils.getUUID(player);
                if (uuid == null) continue;
                permissions.addAll(provider.getUserPermissions(uuid));
            } else if (o instanceof PlayerRef playerRef) {
                permissions.addAll(provider.getUserPermissions(playerRef.getUuid()));
            } else if (o instanceof UUID uuid) {
                permissions.addAll(provider.getUserPermissions(uuid));
            } else if (o instanceof String s) {
                permissions.addAll(provider.getGroupPermissions(s));
            }
        }

        return permissions.toArray(String[]::new);
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

        HashSet<String> permissions = new HashSet<>();
        for (Object o : changeWith) {
            if (o instanceof String s) permissions.add(s);
        }

        PermissionProvider provider = PermissionsModule.get().getFirstPermissionProvider();
        for (Object permissible : this.permissables.getArray(ctx)) {
            if (permissible instanceof Player player) {
                UUID uuid = EntityUtils.getUUID(player);
                if (uuid == null) continue;
                permChange(changeMode, provider, uuid, permissions);
            } else if (permissible instanceof PlayerRef playerRef) {
                permChange(changeMode, provider, playerRef.getUuid(), permissions);
            } else if (permissible instanceof UUID uuid) {
                permChange(changeMode, provider, uuid, permissions);
            } else if (permissible instanceof String s) {
                permChange(changeMode, provider, s, permissions);
            }
        }
    }

    private void permChange(ChangeMode mode, PermissionProvider provider, UUID uuid, Set<String> permissions) {
        if (mode == ChangeMode.ADD) {
            provider.addUserPermissions(uuid, permissions);
        } else if (mode == ChangeMode.REMOVE) {
            provider.removeUserPermissions(uuid, permissions);
        }
    }

    private void permChange(ChangeMode mode, PermissionProvider provider, String group, Set<String> permissions) {
        if (mode == ChangeMode.ADD) {
            provider.addGroupPermissions(group, permissions);
        } else if (mode == ChangeMode.REMOVE) {
            provider.removeGroupPermissions(group, permissions);
        }
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String group = this.group ? "group[s] " : "";
        return "all permissions of " + group + this.permissables.toString(ctx, debug);
    }

}
