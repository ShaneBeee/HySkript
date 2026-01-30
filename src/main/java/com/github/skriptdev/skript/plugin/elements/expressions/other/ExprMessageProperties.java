package com.github.skriptdev.skript.plugin.elements.expressions.other;

import com.hypixel.hytale.protocol.MaybeBool;
import com.hypixel.hytale.server.core.Message;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ExprMessageProperties implements Expression<Boolean> {

    public static void register(SkriptRegistration registration) {
        registration.newExpression(ExprMessageProperties.class, Boolean.class, true,
                "bold [message[ property of %message%",
                "italic [message[ property of %message%",
                "monospace [message[ property of %message%",
                "underlined [message[ property of %message%",
                "markup enabled [message[ property of %message%")
            .name("Message Properties")
            .description("Get/set different properties of a message.")
            .examples("set bold property of {_msg} to true",
                "set monospace property of {_msg} to false")
            .since("INSERT VERSION")
            .register();
    }

    private int pattern;
    private Expression<Message> message;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.pattern = matchedPattern;
        this.message = (Expression<Message>) expressions[0];
        return true;
    }

    @Override
    public Boolean[] getValues(@NotNull TriggerContext ctx) {
        Optional<? extends Message> messageSingle = this.message.getSingle(ctx);
        if (messageSingle.isPresent()) {
            Message message = messageSingle.get();

            Boolean value = switch (this.pattern) {
                case 0 -> translateMaybeBool(message.getFormattedMessage().bold);
                case 1 -> translateMaybeBool(message.getFormattedMessage().italic);
                case 2 -> translateMaybeBool(message.getFormattedMessage().monospace);
                case 3 -> translateMaybeBool(message.getFormattedMessage().underlined);
                case 4 -> message.getFormattedMessage().markupEnabled;
                default -> null;
            };
            return new Boolean[]{value};
        }
        return null;
    }

    @Override
    public Optional<Class<?>[]> acceptsChange(@NotNull ChangeMode mode) {
        if (mode == ChangeMode.SET) return Optional.of(new Class<?>[]{Boolean.class});
        return Optional.empty();
    }

    @Override
    public void change(@NotNull TriggerContext ctx, @NotNull ChangeMode changeMode, Object[] changeWith) {
        if (!(changeWith[0] instanceof Boolean bool)) return;
        this.message.getSingle(ctx).ifPresent(message -> {
            switch (this.pattern) {
                case 0 -> message.bold(bool);
                case 1 -> message.italic(bool);
                case 2 -> message.monospace(bool);
                case 3 -> message.getFormattedMessage().underlined = bool ? MaybeBool.True : MaybeBool.False;
                case 4 -> message.getFormattedMessage().markupEnabled = bool;
            }
        });
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String s = new String[]{"bold", "italic", "monospace", "underlined", "markup enabled"}[pattern];
        return s + " message property of " + this.message.toString(ctx, debug);
    }

    private Boolean translateMaybeBool(MaybeBool bool) {
        return switch (bool) {
            case Null -> null;
            case True -> true;
            case False -> false;
        };
    }

}
