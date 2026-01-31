package com.github.skriptdev.skript.plugin.elements.events.player;

import com.github.skriptdev.skript.api.skript.event.CancellableContext;
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

        reg.addContextValue(MouseMoveContext.class, Player.class, true, "player", MouseMoveContext::getPlayer);
        reg.addContextValue(MouseMoveContext.class, Item.class, true, "item", MouseMoveContext::getItemInHand);
        reg.addContextValue(MouseMoveContext.class, Entity.class, true, "target-entity", MouseMoveContext::getTargetEntity);
        reg.addContextValue(MouseMoveContext.class, Vector3i.class, true, "target-block", MouseMoveContext::getTargetBlock);
        reg.addContextValue(MouseMoveContext.class, Vector2f.class, true, "screen-point", MouseMoveContext::getScreenPoint);
        reg.addContextValue(MouseMoveContext.class, MouseMotionEvent.class, true, "mouse-motion", MouseMoveContext::getMouseMotion);
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

    private record MouseMoveContext(PlayerMouseMotionEvent event) implements TriggerContext, CancellableContext {

        private Player[] getPlayer() {
            return new Player[]{this.event.getPlayer()};
        }

        private Item[] getItemInHand() {
            return new Item[]{this.event.getItemInHand()};
        }

        private Entity[] getTargetEntity() {
            return new Entity[]{this.event.getTargetEntity()};
        }

        private Vector3i[] getTargetBlock() {
            return new Vector3i[]{this.event.getTargetBlock()};
        }

        private Vector2f[] getScreenPoint() {
            return new Vector2f[]{this.event.getScreenPoint()};
        }

        private MouseMotionEvent[] getMouseMotion() {
            return new MouseMotionEvent[]{this.event.getMouseMotion()};
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
