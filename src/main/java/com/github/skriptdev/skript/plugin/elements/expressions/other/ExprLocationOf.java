package com.github.skriptdev.skript.plugin.elements.expressions.other;

import com.github.skriptdev.skript.api.hytale.Block;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExprLocationOf extends PropertyExpression<Object, Location> {

    public static void register(SkriptRegistration registration) {
        registration.newPropertyExpression(ExprLocationOf.class, Location.class,
                "location", "blocks/entities")
            .name("Location of Block/Entity")
            .description("Returns the location of a block or entity.")
            .examples("set {_loc} to location of context-player")
            .since("1.0.0")
            .register();
    }

    @SuppressWarnings("removal")
    @Override
    public @Nullable Location getProperty(@NotNull Object owner) {
        if (owner instanceof Entity entity) {
            World world = entity.getWorld();
            assert world != null;
            TransformComponent transform = entity.getTransformComponent();
            return new Location(world.getName(), transform.getPosition(), transform.getRotation());
        } else if (owner instanceof Block block) {
            return block.getLocation();
        }
        return null;
    }

}
