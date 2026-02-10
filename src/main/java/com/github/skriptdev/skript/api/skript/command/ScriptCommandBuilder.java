package com.github.skriptdev.skript.api.skript.command;

import com.github.skriptdev.skript.plugin.HySk;
import com.github.skriptdev.skript.plugin.Skript;
import com.github.skriptdev.skript.plugin.elements.command.ScriptCommand.PlayerScriptCommandContext;
import com.github.skriptdev.skript.plugin.elements.command.ScriptCommand.ScriptCommandContext;
import com.github.skriptdev.skript.plugin.elements.command.ScriptCommand.WorldScriptCommandContext;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandRegistration;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.arguments.system.Argument;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.entries.SectionConfiguration;
import io.github.syst3ms.skriptparser.lang.entries.SectionLoader;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import io.github.syst3ms.skriptparser.parsing.SyntaxParser;
import io.github.syst3ms.skriptparser.variables.Variables;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Builder for Script Commands.
 */
public class ScriptCommandBuilder {

    private static final boolean CAN_GENERATE_PERMISSION_DEFAULT = Skript.getInstance().getSkriptConfig().getCommandsGeneratePermissions();

    public static ScriptCommandBuilder create(int commandType, SkriptLogger logger) {
        return new ScriptCommandBuilder(commandType, logger);
    }

    public int commandType;
    private final SkriptLogger logger;
    private String commandName;
    private final Map<String, CommandArg> args = new LinkedHashMap<>();
    private final Map<String, Argument<?, ?>> argsFromCommand = new LinkedHashMap<>();
    private AbstractCommand hyCommand;
    private CommandRegistration commandRegistration;

    private final SectionConfiguration sec = new SectionConfiguration.Builder()
        .addOptionalLiteral("can-generate-permission", Boolean.class)
        .addOptionalKey("permission")
        .addOptionalKey("description")
        .addOptionalList("aliases")
        .addLoader(new SectionLoader("trigger", true))
        .build();

    public ScriptCommandBuilder(int commandType, SkriptLogger logger) {
        this.commandType = commandType;
        this.logger = logger;
    }

    public boolean parseCommandLine(String commandLine) {
        if (commandLine.startsWith("/")) {
            commandLine = commandLine.substring(1);
        }
        if (commandLine.contains(" ")) {
            String[] commandLineSplit = commandLine.split(" ", 2);
            this.commandName = commandLineSplit[0];

            String[] argSplit = commandLineSplit[1].split("(?<=[>\\]])\\s+(?=[<\\[])");
            for (String s : argSplit) {
                CommandArg arg = CommandArg.parseArg(s);
                if (arg == null) {
                    this.logger.error("Invalid argument format: '" + s + "'", ErrorType.SEMANTIC_ERROR);
                    return false;
                }
                setupArg(arg);
            }
        } else {
            this.commandName = commandLine;
        }
        if (this.commandName.isEmpty()) {
            this.logger.error("Command cannot be empty", ErrorType.SEMANTIC_ERROR);
            return false;
        }
        return true;
    }

    public List<Statement> setupCommand(FileSection section, ParserState parserState, SkriptLogger logger, int commandType) {
        this.commandType = commandType;
        this.sec.loadConfiguration(null, section, parserState, logger);

        Optional<CodeSection> triggerSec = this.sec.getSection("trigger");
        boolean hasTrigger = triggerSec.isPresent();

        Optional<String> descOption = this.sec.getValue("description", String.class);
        if (descOption.isEmpty()) {
            descOption = Optional.of("");
        }

        String description = trim(descOption.get());
        if (description.isEmpty()) {
            description = "";
        }

        Boolean canGeneratePermission = this.sec.getValue("can-generate-permission", Boolean.class)
            .orElse(CAN_GENERATE_PERMISSION_DEFAULT);

        if (hasTrigger) {
            CodeSection trigger = triggerSec.get();
            if (trigger.getItems().isEmpty()) {
                logger.warn("Trigger section should not be empty. Or remove it if you don't need it.");
                return List.of();
            }

            this.hyCommand = switch (commandType) {
                case 1 -> new AbstractPlayerCommand(this.commandName, description) {
                    @Override
                    protected boolean canGeneratePermission() {
                        return canGeneratePermission;
                    }

                    @Override
                    protected void execute(@NotNull CommandContext commandContext, @NotNull Store<EntityStore> store,
                                           @NotNull Ref<EntityStore> ref, @NotNull PlayerRef playerRef, @NotNull World world) {

                        Player player = store.getComponent(ref, Player.getComponentType());
                        PlayerScriptCommandContext context = new PlayerScriptCommandContext(
                            ScriptCommandBuilder.this.commandName, player);

                        createLocalVariables(commandContext, context);
                        Statement.runAll(trigger, context);
                        Variables.clearLocalVariables(context);
                    }
                };
                case 2 -> new AbstractWorldCommand(this.commandName, description) {
                    @Override
                    protected boolean canGeneratePermission() {
                        return canGeneratePermission;
                    }

                    @Override
                    protected void execute(@NotNull CommandContext commandContext, @NotNull World world,
                                           @NotNull Store<EntityStore> store) {
                        WorldScriptCommandContext context = new WorldScriptCommandContext(
                            ScriptCommandBuilder.this.commandName, commandContext.sender(), world);

                        createLocalVariables(commandContext, context);
                        Statement.runAll(trigger, context);
                        Variables.clearLocalVariables(context);
                    }
                };
                default -> new AbstractCommand(this.commandName, description) {
                    @Override
                    protected boolean canGeneratePermission() {
                        return canGeneratePermission;
                    }
                    @Override
                    protected @Nullable CompletableFuture<Void> execute(@NotNull CommandContext commandContext) {
                        CompletableFuture.runAsync(() -> {
                            CommandSender sender = commandContext.sender();

                            AtomicReference<ScriptCommandContext> context = new AtomicReference<>();

                            Runnable code = () -> {
                                createLocalVariables(commandContext, context.get());
                                Statement.runAll(trigger, context.get());
                                Variables.clearLocalVariables(context.get());
                            };

                            if (sender instanceof Player player && player.getWorld() != null) {
                                // If a player runs the command, run it in their world
                                context.set(new PlayerScriptCommandContext(commandName, player));
                                World world = player.getWorld();
                                if (world.isInThread()) {
                                    code.run();
                                } else {
                                    world.execute(code);
                                }
                            } else {
                                // Otherwise run as normal
                                context.set(new ScriptCommandContext(commandName, sender));
                                code.run();
                            }
                        });
                        return null;
                    }
                };
            };
        } else {
            this.hyCommand = new AbstractCommandCollection(this.commandName, description) {
                @Override
                protected boolean canGeneratePermission() {
                    return canGeneratePermission;
                }
            };
        }
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

        // LOAD CHILDREN
        section.getElements().forEach(e -> {
            String lineContent = e.getLineContent();
            if (e instanceof FileSection fs && lineContent.startsWith("sub command")) {
                Optional<? extends CodeSection> codeSection = SyntaxParser.parseSection(fs, parserState, logger);
                if (codeSection.isPresent() && codeSection.get() instanceof ScriptCommandParent child) {
                    child.loadChild(this, logger);
                }
            }
        });
        return List.of();
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

    private void createLocalVariables(CommandContext ctx, TriggerContext triggerContext) {
        this.argsFromCommand.forEach((name, arg) -> {
            Object o = ctx.get(arg);
            if (o != null) Variables.setVariable(name, o, triggerContext, true);
        });
    }

    public void unregister() {
        if (this.commandRegistration != null) {
            this.commandRegistration.unregister();
        }
    }

    public String getCommandName() {
        return this.commandName;
    }

    public int getCommandType() {
        return this.commandType;
    }

    public void build(ScriptCommandBuilder parent) {
        if (this.hyCommand == null) return;

        if (parent == null) {
            this.commandRegistration = HySk.getInstance().getCommandRegistry().registerCommand(this.hyCommand);
        } else {
            parent.hyCommand.addSubCommand(this.hyCommand);
        }
    }

}
