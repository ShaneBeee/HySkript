package com.github.skriptdev.skript.plugin.elements.events.player;

import com.github.skriptdev.skript.api.skript.event.CancellableContext;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.plugin.HySk;
import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.TriggerMap;
import io.github.syst3ms.skriptparser.lang.event.SkriptEvent;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

public class EvtPlayerChat extends SkriptEvent {

    public static void register(SkriptRegistration reg) {
        reg.newEvent(EvtPlayerChat.class, "[player] chat")
            .setHandledContexts(PlayerChatContext.class)
            .name("Player Chat")
            .description("Event triggered when a player sends a message in chat.",
                "The message can be changed by setting `context-message`.",
                "The message format can be changed by setting `context-message-format` (to a Message) or `context-format` (to a String).")
            .examples("on player chat:",
                "\tif name of context-sender = \"bob\":",
                "\t\tcancel event",
                "\t\tsend \"You said: %message% and we cancelled that!!!\" to context-sender",
                "",
                "on player chat:",
                "\tset {_message} to formatted message from \"[<blue>MyServer<reset>]" +
                    " <red>%name of context-playerref%<reset>: %context-message%\"",
                "\tset context-message-format to {_message}")
            .since("1.0.0")
            .register();

        reg.newSingleContextValue(PlayerChatContext.class, String.class,
                "message", PlayerChatContext::getMessage)
            .addSetter(PlayerChatContext::setMessage)
            .register();
        reg.newSingleContextValue(PlayerChatContext.class, Message.class,
                "message-format", PlayerChatContext::getMessageFormat)
            .addSetter(PlayerChatContext::setMessageFormat)
            .register();
        reg.newSingleContextValue(PlayerChatContext.class, String.class,
                "format", PlayerChatContext::getFormat)
            .addSetter(PlayerChatContext::setFormat)
            .register();
        reg.addSingleContextValue(PlayerChatContext.class, PlayerRef.class,
            "sender", PlayerChatContext::getSender);
        reg.addSingleContextValue(PlayerChatContext.class, PlayerRef.class,
            "playerref", PlayerChatContext::getSender);
    }

    private static EventRegistration<String, PlayerChatEvent> LISTENER;

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        if (LISTENER == null) {
            // Only register listener once
            LISTENER = HySk.getInstance().getEventRegistry().registerAsyncGlobal(PlayerChatEvent.class, future -> {
                future.thenAccept(event -> {
                    PlayerChatContext ctx = new PlayerChatContext(event);
                    TriggerMap.callTriggersByContext(ctx);
                });
                return future;
            });
        }
        return true;
    }

    @Override
    public boolean check(@NotNull TriggerContext ctx) {
        return ctx instanceof PlayerChatContext;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "player chat";
    }

    public record PlayerChatContext(PlayerChatEvent event) implements TriggerContext, CancellableContext {

        public PlayerChatEvent getEvent() {
            return this.event;
        }

        public String getMessage() {
            return this.event.getContent();
        }

        public void setMessage(String message) {
            this.event.setContent(message);
        }

        public String getFormat() {
            PlayerChatEvent.Formatter formatter = this.event.getFormatter();
            Message format = formatter.format(this.event.getSender(), this.event.getContent());
            return format.toString();
        }

        public Message getMessageFormat() {
            PlayerChatEvent.Formatter formatter = this.event.getFormatter();
            return formatter.format(this.event.getSender(), this.event.getContent());
        }

        public void setFormat(String format) {
            this.event.setFormatter(new PlayerChatEvent.Formatter() {
                @Override
                public @NotNull Message format(@NotNull PlayerRef playerRef, @NotNull String s) {
                    return Message.raw(format)
                        .param("message", s)
                        .param("player", playerRef.getUsername())
                        .param("username", playerRef.getUsername());
                }
            });
        }

        public void setMessageFormat(Message format) {
            this.event.setFormatter(new PlayerChatEvent.Formatter() {
                @Override
                public @NotNull Message format(@NotNull PlayerRef playerRef, @NotNull String s) {
                    return format
                        .param("message", s)
                        .param("player", playerRef.getUsername())
                        .param("username", playerRef.getUsername());
                }
            });
        }

        public PlayerRef getSender() {
            return this.event.getSender();
        }

        @Override
        public boolean isCancelled() {
            return this.event.isCancelled();
        }

        @Override
        public void setCancelled(boolean cancelled) {
            this.event.setCancelled(cancelled);
        }

        @Override
        public String getName() {
            return "player chat context";
        }
    }

}
