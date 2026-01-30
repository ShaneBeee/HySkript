package com.github.skriptdev.skript.plugin.elements.expressions.other;

import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.Nullable;

public class ExprLocationOf extends PropertyExpression<Entity, Location> {

    public static void register(SkriptRegistration registration) {
        registration.newPropertyExpression(ExprLocationOf.class, Location.class,
                "location", "entities")
            .name("Location of Entity")
            .description("Returns the location of an entity.")
            .examples("set {_loc} to location of context-player")
            .since("INSERT VERSION")
            .register();
    }

    @Override
    public @Nullable Location getProperty(Entity entity) {
        World world = entity.getWorld();
        assert world != null;
        TransformComponent transform = entity.getTransformComponent();
        return new Location(world.getName(), transform.getTransform());
    }

}
