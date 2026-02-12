package com.github.skriptdev.skript.plugin.elements.effects.server;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.server.core.HytaleServer;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

public class EffServerShutdown extends Effect {

    public static void register(SkriptRegistration reg) {
        reg.newEffect(EffServerShutdown.class, "shutdown server")
            .name("Shutdown Server")
            .description("Shuts down the server.")
            .since("INSERT VERSION")
            .register();
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        return true;
    }

    @Override
    protected void execute(@NotNull TriggerContext ctx) {
        if (HytaleServer.get().isShuttingDown()) {
            return;
        }
        HytaleServer.get().shutdownServer();
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "shutdown server";
    }

}
