package com.github.skriptdev.skript.plugin.elements.expressions.other;

import com.github.skriptdev.skript.api.hytale.Direction;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.math.vector.Location;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ExprLocationDirection implements Expression<Location> {

    public static void register(SkriptRegistration reg) {
        // TODO its broken when not using "of" (parser issue)
        reg.newExpression(ExprLocationDirection.class, Location.class, true,
                "location %direction% [of] %location%",
                "location %number% [block[s]] %direction% [of] %location%")
            .name("Location Direction")
            .description("Returns a location at the specified direction and location with an optional offset.",
                "The parser is broken, and even thought 'of' is optional, you have to put it.")
            .examples("set {_loc} to location above {_loc}",
                "set {_loc} to location 3 north of location of player",
                "set {_loc} to location 3 blocks below location of player")
            .since("1.0.0")
            .register();
    }

    private Expression<Number> offset;
    private Expression<Direction> direction;
    private Expression<Location> location;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        if (matchedPattern == 0) {
            this.direction = (Expression<Direction>) expressions[0];
            this.location = (Expression<Location>) expressions[1];
        } else {
            this.offset = (Expression<Number>) expressions[0];
            this.direction = (Expression<Direction>) expressions[1];
            this.location = (Expression<Location>) expressions[2];
        }
        return true;
    }

    @Override
    public Location[] getValues(@NotNull TriggerContext ctx) {
        Optional<? extends Direction> directionSingle = this.direction.getSingle(ctx);
        Optional<? extends Location> locationSingle = this.location.getSingle(ctx);
        if (directionSingle.isEmpty() || locationSingle.isEmpty()) return null;

        double offset = 1;
        if (this.offset != null) {
            Optional<? extends Number> s = this.offset.getSingle(ctx);
            if (s.isPresent()) offset = s.get().doubleValue();
        }

        Direction direction = directionSingle.get();
        Location location = locationSingle.get();
        Location apply = direction.apply(location, offset);
        return new Location[]{apply};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Location> getReturnType() {
        return Location.class;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String amount = this.offset != null ? this.offset.toString(ctx, debug) + " blocks " : "";
        return "location " + amount + this.direction.toString(ctx, debug) + " " + this.location.toString(ctx, debug);
    }

}
