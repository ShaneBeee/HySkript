package com.github.skriptdev.skript.plugin.elements.listeners;

import com.github.skriptdev.skript.plugin.elements.events.EvtPlayerChat;
import com.github.skriptdev.skript.plugin.elements.events.EvtPlayerChat.PlayerChatEventContext;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.Trigger;

import java.util.ArrayList;
import java.util.List;

public class PlayerListener {

    private final List<Trigger> chatTriggers = new ArrayList<>();

    public PlayerListener(EventRegistry registry) {
        registry.registerAsyncGlobal(PlayerChatEvent.class, future -> {
            future.thenAccept(event -> {
                PlayerChatEventContext ctx = new PlayerChatEventContext(event.getContent());
                for (Trigger trigger : this.chatTriggers) {
                    Statement.runAll(trigger, ctx);
                }
                if (ctx.isMessageChanged()) event.setContent(ctx.getMessage()[0]);
                event.setCancelled(ctx.isCancelled());
            });
            return future;
        });
    }

    public void clearTriggers() {
        this.chatTriggers.clear();
    }

    public void handleTrigger(Trigger trigger) {
        if (trigger.getEvent() instanceof EvtPlayerChat) {
            this.chatTriggers.add(trigger);
        }
    }

}
