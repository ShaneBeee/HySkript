package com.github.skriptdev.skript.plugin.elements.expressions.entity;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.Nullable;

public class ExprName extends PropertyExpression<Object, String> {

    public static void register(SkriptRegistration registration) {
        registration.newPropertyExpression(ExprName.class, String.class, "name[s]", "object")
            .name("Name of Object")
            .description("Returns the name of an object.",
                "Currently supports players, entities, and worlds.")
            .examples("set {_name} to name of player",
                "set {_w} to name of world of player")
            .since("1.0.0")
            .register();
    }

    @Override
    public @Nullable String getProperty(Object object) {
        return switch (object) {
            case PlayerRef playerRef -> playerRef.getUsername();
            case Player player -> player.getDisplayName();
            case Entity entity -> entity.getLegacyDisplayName();
            case World world -> world.getName();
            case GameMode gameMode -> gameMode.name();
            default -> null;
        };
    }

}
