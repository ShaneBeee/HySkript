package com.github.skriptdev.skript.plugin.elements.expressions.other;

import com.hypixel.hytale.server.core.Message;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import io.github.syst3ms.skriptparser.util.color.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ExprMessageColor extends PropertyExpression<Message, String> {

    public static void register(SkriptRegistration registration) {
        registration.newPropertyExpression(ExprMessageColor.class, String.class,
                "message color", "messages")
            .name("Message Color")
            .description("Get/set the color of a message.")
            .examples("set message color of {_msg} to \"\"",
                "on player ready:",
                "\tset {_message} to message from \"Welcome to the server %context-player%\"",
                "\tset message color of {_message} to \"##0CB1F7\"",
                "\tsend {_message} to player")
            .since("INSERT VERSION")
            .register();
    }

    @Override
    public @Nullable String getProperty(Message message) {
        return message.getColor();
    }

    @Override
    public Optional<Class<?>[]> acceptsChange(@NotNull ChangeMode mode) {
        return Optional.of(new Class<?>[]{String.class});
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void change(TriggerContext ctx, ChangeMode changeMode, Object[] changeWith) {
        Optional<? extends Message> messageSingle = getOwner().getSingle(ctx);
        if (messageSingle.isEmpty()) return;

        Message message = messageSingle.get();
        if (changeWith[0] instanceof String s) {
            message.color(s);
        } else if (changeWith[0] instanceof Color color) {
            message.color(color.toJavaColor());
        }
    }

}
