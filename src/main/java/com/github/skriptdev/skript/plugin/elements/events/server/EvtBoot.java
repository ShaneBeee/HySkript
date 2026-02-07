package com.github.skriptdev.skript.plugin.elements.events.server;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.plugin.HySk;
import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.server.core.event.events.BootEvent;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.TriggerMap;
import io.github.syst3ms.skriptparser.lang.event.SkriptEvent;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

public class EvtBoot extends SkriptEvent {

    public static void register(SkriptRegistration reg) {
        reg.newEvent(EvtBoot.class, "boot", "server boot")
            .name("Server Boot")
            .description("Called when the server is starting up.")
            .since("1.0.0")
            .register();
    }

    private static EventRegistration<Void, BootEvent> LISTENER;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        if (LISTENER == null) {
            LISTENER = HySk.getInstance().getEventRegistry().registerGlobal(BootEvent.class, event -> {
                BootContext bootContext = new BootContext(event);
                TriggerMap.callTriggersByContext(bootContext);
            });
        }
        return true;
    }

    @Override
    public boolean check(TriggerContext triggerContext) {
        return triggerContext instanceof BootContext;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "server boot event";
    }

    private record BootContext(BootEvent event) implements TriggerContext {
        @Override
        public String getName() {
            return "boot context";
        }
    }

}
