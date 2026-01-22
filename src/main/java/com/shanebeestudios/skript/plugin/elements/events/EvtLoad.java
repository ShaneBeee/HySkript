package com.shanebeestudios.skript.plugin.elements.events;

import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.shanebeestudios.skript.api.skript.eventcontext.ScriptLoadContext;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SkriptEvent;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.Nullable;

public class EvtLoad extends SkriptEvent {

    public static void register(SkriptRegistration registration) {
        registration.newEvent(EvtLoad.class, "load[ing]")
            .setHandledContexts(ScriptLoadContext.class)
            .register();

        registration.addContextValue(ScriptLoadContext.class, CommandSender.class, true,"sender", ScriptLoadContext::getSender);
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

}
