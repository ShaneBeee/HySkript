package com.github.skriptdev.skript.plugin.elements.effects.other;

import com.github.skriptdev.skript.api.skript.event.CancellableContext;
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
            .description("Cancels/uncancels the current event.",
                "**Reminder**: This effect cannot be used after a delay.")
            .examples("on chat:",
                "\tcancel event")
            .since("1.0.0")
            .register();
    }

    private boolean cancel;

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, ParseContext parseContext) {
        if (parseContext.getParserState().isDelayed()) {
            parseContext.getLogger().error("Cannot cancel an event after a delay.", ErrorType.SEMANTIC_ERROR);
            return false;
        }
        this.cancel = matchedPattern == 0;
        for (Class<? extends TriggerContext> currentContext : parseContext.getParserState().getCurrentContexts()) {
            if (CancellableContext.class.isAssignableFrom(currentContext)) {
                return true;
            }
        }
        parseContext.getLogger().error("This event cannot be cancelled", ErrorType.SEMANTIC_ERROR);
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
