package com.github.skriptdev.skript.plugin.elements.expressions.other;

import com.hypixel.hytale.math.vector.Vector3d;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.NotNull;

public class ExprVector3d implements Expression<Vector3d> {

    public static void register(SkriptRegistration registration) {
        registration.newExpression(ExprVector3d.class, Vector3d.class, true,
                "Vector3d from %number%, %number%(, and) %number%",
                "Vector3d(%number%,[ ]%number%,[ ]%number%)")
            .name("Vector3d")
            .description("Represents a vector in 3D space using doubles.",
                "Often used for the position of entities in a world.")
            .examples("set {_pos} to Vector3d from 1.1, 2, 3.3",
                "set {_pos} to Vector3d(1.1, 2.5, 3)")
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
    public Vector3d[] getValues(@NotNull TriggerContext ctx) {
        if (this.x != null && this.y != null && this.z != null) {
            Number x = this.x.getSingle(ctx).orElse(null);
            Number y = this.y.getSingle(ctx).orElse(null);
            Number z = this.z.getSingle(ctx).orElse(null);
            if (x != null && y != null && z != null) {
                return new Vector3d[]{new Vector3d(x.floatValue(), y.floatValue(), z.floatValue())};
            }
        }
        return null;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "Vector3d from " + this.x.toString(ctx, debug) + ", " + this.y.toString(ctx, debug) + ", " + this.z.toString(ctx, debug);
    }

}
