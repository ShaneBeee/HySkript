package com.github.skriptdev.skript.plugin.elements.events.server;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.plugin.HySk;
import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.server.core.event.events.ShutdownEvent;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.TriggerMap;
import io.github.syst3ms.skriptparser.lang.event.SkriptEvent;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

public class EvtShutdown extends SkriptEvent {

    public static void register(SkriptRegistration reg) {
        reg.newEvent(EvtShutdown.class, "shutdown", "server shutdown")
            .name("Server Shutdown")
            .description("Called when the server is shutting down.")
            .since("INSERT VERSION")
            .register();
    }

    private static EventRegistration<Void, ShutdownEvent> LISTENER;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        if (LISTENER == null) {
            LISTENER = HySk.getInstance().getEventRegistry().registerGlobal(ShutdownEvent.class, event -> {
                ShutdownContext shutdownContext = new ShutdownContext(event);
                TriggerMap.callTriggersByContext(shutdownContext);
            });
        }
        return true;
    }

    @Override
    public boolean check(TriggerContext triggerContext) {
        return triggerContext instanceof ShutdownContext;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "server shutdown event";
    }

    private record ShutdownContext(ShutdownEvent event) implements TriggerContext {
        @Override
        public String getName() {
            return "shutdown context";
        }
    }

}
