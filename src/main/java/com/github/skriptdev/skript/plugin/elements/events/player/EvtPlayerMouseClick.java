package com.github.skriptdev.skript.plugin.elements.events.player;

import com.github.skriptdev.skript.api.skript.event.CancellableContext;
import com.github.skriptdev.skript.api.skript.event.PlayerContext;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.plugin.HySk;
import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.MouseButtonEvent;
import com.hypixel.hytale.protocol.Vector2f;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerMouseButtonEvent;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.TriggerMap;
import io.github.syst3ms.skriptparser.lang.event.SkriptEvent;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

public class EvtPlayerMouseClick extends SkriptEvent {

    public static void register(SkriptRegistration reg) {
        reg.newEvent(EvtPlayerMouseClick.class, "player mouse button", "player mouse click")
            .name("Player Mouse Button")
            .description("Called when a player clicks on their mouse.",
                "**NOTE**: This event appears to be broken internally and doesn't seem to call")
            .since("1.0.0")
            .setHandledContexts(MouseClickContext.class)
            .register();

        reg.addSingleContextValue(MouseClickContext.class, Item.class, "item", MouseClickContext::getItemInHand);
        reg.addSingleContextValue(MouseClickContext.class, Entity.class, "target-entity", MouseClickContext::getTargetEntity);
        reg.addSingleContextValue(MouseClickContext.class, Vector3i.class, "target-block", MouseClickContext::getTargetBlock);
        reg.addSingleContextValue(MouseClickContext.class, Vector2f.class, "screen-point", MouseClickContext::getScreenPoint);
        //reg.addSingleContextValue(MouseClickContext.class, MouseButtonEvent.class, "mouse-button", MouseClickContext::getMouseButton);
    }

    private static EventRegistration<Void, PlayerMouseButtonEvent> LISTENER;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        if (LISTENER == null) {
            LISTENER = HySk.getInstance().getEventRegistry().registerGlobal(PlayerMouseButtonEvent.class, event -> {
                MouseClickContext context = new MouseClickContext(event);
                TriggerMap.callTriggersByContext(context);
            });
        }
        return true;
    }

    @Override
    public boolean check(TriggerContext triggerContext) {
        return triggerContext instanceof MouseClickContext;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "player mouse click";
    }

    private record MouseClickContext(PlayerMouseButtonEvent event) implements PlayerContext, CancellableContext {

        public Player getPlayer() {
            return this.event.getPlayer();
        }

        private Item getItemInHand() {
            return this.event.getItemInHand();
        }

        private Entity getTargetEntity() {
            return this.event.getTargetEntity();
        }

        private Vector3i getTargetBlock() {
            return this.event.getTargetBlock();
        }

        private Vector2f getScreenPoint() {
            return this.event.getScreenPoint();
        }

        private MouseButtonEvent getMouseButton() {
            return this.event.getMouseButton();
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
            return "mouse click context";
        }
    }

}
