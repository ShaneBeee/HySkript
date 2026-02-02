package com.github.skriptdev.skript.plugin.elements.effects.other;

import com.github.skriptdev.skript.api.skript.event.CancellableContext;
import com.github.skriptdev.skript.api.utils.Utils;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.ErrorType;
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
            .since("1.0.0")
            .register();
    }

    private boolean cancel;

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, ParseContext parseContext) {
        this.cancel = matchedPattern == 0;
        Utils.log("Contexts:");
        for (Class<? extends TriggerContext> currentContext : parseContext.getParserState().getCurrentContexts()) {
            Utils.log(" - %s", currentContext.getSimpleName());
        }
        for (Class<? extends TriggerContext> currentContext : parseContext.getParserState().getCurrentContexts()) {
            if (!CancellableContext.class.isAssignableFrom(currentContext)) {
                parseContext.getLogger().error("This event cannot be cancelled", ErrorType.SEMANTIC_ERROR);
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
