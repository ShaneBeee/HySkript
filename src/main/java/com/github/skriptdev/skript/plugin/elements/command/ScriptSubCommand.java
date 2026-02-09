package com.github.skriptdev.skript.plugin.elements.command;

import com.github.skriptdev.skript.api.skript.command.ScriptCommandBuilder;
import com.github.skriptdev.skript.api.skript.command.ScriptCommandParent;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ScriptSubCommand extends CodeSection implements ScriptCommandParent {

    public static void register(SkriptRegistration registration) {
        registration.newSection(ScriptSubCommand.class, "sub command <.+>")
            .name("Sub Command")
            .description("Creates a sub command for a top level command or another sub command.",
                "Sub commands have the same entries/trigger as top level commands.")
            .since("1.0.0")
            .register();
    }

    private ScriptCommandBuilder commandBuilder;
    private String commandLine;
    private FileSection section;
    private ParserState parserState;

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        for (Class<? extends TriggerContext> currentSection : parseContext.getParserState().getCurrentContexts()) {
            if (!ScriptCommand.ScriptCommandContext.class.isAssignableFrom(currentSection)) {
                parseContext.getLogger().error("Sub commands can only be used within commands/sub commands.", ErrorType.STRUCTURE_ERROR);
                return false;
            }
        }
        this.commandLine = parseContext.getMatches().getFirst().group();
        this.commandBuilder = ScriptCommandBuilder.create(-1, parseContext.getLogger());
        return this.commandBuilder.parseCommandLine(this.commandLine);
    }

    @Override
    public void loadChild(ScriptCommandBuilder parent, SkriptLogger logger) {
        this.commandBuilder.setupCommand(this.section, this.parserState, logger, parent.getCommandType());
        this.commandBuilder.build(parent);
    }

    @Override
    public boolean loadSection(@NotNull FileSection section, @NotNull ParserState parserState, @NotNull SkriptLogger logger) {
        this.section = section;
        this.parserState = parserState;
        return true;
    }

    @Override
    public Optional<? extends Statement> walk(@NotNull TriggerContext ctx) {
        return Optional.empty();
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "sub command + " + this.commandLine;
    }
}
