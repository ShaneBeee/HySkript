package com.github.skriptdev.skript.plugin.elements.expressions.other;

import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ExprUUID extends PropertyExpression<Object, UUID> {

    public static void register(SkriptRegistration registration) {
        registration.newPropertyExpression(ExprUUID.class, UUID.class,
                "uuid", "players/playerrefs/worlds")
            .name("UUID of Object")
            .description("Get the UUID of a player, player ref, entity, or world.")
            .examples("set {_uuid} to uuid of {_player}")
            .since("1.0.0")
            .register();
    }

    @SuppressWarnings("removal") // Entity#getUuid TODO (what else would you use?)
    @Override
    public @Nullable UUID getProperty(@NotNull Object owner) {
        return switch (owner) {
            case PlayerRef playerRef -> playerRef.getUuid();
            case Entity entity -> entity.getUuid();
            case World world -> world.getWorldConfig().getUuid();
            default -> null;
        };
    }

}
