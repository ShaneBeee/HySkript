package com.github.skriptdev.skript.plugin.elements.command;

import com.github.skriptdev.skript.api.skript.command.ScriptCommandBuilder;
import com.github.skriptdev.skript.api.skript.command.ScriptCommandParent;
import com.github.skriptdev.skript.api.skript.event.PlayerContext;
import com.github.skriptdev.skript.api.skript.event.WorldContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.Structure;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.registration.context.ContextValue.Usage;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ScriptCommand extends Structure implements ScriptCommandParent {

    @Override
    public void loadChild(ScriptCommandBuilder parent, SkriptLogger logger) {
        // UNUSED IN TOP LEVEL COMMAND
    }

    public static class ScriptCommandContext implements TriggerContext {

        private final String command;
        private final CommandSender sender;

        public ScriptCommandContext(String command, CommandSender sender) {
            this.command = command;
            this.sender = sender;
        }

        public String getCommand() {
            return this.command;
        }

        public CommandSender getSender() {
            return this.sender;
        }

        @Override
        public String getName() {
            return "command context";
        }
    }

    public static class PlayerScriptCommandContext extends ScriptCommandContext implements PlayerContext, WorldContext {

        private final Player player;

        public PlayerScriptCommandContext(String command, Player player) {
            super(command, player);
            this.player = player;
        }

        @Override
        public Player getPlayer() {
            return this.player;
        }

        @Override
        public World getWorld() {
            return this.player.getWorld();
        }

    }

    public static class WorldScriptCommandContext extends ScriptCommandContext implements WorldContext {

        private final World world;

        public WorldScriptCommandContext(String command, CommandSender sender, World world) {
            super(command, sender);
            this.world = world;
        }

        @Override
        public World getWorld() {
            return this.world;
        }
    }

    public static void register(SkriptRegistration reg) {
        reg.newStructure(ScriptCommand.class,
                "*[global] command <.+>",
                "*player command <.+>",
                "*world command <.+>")
            .setHandledContexts(ScriptCommandContext.class,
                PlayerScriptCommandContext.class,
                WorldScriptCommandContext.class)
            .name("Command")
            .description("Create a command.",
                "**Command Format**:",
                "- `<global/player/world> command /command_name (args)`",
                "",
                "**Argument Formats**:",
                "- `<type>`",
                "- `<name:type>`",
                "- `<type:\"description\">`",
                "- `<name:type:\"description\">`",
                "- <> = Makes the argument required.",
                "- [<>] = Makes the argument optional.",
                "- Type = The type of argument to use (required).",
                "- Name = The name of the argument, this will be used to create local variables (optional).",
                "- Description = The description of the argument, this is show in the command GUI (optional).",
                "",
                "**Entries**:",
                "- `description` = The description for your command that will show in the commands gui (optional).",
                "- `permission` = The permission required to execute the command (optional).",
                "- `can-generate-permission` = Whether the command should generate a permission based on the command name " +
                    "(optional, default true).",
                "- `aliases` = A list of aliases for the command (optional).",
                "- `trigger` = The code that will be executed when the command is executed (optional).")
            .examples("command /kill:",
                "\tdescription: Kill all the players",
                "\ttrigger:",
                "\t\tkill all players",
                "",
                "command /home [<name:string:\"Name of home\">]:",
                "\ttrigger:",
                "\t\tif {_name} is set:",
                "\t\t\tteleport player to {homes::%{_name}%}",
                "\t\telse:",
                "\t\t\tteleport player to {homes::default}",
                "",
                "command /broadcast <message:string:\"Message to broadcast\">:",
                "\ttrigger:",
                "\t\tbroadcast {_message}",
                "",
                "player command /clear:",
                "\tpermission: my.script.command.clear",
                "\tdescription: Clear your inventory",
                "\ttrigger:",
                "\t\tclear inventory of player",
                "\t\tsend \"Your inventory has been cleared\" to player",
                "",
                "world command /spawn:",
                "\tdescription: Will teleport all players to the world spawn",
                "\ttrigger:",
                "\t\tteleport all players to spawn location of context-world")
            .since("1.0.0")
            .register();

        reg.newSingleContextValue(ScriptCommandContext.class, CommandSender.class,
                "sender", ScriptCommandContext::getSender)
            .setUsage(Usage.EXPRESSION_OR_ALONE)
            .register();
        reg.newSingleContextValue(ScriptCommandContext.class, String.class,
                "command", ScriptCommandContext::getCommand)
            .setUsage(Usage.EXPRESSION_OR_ALONE)
            .register();
    }

    private ScriptCommandBuilder commandBuilder;

    private int commandType;

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, ParseContext parseContext) {
        this.commandType = matchedPattern;
        this.commandBuilder = ScriptCommandBuilder.create(matchedPattern, parseContext.getLogger());
        return this.commandBuilder.parseCommandLine(parseContext.getMatches().getFirst().group());
    }

    @Override
    public List<Statement> loadSection(@NotNull FileSection section, @NotNull ParserState parserState, @NotNull SkriptLogger logger) {
        List<Statement> statements = this.commandBuilder.setupCommand(section, parserState, logger, this.commandType);
        this.commandBuilder.build(null);
        return statements;
    }


    @Override
    public boolean check(@NotNull TriggerContext ctx) {
        return ctx instanceof ScriptCommandContext sctx && sctx.getCommand().equals(this.commandBuilder.getCommandName());
    }

    @Override
    public void unload() {
        this.commandBuilder.unregister();
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String type = switch (this.commandType) {
            case 1 -> "player";
            case 2 -> "world";
            default -> "global";
        };
        return type + " command /" + this.commandBuilder.getCommandName();
    }

}
