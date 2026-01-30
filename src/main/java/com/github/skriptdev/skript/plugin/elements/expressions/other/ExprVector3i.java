package com.github.skriptdev.skript.plugin.elements.expressions.other;

import com.hypixel.hytale.math.vector.Vector3i;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.NotNull;

public class ExprVector3i implements Expression<Vector3i> {

    public static void register(SkriptRegistration registration) {
        registration.newExpression(ExprVector3i.class, Vector3i.class, true,
                "vector3i from %number%, %number%(, and) %number%",
                "vector3i(%number%,[ ]%number%,[ ]%number%)")
            .name("Vector3i")
            .description("Represents a vector in 3D space using integers.",
                "Often used for the position of blocks in a world.")
            .examples("set {_pos} to vector3i from 1, 2, 3",
                "set {_pos} to vector3i(1, 2, 3)")
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
    public Vector3i[] getValues(@NotNull TriggerContext ctx) {
        if (this.x != null && this.y != null && this.z != null) {
            Number x = this.x.getSingle(ctx).orElse(null);
            Number y = this.y.getSingle(ctx).orElse(null);
            Number z = this.z.getSingle(ctx).orElse(null);
            if (x != null && y != null && z != null) {
                return new Vector3i[]{new Vector3i(x.intValue(), y.intValue(), z.intValue())};
            }
        }
        return null;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "vector3i from " + this.x.toString(ctx, debug) + ", " + this.y.toString(ctx, debug) + ", " + this.z.toString(ctx, debug);
    }

}
