package com.github.skriptdev.skript.plugin.elements.expressions.other;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Vector3f;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ExprLocationRotation implements Expression<Vector3f> {

    public static void register(SkriptRegistration reg) {
        reg.newExpression(ExprLocationRotation.class, Vector3f.class, true,
                "rotation of %location%")
            .name("Location Rotation")
            .description("Get/set the rotation of a location.",
                "Do note that setting the rotation of a location will not rotate an entity it may be attached to.")
            .examples("set rotation of {_loc} to vector3f(0, 10, 0)",
                "set {_rot} to rotation of location of player")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<Location> location;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.location = (Expression<Location>) expressions[0];
        return true;
    }

    @Override
    public Vector3f[] getValues(@NotNull TriggerContext ctx) {
        Location location = this.location.getSingle(ctx).orElse(null);
        if (location == null) return null;

        return new Vector3f[]{location.getRotation()};
    }

    @Override
    public Optional<Class<?>[]> acceptsChange(@NotNull ChangeMode mode) {
        if (mode == ChangeMode.SET) {
            return Optional.of(new Class<?>[]{Vector3f.class});
        }
        return Optional.empty();
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public void change(@NotNull TriggerContext ctx, @NotNull ChangeMode changeMode, Object @NotNull [] changeWith) {
        if (changeWith == null || changeWith.length == 0 || !(changeWith[0] instanceof Vector3f vector3f)) {
            return;
        }

        this.location.getSingle(ctx).ifPresent(loc -> loc.setRotation(vector3f));
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "rotation of " + this.location.toString(ctx, debug);
    }

}
