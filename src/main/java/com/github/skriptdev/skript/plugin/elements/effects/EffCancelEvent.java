package com.github.skriptdev.skript.plugin.elements.effects;

import com.github.skriptdev.skript.api.skript.eventcontext.CancellableContext;
import com.github.skriptdev.skript.plugin.Skript;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.NotNull;

public class EffCancelEvent extends Effect {

    public static void register(SkriptRegistration registration) {
        registration.newEffect(EffCancelEvent.class, "cancel event", "uncancel event")
            .name("Cancel Event")
            .description("Cancels/uncancels the current event.")
            .examples("on chat:",
                "\tcancel event")
            .since("INSERT VERSION")
            .register();
    }

    private boolean cancel;

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, ParseContext parseContext) {
        this.cancel = matchedPattern == 0;
        for (Class<? extends TriggerContext> currentContext : parseContext.getParserState().getCurrentContexts()) {
            if (!CancellableContext.class.isAssignableFrom(currentContext)) {
                Skript.error("This event cannot be cancelled");
                return false;
            }
        }
        return true;
    }

    @Override
    protected void execute(@NotNull TriggerContext ctx) {
        if (ctx instanceof CancellableContext cancellableContext) cancellableContext.setCancelled(this.cancel);
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return (this.cancel ? "cancel" : "uncancel") + " event";
    }

}
