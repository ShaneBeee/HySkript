package com.github.skriptdev.skript.plugin.elements.expressions.other;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ExprClassInfoOf implements Expression<String> {

    public static void register(SkriptRegistration registration) {
        registration.newExpression(ExprClassInfoOf.class, String.class, false,
                "class[ ]info of %objects%")
            .name("Class Info")
            .description("Returns the name of the class/type of the given object.")
            .since("1.0.0")
            .register();
    }

    private Expression<?> object;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.object = expressions[0];
        return true;
    }

    @Override
    public String[] getValues(@NotNull TriggerContext ctx) {
        Object[] array = this.object.getArray(ctx);
        String[] strings = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            Optional<? extends Type<?>> byClass = TypeManager.getByClass(array[i].getClass());
            if (byClass.isEmpty()) {
                strings[i] = "Class: " + array[i].getClass().getSimpleName();
                continue;
            }
            Type<?> type = byClass.get();
            String name = type.getDocumentation().getName();
            strings[i] = name != null ? name : type.getBaseName();
        }
        return strings;
    }

    @Override
    public boolean isSingle() {
        return this.object.isSingle();
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "classinfo of " + this.object.toString(ctx, debug);
    }

}
