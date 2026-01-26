package com.github.skriptdev.skript.plugin.elements.expressions;

import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ExprWorldOfLocation extends PropertyExpression<Location, World> {

    public static void register(SkriptRegistration registration) {
        registration.newPropertyExpression(ExprWorldOfLocation.class, World.class,
                "world", "locations")
            .name("World of Location")
            .description("Returns the world of a location.",
                "The world of a location can also be set.")
            .examples("set {_world} to world of {_location}",
                "set world of {_loc} to world of player")
            .since("INSERT VERSION")
            .register();
    }

    @Override
    public @Nullable World getProperty(Location location) {
        return Universe.get().getWorld(location.getWorld());
    }

    @Override
    public Optional<Class<?>[]> acceptsChange(@NotNull ChangeMode mode) {
        if (mode == ChangeMode.SET) return Optional.of(new Class<?>[]{World.class, String.class});
        return Optional.empty();
    }

    @Override
    public void change(@NotNull TriggerContext ctx, @NotNull ChangeMode changeMode, Object @NotNull [] changeWith) {
        Location loc = getOwner().getSingle(ctx).orElse(null);
        if (loc == null) return;
        if (changeMode == ChangeMode.SET) {
            if (changeWith[0] instanceof String s) loc.setWorld(s);
            else if (changeWith[0] instanceof World world) loc.setWorld(world.getName());
        }
    }

}
