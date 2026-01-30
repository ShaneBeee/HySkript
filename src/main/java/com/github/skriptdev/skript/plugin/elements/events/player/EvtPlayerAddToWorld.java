package com.github.skriptdev.skript.plugin.elements.events.player;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.plugin.HySk;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.TriggerMap;
import io.github.syst3ms.skriptparser.lang.event.SkriptEvent;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

public class EvtPlayerAddToWorld extends SkriptEvent {

    public static void register(SkriptRegistration reg) {
        reg.newEvent(EvtPlayerAddToWorld.class,
                "player added to world", "add player to world")
            .name("Player Add To World")
            .description("Called when a player joins a world.")
            .examples("on player added to world:",
                "\tsend \"Welcome to the world!\" to context-player")
            .since("INSERT VERSION")
            .register();

        reg.addContextValue(AddContext.class, World.class, true, "world", AddContext::getWorld);
        reg.addContextValue(AddContext.class, Player.class, true, "player", AddContext::getPlayer);
    }

    private static EventRegistration<String, AddPlayerToWorldEvent> LISTENER;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        if (LISTENER == null) {
            LISTENER = HySk.getInstance().getEventRegistry().registerGlobal(AddPlayerToWorldEvent.class, event -> {
                AddContext context = new AddContext(event);
                TriggerMap.callTriggersByContext(context);
            });
        }
        return true;
    }

    @Override
    public boolean check(TriggerContext triggerContext) {
        return triggerContext instanceof AddContext;
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "";
    }

    private record AddContext(AddPlayerToWorldEvent event) implements TriggerContext {

        public World[] getWorld() {
            return new World[]{this.event.getWorld()};
        }

        public Player[] getPlayer() {
            Holder<EntityStore> holder = this.event.getHolder();
            Player component = holder.getComponent(Player.getComponentType());
            return new Player[]{component};
        }

        @Override
        public String getName() {
            return "add player to world context";
        }
    }

}
