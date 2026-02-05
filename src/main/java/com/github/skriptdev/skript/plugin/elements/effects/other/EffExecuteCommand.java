package com.github.skriptdev.skript.plugin.elements.effects.other;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

public class EffExecuteCommand extends Effect {

    public static void register(SkriptRegistration reg) {
        reg.newEffect(EffExecuteCommand.class, "execute %commandsenders% command %string%")
            .name("Execute Command")
            .description("Execute a command as a player or console.")
            .examples("execute player command \"/inv clear\"",
                "execute console command \"/stop\"")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<CommandSender> commandSenders;
    private Expression<String> command;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.commandSenders = (Expression<CommandSender>) expressions[0];
        this.command = (Expression<String>) expressions[1];
        return true;
    }

    @Override
    protected void execute(@NotNull TriggerContext ctx) {
        String command = this.command.getSingle(ctx).orElse(null);
        if (command == null) return;

        if (command.charAt(0) == '/') command = command.substring(1);

        CommandManager commandManager = CommandManager.get();
        for (CommandSender sender : commandSenders.getArray(ctx)) {
            commandManager.handleCommand(sender, command);
        }

    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return String.format("execute %s command %s",
            this.commandSenders.toString(ctx, debug),
            this.command.toString(ctx, debug));
    }

}
