package com.github.skriptdev.skript.plugin.elements.expressions.other;

import com.hypixel.hytale.math.vector.Vector3f;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.NotNull;

public class ExprVector3f implements Expression<Vector3f> {

    public static void register(SkriptRegistration registration) {
        registration.newExpression(ExprVector3f.class, Vector3f.class, true,
                "vector3f from %number%, %number%(, and) %number%",
                "vector3f(%number%,[ ]%number%,[ ]%number%)")
            .name("Vector3f")
            .description("Represents a vector in 3D space using floats.",
                "Often used for the rotation of entities in a world.")
            .examples("set {_pos} to vector3f from 1.1, 2, 3.3",
                "set {_pos} to vector3f(1.1, 2.5, 3)")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<Number> x, y, z;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.x = (Expression<Number>) expressions[0];
        this.y = (Expression<Number>) expressions[1];
        this.z = (Expression<Number>) expressions[2];
        return true;
    }

    @Override
    public Vector3f[] getValues(@NotNull TriggerContext ctx) {
        if (this.x != null && this.y != null && this.z != null) {
            Number x = this.x.getSingle(ctx).orElse(null);
            Number y = this.y.getSingle(ctx).orElse(null);
            Number z = this.z.getSingle(ctx).orElse(null);
            if (x != null && y != null && z != null) {
                return new Vector3f[]{new Vector3f(x.floatValue(), y.floatValue(), z.floatValue())};
            }
        }
        return null;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "vector3f from " + this.x.toString(ctx, debug) + ", " + this.y.toString(ctx, debug) + ", " + this.z.toString(ctx, debug);
    }

}
