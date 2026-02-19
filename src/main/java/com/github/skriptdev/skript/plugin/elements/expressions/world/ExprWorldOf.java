package com.github.skriptdev.skript.plugin.elements.expressions.world;

import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class ExprWorldOf implements Expression<World> {

    public static void register(SkriptRegistration registration) {
        registration.newExpression(ExprWorldOf.class, World.class,
                true, "world of %location/playerref/entity%")
            .name("World of")
            .description("Get the world of a Location/Entity/PlayerRef.",
                "The world of a location can also be set.")
            .examples("set {_world} to world of {_location}",
                "set world of {_loc} to world of player")
            .since("1.0.0")
            .register();
    }

    private Expression<?> owner;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.owner = expressions[0];
        return true;
    }

    @Override
    public World[] getValues(@NotNull TriggerContext ctx) {
        Optional<?> single = this.owner.getSingle(ctx);
        if (single.isEmpty()) return null;

        Object o = single.get();
        if (o instanceof Location location) {
            return new World[]{Universe.get().getWorld(location.getWorld())};
        } else if (o instanceof PlayerRef ref) {
            UUID worldUuid = ref.getWorldUuid();
            if (worldUuid == null) return null;
            return new World[]{Universe.get().getWorld(worldUuid)};
        } else if (o instanceof Entity entity) {
            return new World[]{entity.getWorld()};
        }
        return new World[0];
    }

    @Override
    public Optional<Class<?>[]> acceptsChange(@NotNull ChangeMode mode) {
        if (mode == ChangeMode.SET) return Optional.of(new Class<?>[]{World.class, String.class});
        return Optional.empty();
    }

    @Override
    public void change(@NotNull TriggerContext ctx, @NotNull ChangeMode changeMode, Object @NotNull [] changeWith) {
        Object o = this.owner.getSingle(ctx).orElse(null);
        if (o == null) return;
        if (changeMode == ChangeMode.SET) {
            if (o instanceof Location loc) {
                if (changeWith[0] instanceof String s) loc.setWorld(s);
                else if (changeWith[0] instanceof World world) loc.setWorld(world.getName());
            }
        }
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends World> getReturnType() {
        return World.class;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "world of " + this.owner.toString(ctx, debug);
    }

}
