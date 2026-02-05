package com.github.skriptdev.skript.plugin.elements.effects.other;

import com.github.skriptdev.skript.api.skript.event.PlayerContext;
import com.github.skriptdev.skript.api.utils.Utils;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.receiver.IMessageReceiver;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.types.TypeManager;
import org.jetbrains.annotations.NotNull;

public class EffSendMessage extends Effect {

    public static void register(SkriptRegistration registration) {
        registration.newEffect(EffSendMessage.class,
                "send [message[s]] %objects% [to %-messagereceivers%]")
            .name("Send Message")
            .description("Sends a message to a command sender such as a player or the console.",
                "See [Message Format](https://github.com/SkriptDev/HySkript/wiki/Tutorial-Message-Format) on the wiki" +
                    "for info about formatting messages.")
            .examples("send \"Welcome to the server\" to player")
            .since("1.0.0")
            .register();
    }

    private Expression<?> messages;
    private Expression<IMessageReceiver> senders;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull ParseContext parseContext) {
        this.messages = exprs[0];
        if (exprs.length > 1) { // TODO whatttt?!?!?
            this.senders = (Expression<IMessageReceiver>) exprs[1];
        }
        return true;
    }

    @Override
    protected void execute(@NotNull TriggerContext ctx) {
        Object[] messages = this.messages.getValues(ctx);
        if (messages == null || messages.length == 0) return;

        if (this.senders != null) {
            for (IMessageReceiver commandSender : this.senders.getArray(ctx)) {
                for (Object value : messages) {
                    if (value instanceof String string) {
                        Utils.sendMessage(commandSender, string);
                    } else if (value instanceof Message message) {
                        commandSender.sendMessage(message);
                    } else {
                        Utils.sendMessage(commandSender, TypeManager.toString(new Object[]{value}));
                    }
                }
            }
        } else {
            if (ctx instanceof PlayerContext playerContext) {
                Player commandSender = playerContext.getPlayer();
                for (Object value : messages) {
                    if (value instanceof String string) {
                        Utils.sendMessage(commandSender, string);
                    } else if (value instanceof Message message) {
                        commandSender.sendMessage(message);
                    } else {
                        Utils.sendMessage(commandSender, TypeManager.toString(new Object[]{value}));
                    }
                }
            } else {
                for (Object value : messages) {
                    if (value instanceof String s) {
                        Utils.log(s);
                    } else if (value instanceof Message message) {
                        Utils.log(message.getRawText());
                    } else {
                        Utils.log(TypeManager.toString(new Object[]{value}));
                    }
                }
            }
        }
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "send message[s] " + this.messages.toString(ctx, debug);
    }

}
