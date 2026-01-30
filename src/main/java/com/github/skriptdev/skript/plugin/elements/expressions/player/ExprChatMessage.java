package com.github.skriptdev.skript.plugin.elements.expressions.player;

import com.github.skriptdev.skript.plugin.elements.events.player.EvtPlayerChat.PlayerChatContext;
import com.hypixel.hytale.server.core.Message;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ExprChatMessage implements Expression<String> {

    public static void register(SkriptRegistration registration) {
        registration.newExpression(ExprChatMessage.class, String.class, true,
                "chat message")
            .name("Chat Message")
            .description("Returns the message sent in chat, can be changed.")
            .examples("on chat:",
                "\tloop {badwords::*}:",
                "\t\tif chat message contains loop-value:",
                "\t\t\treplace loop-value with \"****\" in chat message")
            .since("INSERT VERSION")
            .register();
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        return true;
    }

    @Override
    public String[] getValues(@NotNull TriggerContext ctx) {
        if (ctx instanceof PlayerChatContext chat) {
            return chat.getMessage();
        }
        return null;
    }

    @Override
    public Optional<Class<?>[]> acceptsChange(@NotNull ChangeMode mode) {
        if (mode == ChangeMode.SET) return Optional.of(new Class<?>[]{String.class, Message.class});
        return Optional.empty();
    }

    @Override
    public void change(@NotNull TriggerContext ctx, @NotNull ChangeMode changeMode, Object @NotNull [] changeWith) {
        if (ctx instanceof PlayerChatContext chat) {
            if (changeWith[0] instanceof String s) {
                chat.getEvent().setContent(s);
            } else if (changeWith[0] instanceof Message message) {
                String rawText = message.getRawText();
                if (rawText == null) rawText = "";
                chat.getEvent().setContent(rawText);
            }
        }
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "chat message";
    }

}
