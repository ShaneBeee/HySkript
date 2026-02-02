package com.github.skriptdev.skript.plugin.elements.expressions.other;

import com.hypixel.hytale.server.core.Message;
import fi.sulku.hytale.TinyMsg;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ExprMessage implements Expression<Message> {

    public static void register(SkriptRegistration registration) {
        registration.newExpression(ExprMessage.class, Message.class, true,
                "[new] [raw] message [from] %string%",
                "[new] translated message [from] %string%",
                "[new] message translation [from] %string%",
                "[new] formatted [message] [from] %string%")
            .name("Message")
            .description("Create a new message from a string.",
                "The translated option will use a translation key from the game's lang file instead of a raw string.",
                "The formatted option will parse the string as a TinyMessage string.")
            // TODO link to a wiki page regarding messages
            .examples("on player ready:",
                "\tset {_message} to message from \"Welcome to the server %context-player%\"",
                "\tset message color of {_message} to \"##0CB1F7\"",
                "\tsend {_message} to player",
                "set {_message} to message translation from \"server.chat.playerMessage\"",
                "on player ready:",
                "\tsend formatted message from \"<gradient:F5330C:27F5D6>WELCOME TO THE SERVER!!!\" to player")
            .since("1.0.0")
            .register();
    }

    private Expression<String> string;
    private boolean translation;
    private boolean formatted;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.string = (Expression<String>) expressions[0];
        this.translation = matchedPattern == 1 || matchedPattern == 2;
        this.formatted = matchedPattern == 3;
        return true;
    }

    @Override
    public Message[] getValues(@NotNull TriggerContext ctx) {
        Optional<? extends String> single = this.string.getSingle(ctx);
        return single.map(s -> {
            Message m;
            if (this.formatted) {
                m = TinyMsg.parse(s);
            } else if (this.translation) {
                m = Message.translation(s);
            } else {
                m = Message.raw(s);
            }
            return new Message[]{m};
        }).orElse(null);
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String type = this.formatted ? "formatted" : this.translation ? "translated" : "raw";
        return type + " message from " + this.string.toString(ctx, debug);
    }

}
