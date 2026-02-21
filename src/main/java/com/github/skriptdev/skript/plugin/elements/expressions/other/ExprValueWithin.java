package com.github.skriptdev.skript.plugin.elements.expressions.other;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Literal;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import io.github.syst3ms.skriptparser.types.changers.Changer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ExprValueWithin implements Expression<Object> {

    public static void register(SkriptRegistration reg) {
        reg.newExpression(ExprValueWithin.class, Object.class, true,
                "value[:s] within %objects%",
                "%*type% within %objects%")
            .name("Value Within")
            .description("Gets the value within another object, such as a variable.")
            .examples("delete entity within {_e}")
            .since("INSERT VERSION")
            .register();
    }

    private boolean plural;
    private Type<?> type;
    private Expression<?> objects;
    private Changer<Object> changer;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        Expression<?> objects;
        if (matchedPattern == 0) {
            objects = expressions[0];
            this.plural = parseContext.hasMark("s");
        } else {
            Literal<Type<?>> typeLiteral = (Literal<Type<?>>) expressions[0];
            typeLiteral.getSingle().ifPresent(value -> this.type = value);
            objects = expressions[1];

            // TODO plural
            // I dunno how to get whether a plural type was used
        }
        SkriptLogger logger = parseContext.getLogger();
        if (this.plural && objects.isSingle()) {
            logger.error("You cannot get multiple elements from a single value.", ErrorType.SEMANTIC_ERROR);
            return false;
        }
        if (this.type == null) {
            this.objects = objects;
        } else {
            Optional<? extends Expression<?>> exp = objects.convertExpression(this.type.getTypeClass());
            if (exp.isEmpty()) {
                logger.error("Cannot convert " + objects.toString(TriggerContext.DUMMY, true) +
                    " to " + this.type.getBaseName(), ErrorType.SEMANTIC_ERROR);
                return false;
            }
            this.objects = exp.get();
        }
        return true;
    }

    @Override
    public Object[] getValues(@NotNull TriggerContext ctx) {
        return this.objects.getValues(ctx);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<Class<?>[]> acceptsChange(@NotNull ChangeMode mode) {
        Type<?> type;
        if (this.type != null) {
            type = this.type;
        } else {
            Optional<? extends Type<?>> typeByClass = TypeManager.getByClass(getReturnType());
            if (typeByClass.isEmpty()) {
                return Optional.empty();
            }
            type = typeByClass.get();
        }

        Optional<? extends Changer<?>> defaultChanger = type.getDefaultChanger();
        if (defaultChanger.isEmpty()) {
            return Optional.empty();
        }

        this.changer = (Changer<Object>) defaultChanger.get();
        return Optional.ofNullable(this.changer.acceptsChange(mode));
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void change(TriggerContext ctx, ChangeMode changeMode, Object[] changeWith) {
        if (this.changer == null) return;

        this.changer.change(getArray(ctx), changeWith, changeMode);
    }

    @Override
    public boolean isSingle() {
        return this.objects.isSingle();
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        if (this.type != null) {
            return this.type.getBaseName() + " within " + this.objects.toString(ctx, debug);
        }
        String value = this.plural ? "values" : "value";
        return value + " within " + this.objects.toString(ctx, debug);
    }

}
