package com.github.skriptdev.skript.plugin.elements.events;

import com.github.skriptdev.skript.api.skript.event.IEventContext;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.plugin.HySk;
import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.event.SkriptEvent;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

public class EvtPlayerChat extends SkriptEvent {

    public static void register(SkriptRegistration registration) {
        registration.newEvent(EvtPlayerChat.class, "[player] chat")
            .setHandledContexts(IEventContext.class)
            .name("Player Chat")
            .description("Event triggered when a player sends a message in chat.",
                "This event can be cancelled.")
            .examples("on player chat:",
                "\tif name of context-sender = \"bob\":",
                "\t\tcancel event",
                "\t\tsend \"You said: %message% and we cancelled that!!!\" to context-sender")
            .since("INSERT VERSION")
            .register();

        registration.addIEventContext(PlayerChatEvent.class, String.class, "message", PlayerChatEvent::getContent);
        registration.addIEventContext(PlayerChatEvent.class, PlayerRef.class, "sender", PlayerChatEvent::getSender);
        registration.addIEventContext(PlayerChatEvent.class, PlayerRef.class, "playerref", PlayerChatEvent::getSender);
    }

    private EventRegistration<String, PlayerChatEvent> chatListener;

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.chatListener = HySk.getInstance().getEventRegistry().registerAsyncGlobal(PlayerChatEvent.class, future -> {
            future.thenAccept(event -> {
                IEventContext<PlayerChatEvent> ctx = new IEventContext<>(event);
                for (Trigger trigger : this.getTriggers()) {
                    Statement.runAll(trigger, ctx);
                }
            });
            return future;
        });
        return true;
    }

    @Override
    public boolean check(@NotNull TriggerContext ctx) {
        return true;
    }

    @Override
    public void clearTrigger(String scriptName) {
        this.chatListener.unregister();
        super.clearTrigger(scriptName);
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "player chat";
    }

}
