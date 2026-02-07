package com.github.skriptdev.skript.plugin.elements.events.entity;

import com.github.skriptdev.skript.api.skript.event.WorldContext;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.plugin.HySk;
import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.event.events.entity.EntityRemoveEvent;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.TriggerMap;
import io.github.syst3ms.skriptparser.lang.event.SkriptEvent;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

public class EvtEntityRemove extends SkriptEvent {

    public static void register(SkriptRegistration reg) {
        reg.newEvent(EvtEntityRemove.class,
                "entity remove", "entity removed", "entity removal")
            .name("Entity Remove")
            .description("Called when an entity is removed from the world.")
            .since("1.0.0")
            .setHandledContexts(EntityRemoveEventContext.class)
            .register();
        reg.addSingleContextValue(EntityRemoveEventContext.class, Entity.class, "entity", EntityRemoveEventContext::getEntity);
    }

    private static EventRegistration<String, EntityRemoveEvent> LISTENER;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        if (LISTENER == null) {
            LISTENER = HySk.getInstance().getEventRegistry().registerGlobal(EntityRemoveEvent.class, event -> {
                EntityRemoveEventContext context = new EntityRemoveEventContext(event);
                TriggerMap.callTriggersByContext(context);
            });
        }
        return true;
    }

    @Override
    public boolean check(TriggerContext triggerContext) {
        return triggerContext instanceof EntityRemoveEventContext;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "entity remove event";
    }

    private record EntityRemoveEventContext(EntityRemoveEvent event) implements TriggerContext, WorldContext {
        @Override
        public String getName() {
            return "entity remove context";
        }

        private Entity getEntity() {
            return this.event.getEntity();
        }

        @Override
        public World getWorld() {
            return getEntity().getWorld();
        }
    }

}
