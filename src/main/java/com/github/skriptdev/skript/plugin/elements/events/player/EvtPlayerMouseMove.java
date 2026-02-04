package com.github.skriptdev.skript.plugin.elements.events.player;

import com.github.skriptdev.skript.api.skript.event.CancellableContext;
import com.github.skriptdev.skript.api.skript.event.PlayerContext;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.plugin.HySk;
import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.MouseMotionEvent;
import com.hypixel.hytale.protocol.Vector2f;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerMouseMotionEvent;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.TriggerMap;
import io.github.syst3ms.skriptparser.lang.event.SkriptEvent;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

public class EvtPlayerMouseMove extends SkriptEvent {

    public static void register(SkriptRegistration reg) {
        reg.newEvent(EvtPlayerMouseClick.class, "player mouse motion", "player mouse move")
            .name("Player Mouse Motion")
            .description("Called when a player moves their mouse.",
                "**NOTE**: This event appears to be broken internally and doesn't seem to call")
            .since("1.0.0")
            .setHandledContexts(MouseMoveContext.class)
            .register();

        reg.addSingleContextValue(MouseMoveContext.class, Item.class, "item", MouseMoveContext::getItemInHand);
        reg.addSingleContextValue(MouseMoveContext.class, Entity.class, "target-entity", MouseMoveContext::getTargetEntity);
        reg.addSingleContextValue(MouseMoveContext.class, Vector3i.class, "target-block", MouseMoveContext::getTargetBlock);
        reg.addSingleContextValue(MouseMoveContext.class, Vector2f.class, "screen-point", MouseMoveContext::getScreenPoint);
        //reg.addSingleContextValue(MouseMoveContext.class, MouseMotionEvent.class, "mouse-motion", MouseMoveContext::getMouseMotion);
    }

    private static EventRegistration<Void, PlayerMouseMotionEvent> LISTENER;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        if (LISTENER == null) {
            LISTENER = HySk.getInstance().getEventRegistry().registerGlobal(PlayerMouseMotionEvent.class, event -> {
                MouseMoveContext context = new MouseMoveContext(event);
                TriggerMap.callTriggersByContext(context);
            });
        }
        return true;
    }

    @Override
    public boolean check(TriggerContext triggerContext) {
        return triggerContext instanceof MouseMoveContext;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "player mouse motion event";
    }

    private record MouseMoveContext(PlayerMouseMotionEvent event) implements PlayerContext, CancellableContext {

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

        private MouseMotionEvent getMouseMotion() {
            return this.event.getMouseMotion();
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
            return "mouse motion context";
        }
    }

}
