package com.github.skriptdev.skript.plugin.elements.conditions.other;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Literal;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.ConditionalExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.Type;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class CondObjectOfType extends ConditionalExpression {

    public static void register(SkriptRegistration reg) {
        reg.newExpression(CondObjectOfType.class, Boolean.class, true,
                "%objects% (is a[n]|are) %*types%")
            .name("Object is of Type")
            .description("Checks if the objects are of the specified types.")
            .examples("if {var} is a Player:",
                "if {var} is an Entity:")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<?> objects;
    private Type<?> type;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.objects = expressions[0];
        Literal<Type<?>> type = (Literal<Type<?>>) expressions[1];
        Optional<? extends Type<?>> single = type.getSingle();
        if (single.isEmpty()) {
            return false;
        }
        this.type = single.get();
        return true;
    }

    @Override
    public boolean check(@NotNull TriggerContext ctx) {
        return this.objects.check(ctx, o ->
            this.type.getTypeClass().isAssignableFrom(o.getClass()));
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String plural = this.objects.isSingle() ? " is a " : " are ";
        return this.objects + plural + this.type;
    }

}
