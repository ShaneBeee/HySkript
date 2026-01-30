package com.github.skriptdev.skript.plugin.elements.expressions.other;

import com.hypixel.hytale.protocol.BoolParamValue;
import com.hypixel.hytale.protocol.DoubleParamValue;
import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.protocol.IntParamValue;
import com.hypixel.hytale.protocol.LongParamValue;
import com.hypixel.hytale.protocol.ParamValue;
import com.hypixel.hytale.protocol.StringParamValue;
import com.hypixel.hytale.server.core.Message;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

public class ExprMessageParam implements Expression<Object> {

    public static void register(SkriptRegistration registration) {
        registration.newExpression(ExprMessageParam.class, Object.class, true,
                "[message] string param %string% of %message%",
                "[message] bool[ean] param %string% of %message%",
                "[message] double param %string% of %message%",
                "[message] int param %string% of %message%",
                "[message] long param %string% of %message%",
                "[message] formatted [message] param %string% of %message%")
            .name("Message Parameter")
            .description("Get/set a parameters of a message.",
                "These are used for filling in placeholders in translated message.")
            .examples("on player ready:",
                "\tset {_m} to translated message from \"server.chat.playerMessage\"",
                "\tset message string param \"username\" of {_m} to \"SomeUserName\"",
                "\tset message string param \"message\" of {_m} to \"some message I guess\"",
                "\tsend {_m} to player")
            .since("INSERT VERSION")
            .register();
    }

    private int pattern;
    private Expression<String> param;
    private Expression<Message> message;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.pattern = matchedPattern;
        this.param = (Expression<String>) expressions[0];
        this.message = (Expression<Message>) expressions[1];
        return true;
    }

    @Override
    public Object[] getValues(@NotNull TriggerContext ctx) {
        Optional<? extends Message> messageSingle = this.message.getSingle(ctx);
        Optional<? extends String> paramSingle = this.param.getSingle(ctx);
        if (messageSingle.isPresent() && paramSingle.isPresent()) {
            Message message = messageSingle.get();
            if (this.pattern == 5) {
                Map<String, FormattedMessage> messageParams = message.getFormattedMessage().messageParams;
                if (messageParams == null || !messageParams.containsKey(paramSingle.get())) return null;
                FormattedMessage formattedMessage = messageParams.get(paramSingle.get());
                return formattedMessage == null ? null : new Message[]{new Message(formattedMessage)};
            } else {
                Map<String, ParamValue> params = message.getFormattedMessage().params;
                if (params == null || !params.containsKey(paramSingle.get())) return null;

                ParamValue paramValue = params.get(paramSingle.get());

                if (this.pattern == 0 && paramValue instanceof StringParamValue sv) {
                    return new String[]{sv.value};
                } else if (this.pattern == 1 && paramValue instanceof BoolParamValue bv) {
                    return new Boolean[]{bv.value};
                } else if (this.pattern == 2 && paramValue instanceof DoubleParamValue dv) {
                    return new Double[]{dv.value};
                } else if (this.pattern == 3 && paramValue instanceof IntParamValue iv) {
                    return new Integer[]{iv.value};
                } else if (this.pattern == 4 && paramValue instanceof LongParamValue lv) {
                    return new Long[]{lv.value};
                }
            }
        }
        return null;
    }

    @Override
    public Optional<Class<?>[]> acceptsChange(@NotNull ChangeMode mode) {
        if (mode != ChangeMode.SET) return Optional.empty();

        return Optional.of(new Class<?>[]{getReturnType()});
    }

    @Override
    public void change(@NotNull TriggerContext ctx, @NotNull ChangeMode changeMode, Object @NotNull [] changeWith) {
        Optional<? extends Message> messageSingle = this.message.getSingle(ctx);
        Optional<? extends String> paramSingle = this.param.getSingle(ctx);
        if (messageSingle.isEmpty() || paramSingle.isEmpty()) return;

        Object changeObject = changeWith[0];
        if (changeObject == null) return;

        Message message = messageSingle.get();
        String param = paramSingle.get();

        switch (changeObject) {
            case String s when this.pattern == 0 -> message.param(param, s);
            case Boolean b when this.pattern == 1 -> message.param(param, b);
            case Double d when this.pattern == 2 -> message.param(param, d);
            case Integer i when this.pattern == 3 -> message.param(param, i);
            case Long l when this.pattern == 4 -> message.param(param, l);
            case Message m when this.pattern == 5 -> message.param(param, m);
            default -> {
            }
        }
    }

    @Override
    public Class<?> getReturnType() {
        return switch (this.pattern) {
            case 0 -> String.class;
            case 1 -> Boolean.class;
            case 2 -> Double.class;
            case 3 -> Integer.class;
            case 4 -> Long.class;
            case 5 -> Message.class;
            default -> null;
        };
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String type = switch (this.pattern) {
            case 0 -> "String";
            case 1 -> "Boolean";
            case 2 -> "Double";
            case 3 -> "Integer";
            case 4 -> "Long";
            case 5 -> "Formatted Message";
            default -> "Unknown";
        };
        return "message " + type + " param " + this.param.toString(ctx, debug) + " of " + this.message.toString(ctx, debug);
    }

}
