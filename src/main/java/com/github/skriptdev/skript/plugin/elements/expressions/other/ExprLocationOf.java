package com.github.skriptdev.skript.plugin.elements.expressions.other;

import com.github.skriptdev.skript.api.hytale.Block;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ExprLocationOf implements Expression<Location> {

    public static void register(SkriptRegistration registration) {
        registration.newExpression(ExprLocationOf.class, Location.class, false,
                "location[s] of %blocks/entities%",
                "%entities/blocks%'[s] location[s]")
            .name("Location of Block/Entity")
            .description("Returns the location of a block or entity.")
            .examples("set {_loc} to location of context-player")
            .since("1.0.0")
            .register();
    }

    private Expression<?> objects;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.objects = expressions[0];
        return true;
    }

    @SuppressWarnings("removal")
    @Override
    public Location[] getValues(@NotNull TriggerContext ctx) {
        List<Location> locations = new ArrayList<>();
        for (Object o : this.objects.getArray(ctx)) {
            if (o instanceof Entity entity) {
                World world = entity.getWorld();
                assert world != null;
                TransformComponent transform = entity.getTransformComponent();
                locations.add(new Location(world.getName(), transform.getPosition()));
            } else if (o instanceof Block block) {
                locations.add(block.getLocation());
            }
        }
        return locations.toArray(new Location[0]);
    }

    @Override
    public boolean isSingle() {
        return this.objects.isSingle();
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String plural = this.objects.isSingle() ? "" : "s";
        return "location" + plural + " of " + this.objects.toString(ctx, debug);
    }

}
