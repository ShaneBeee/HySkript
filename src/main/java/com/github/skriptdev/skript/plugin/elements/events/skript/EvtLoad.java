package com.github.skriptdev.skript.plugin.elements.events.skript;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.TriggerMap;
import io.github.syst3ms.skriptparser.lang.event.SkriptEvent;
import io.github.syst3ms.skriptparser.lang.event.StartOnLoadEvent;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.Nullable;

public class EvtLoad extends SkriptEvent implements StartOnLoadEvent {

    public static void register(SkriptRegistration reg) {
        reg.newEvent(EvtLoad.class, "[script] load[ing]")
            .name("Script Loading")
            .description("Triggered when a script is loaded.")
            .since("1.0.0")
            .setHandledContexts(ScriptLoadContext.class)
            .register();
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        return true;
    }

    @Override
    public boolean check(TriggerContext ctx) {
        return ctx instanceof ScriptLoadContext;
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "script loading";
    }

    @Override
    public void onInitialLoad(Trigger trigger) {
        TriggerMap.callTriggersByContext(new ScriptLoadContext());
    }

    private static class ScriptLoadContext implements TriggerContext {
        @Override
        public String getName() {
            return "script load context";
        }
    }

}
