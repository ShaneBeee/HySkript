package com.github.skriptdev.skript.plugin.elements.command;

import com.github.skriptdev.skript.api.command.ArgUtils;
import com.github.skriptdev.skript.api.command.CommandArg;
import com.github.skriptdev.skript.plugin.HySk;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.arguments.system.Argument;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SkriptEvent;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.entries.SectionConfiguration;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.registration.context.ContextValue.Usage;
import io.github.syst3ms.skriptparser.variables.Variables;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ScriptCommand extends SkriptEvent {

    public static class ScriptCommandContext implements TriggerContext {

        private final String command;
        private final CommandSender sender;
        private final Player player;
        private final World world;

        public ScriptCommandContext(String command, CommandSender sender, Player player, World world) {
            this.command = command;
            this.sender = sender;
            this.player = player;
            this.world = world;
        }

        public String getCommand() {
            return this.command;
        }

        public CommandSender[] getSender() {
            return new CommandSender[]{this.sender};
        }

        public World[] getWorld() {
            return new World[]{this.world};
        }

        public Player[] getPlayer() {
            if (this.player == null && this.sender instanceof Player p) return new Player[]{p};
            return new Player[]{this.player};
        }

        @Override
        public String getName() {
            return "command context";
        }
    }

    public static void register(SkriptRegistration registration) {
        ArgUtils.init();
        registration.newEvent(ScriptCommand.class,
                "*[global] command <.+>",
                "*player command <.+>",
                "*world command <.+>")
            .setHandledContexts(ScriptCommandContext.class)
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
                "- `Description` = The description for your command that will show in the commands gui (optional).",
                "- `Permission` = The permission required to execute the command (optional).",
                "- `Aliases` = A list of aliases for the command (optional).")
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
            .since("INSERT VERSION")
            .register();

        registration.newContextValue(ScriptCommandContext.class, Player.class, true,
                "player", ScriptCommandContext::getPlayer)
            .setUsage(Usage.EXPRESSION_OR_ALONE)
            .register();

        registration.newContextValue(ScriptCommandContext.class, CommandSender.class, true,
                "sender", ScriptCommandContext::getSender)
            .setUsage(Usage.EXPRESSION_OR_ALONE)
            .register();

        registration.newContextValue(ScriptCommandContext.class, World.class, true,
                "world", ScriptCommandContext::getWorld)
            .setUsage(Usage.EXPRESSION_OR_ALONE)
            .register();

        registration.newContextValue(ScriptCommandContext.class, String.class, true,
                "command", ct -> new String[]{ct.getCommand()})
            .setUsage(Usage.EXPRESSION_OR_ALONE)
            .register();

    }

    private final SectionConfiguration sec = new SectionConfiguration.Builder()
        .addOptionalKey("permission")
        .addOptionalKey("description")
        .addOptionalList("aliases")
        .addSection("trigger")
        .build();

    private String command;
    private int commandType;
    private final Map<String, CommandArg> args = new LinkedHashMap<>();
    private final Map<String, Argument<?, ?>> argsFromCommand = new LinkedHashMap<>();

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, ParseContext parseContext) {
        String commandLine = parseContext.getMatches().getFirst().group();
        if (commandLine.startsWith("/")) {
            commandLine = commandLine.substring(1);
        }
        if (commandLine.contains(" ")) {
            String[] commandLineSplit = commandLine.split(" ", 2);
            this.command = commandLineSplit[0];

            String[] argSplit = commandLineSplit[1].split("(?<=[>\\]])\\s+(?=[<\\[])");
            for (String s : argSplit) {
                CommandArg arg = CommandArg.parseArg(s);
                if (arg == null) {
                    parseContext.getLogger().error("Invalid argument format: '" + s + "'", ErrorType.SEMANTIC_ERROR);
                    return false;
                }
                setupArg(arg);
            }
        }
        if (this.command.isEmpty()) {
            parseContext.getLogger().error("Command cannot be empty", ErrorType.SEMANTIC_ERROR);
            return false;
        }
        this.commandType = matchedPattern;
        return true;
    }

    @Override
    public List<Statement> loadSection(@NotNull FileSection section, @NotNull ParserState parserState, @NotNull SkriptLogger logger) {
        this.sec.loadConfiguration(null, section, parserState, logger);
        Optional<CodeSection> triggerSec = this.sec.getSection("trigger");
        if (triggerSec.isEmpty()) {
            logger.error("Trigger section is missing", ErrorType.SEMANTIC_ERROR);
            return List.of();
        }

        CodeSection trigger = triggerSec.get();
        if (trigger.getItems().isEmpty()) {
            logger.warn("Trigger section should not be empty.");
            return List.of();
        }

        Optional<String> descOption = this.sec.getValue("description", String.class);
        if (descOption.isEmpty()) {
            descOption = Optional.of("");
        }

        String description = trim(descOption.get());
        if (description.isEmpty()) {
            description = "";
        }

        AbstractCommand hyCommand = switch (this.commandType) {
            case 1 -> new AbstractPlayerCommand(this.command, description) {
                @Override
                protected void execute(@NotNull CommandContext commandContext, @NotNull Store<EntityStore> store,
                                       @NotNull Ref<EntityStore> ref, @NotNull PlayerRef playerRef, @NotNull World world) {

                    CommandSender sender = commandContext.sender();
                    Player player = store.getComponent(ref, Player.getComponentType());
                    ScriptCommandContext context = new ScriptCommandContext(ScriptCommand.this.command, sender, player, world);
                    createLocalVariables(commandContext, context);
                    Statement.runAll(trigger, context);
                    Variables.clearLocalVariables(context);
                }
            };
            case 2 -> new AbstractWorldCommand(this.command, description) {

                @Override
                protected void execute(@NotNull CommandContext commandContext, @NotNull World world, @NotNull Store<EntityStore> store) {
                    ScriptCommandContext context = new ScriptCommandContext(ScriptCommand.this.command, commandContext.sender(), null, world);
                    createLocalVariables(commandContext, context);
                    Statement.runAll(trigger, context);
                    Variables.clearLocalVariables(context);
                }
            };
            default -> new AbstractCommand(this.command, description) {

                @Override
                protected @Nullable CompletableFuture<Void> execute(@NotNull CommandContext commandContext) {
                    CompletableFuture.runAsync(() -> {
                        CommandSender sender = commandContext.sender();
                        Player player = null;
                        if (sender instanceof Player p) player = p;
                        ScriptCommandContext context = new ScriptCommandContext(ScriptCommand.this.command, sender, player, null);

                        createLocalVariables(commandContext, context);
                        Statement.runAll(trigger, context);
                        Variables.clearLocalVariables(context);
                    });
                    return null;
                }
            };
        };
        this.args.forEach((key, arg) -> {
            if (arg.isOptional()) {
                OptionalArg<?> optionalArg = hyCommand.withOptionalArg(key, arg.getDescription(), arg.getType());
                this.argsFromCommand.put(key, optionalArg);
            } else {
                RequiredArg<?> requiredArg = hyCommand.withRequiredArg(key, arg.getDescription(), arg.getType());
                this.argsFromCommand.put(key, requiredArg);
            }
        });
        Optional<String> permValue = this.sec.getValue("permission", String.class);
        if (permValue.isPresent()) {
            String perm = trim(permValue.get());
            if (!perm.isEmpty()) {
                hyCommand.requirePermission(perm);
            } else {
                logger.warn("Permission is empty, will fallback to default permission.");
            }
        }
        Optional<String[]> aliases = this.sec.getStringList("aliases");
        if (aliases.isPresent()) {
            for (String alias : aliases.get()) {
                hyCommand.addAliases(trim(alias));
            }
        }
        HySk.getInstance().getCommandRegistry().registerCommand(hyCommand);

        return List.of(trigger);
    }

    private String trim(String s) {
        // In case someone puts quotes, let's remove them
        if (s.startsWith("\"")) {
            s = s.substring(1);
        }
        if (s.endsWith("\"")) {
            s = s.substring(0, s.length() - 1);
        }
        return s.trim();
    }

    @Override
    public boolean check(@NotNull TriggerContext ctx) {
        return ctx instanceof ScriptCommandContext sctx && sctx.getCommand().equals(this.command);
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String type = switch (this.commandType) {
            case 1 -> "player";
            case 2 -> "world";
            default -> "global";
        };
        return type + " command /" + this.command;
    }

    private void setupArg(CommandArg arg) {
        String name = arg.getName();
        if (this.args.containsKey(name)) {
            for (int i = 1; i < 10; i++) {
                String newName = name + (i + 1);
                if (!this.args.containsKey(newName)) {
                    this.args.put(newName, arg);
                    return;
                }
            }
        } else {
            this.args.put(name, arg);
        }
    }

    private void createLocalVariables(CommandContext ctx, TriggerContext triggerContext) {
        this.argsFromCommand.forEach((name, arg) -> {
            Object o = ctx.get(arg);
            if (o != null) Variables.setVariable(name, o, triggerContext, true);
        });
    }

}
