package com.github.skriptdev.skript.plugin.elements.events.player;

import com.github.skriptdev.skript.api.skript.event.CancellableContext;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.plugin.HySk;
import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.TriggerMap;
import io.github.syst3ms.skriptparser.lang.event.SkriptEvent;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

public class EvtPlayerChat extends SkriptEvent {

    public static void register(SkriptRegistration registration) {
        registration.newEvent(EvtPlayerChat.class, "[player] chat")
            .setHandledContexts(PlayerChatContext.class)
            .name("Player Chat")
            .description("Event triggered when a player sends a message in chat.")
            .examples("on player chat:",
                "\tif name of context-sender = \"bob\":",
                "\t\tcancel event",
                "\t\tsend \"You said: %message% and we cancelled that!!!\" to context-sender")
            .since("INSERT VERSION")
            .register();

        registration.addContextValue(PlayerChatContext.class, String.class,
            true, "message", PlayerChatContext::getMessage);
        registration.addContextValue(PlayerChatContext.class, PlayerRef.class,
            true, "sender", PlayerChatContext::getSender);
        registration.addContextValue(PlayerChatContext.class, PlayerRef.class,
            true, "playerref", PlayerChatContext::getSender);
    }

    private static EventRegistration<String, PlayerChatEvent> LISTENER;

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        if (LISTENER == null) {
            // Only register listener once
            LISTENER = HySk.getInstance().getEventRegistry().registerAsyncGlobal(PlayerChatEvent.class, future -> {
                future.thenAccept(event -> {
                    PlayerChatContext ctx = new PlayerChatContext(event);
                    for (Trigger trigger : TriggerMap.getTriggersByContext(PlayerChatContext.class)) {
                        Statement.runAll(trigger, ctx);
                    }
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

        public String[] getMessage() {
            return new String[]{this.event.getContent()};
        }

        public PlayerRef[] getSender() {
            return new PlayerRef[]{this.event.getSender()};
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
