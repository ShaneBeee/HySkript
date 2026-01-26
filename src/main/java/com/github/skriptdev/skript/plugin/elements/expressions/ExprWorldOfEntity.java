package com.github.skriptdev.skript.plugin.elements.expressions;

import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.Nullable;

public class ExprWorldOfEntity extends PropertyExpression<Entity, World> {

    public static void register(SkriptRegistration registration) {
        registration.newPropertyExpression(ExprWorldOfEntity.class, World.class,
                "world", "entities")
            .name("World of Entity")
            .description("Returns the world of an entity.")
            .examples("set {_world} to world of context-player")
            .since("INSERT VERSION")
            .register();
    }

    @Override
    public @Nullable World getProperty(Entity entity) {
        return entity.getWorld();
    }

}
