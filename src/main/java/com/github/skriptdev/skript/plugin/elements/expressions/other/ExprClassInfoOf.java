package com.github.skriptdev.skript.plugin.elements.expressions.other;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;

public class ExprClassInfoOf implements Expression<String> {

    public static void register(SkriptRegistration registration) {
        registration.addExpression(ExprClassInfoOf.class, String.class,
            "class[ ]info of %objects%");
    }

    private Expression<?> object;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        this.object = expressions[0];
        return true;
    }


    @Override
    public String[] getValues(TriggerContext ctx) {
        Object[] array = this.object.getArray(ctx);
        String[] strings = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            strings[i] = array[i].getClass().getName();
        }
        return strings;
    }

    @Override
    public boolean isSingle() {
        return this.object.isSingle();
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "classinfo of " + this.object.toString(ctx, debug);
    }

}
