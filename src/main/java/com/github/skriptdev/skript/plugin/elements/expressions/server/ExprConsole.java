package com.github.skriptdev.skript.plugin.elements.expressions.server;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

public class ExprConsole implements Expression<CommandSender> {

    public static void register(SkriptRegistration registration) {
        registration.newExpression(ExprConsole.class, CommandSender.class, false, "console")
            .name("Console")
            .description("Returns the console (as a CommandSender).")
            .examples("send \"Hello Mr Console!\" to console")
            .since("1.0.0")
            .register();

    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        return true;
    }

    @Override
    public CommandSender[] getValues(TriggerContext ctx) {
        return new CommandSender[]{ConsoleSender.INSTANCE};
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "console";
    }

}
