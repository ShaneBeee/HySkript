package com.github.skriptdev.skript.plugin.elements.expressions;

import com.hypixel.hytale.server.core.Message;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ExprMessageLink extends PropertyExpression<Message,String> {

    public static void register(SkriptRegistration registration) {
        registration.newPropertyExpression(ExprMessageLink.class, String.class,
                "message link", "message")
            .name("Message Link")
            .description("Get/set the link of a message.")
            .examples("on player ready:",
                "\tset {_message} to message from \"Don't forget to check out our discord!\"",
                "\tset message link of {_message} to \"https://discord.com/my_link\"",
                "\tsend {_message} to player")
            .since("INSERT VERSION")
            .register();
    }

    @Override
    public @Nullable String getProperty(Message message) {
        return message.getFormattedMessage().link;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Optional<Class<?>[]> acceptsChange(@NotNull ChangeMode mode) {
        if (mode == ChangeMode.SET || mode == ChangeMode.DELETE) return Optional.of(new Class<?>[]{String.class});
        return Optional.empty();
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public void change(@NotNull TriggerContext ctx, @NotNull ChangeMode changeMode, Object @NotNull [] changeWith) {
        Expression<Message> owner = getOwner();
        String link = changeMode != null && changeWith[0] != null ? (String) changeWith[0] : null;
        owner.getSingle(ctx).ifPresent(message ->
            message.getFormattedMessage().link = link);
    }

}
