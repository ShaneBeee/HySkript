package com.github.skriptdev.skript.plugin.command;

import com.github.skriptdev.skript.api.utils.Utils;
import com.github.skriptdev.skript.plugin.HySk;
import com.github.skriptdev.skript.plugin.Skript;
import com.hypixel.hytale.common.util.java.ManifestUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandRegistry;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.docs.Documentation;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.pattern.PatternElement;
import io.github.syst3ms.skriptparser.registration.ExpressionInfo;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.registration.SyntaxInfo;
import io.github.syst3ms.skriptparser.registration.context.ContextValue;
import io.github.syst3ms.skriptparser.types.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SkriptCommand extends AbstractCommandCollection {

    public SkriptCommand(CommandRegistry registry) {
        super("skript", "Skript commands");
        addAliases("sk");

        // Keep these in alphabetical order
        addSubCommand(docsCommand());
        addSubCommand(infoCommand());
        addSubCommand(new ReloadCommand());

        registry.registerCommand(this);
    }

    private static class ReloadCommand extends AbstractCommand {

        private final OptionalArg<String> scriptArg;

        protected ReloadCommand() {
            super("reload", "Reloads all scripts.");
            this.scriptArg = withOptionalArg("script", "A script to reload", ArgTypes.STRING);
        }

        @Override
        protected @Nullable CompletableFuture<Void> execute(@NotNull CommandContext commandContext) {
            return CompletableFuture.runAsync(() -> {
                Skript skript = HySk.getInstance().getSkript();
                skript.getElementRegistration().clearTriggers();
                Path scriptsPath = skript.getScriptsPath();
                if (this.scriptArg.provided(commandContext)) {
                    String scriptName = this.scriptArg.get(commandContext);
                    skript.getScriptsLoader().reloadScript(scriptName);
                } else {
                    skript.getScriptsLoader().loadScripts(scriptsPath, true);
                }
            });
        }
    }

    private AbstractCommand docsCommand() {
        return new AbstractCommand("docs", "Print docs to file.") {
            @Override
            protected CompletableFuture<Void> execute(@NotNull CommandContext commandContext) {
                return CompletableFuture.runAsync(() -> printDocs());
            }
        };
    }

    private AbstractCommand infoCommand() {
        return new AbstractCommand("info", "Get info about HySkript.") {
            @Override
            protected CompletableFuture<Void> execute(@NotNull CommandContext commandContext) {
                return CompletableFuture.runAsync(() -> printInfo(commandContext.sender()));
            }
        };
    }

    private @NotNull File getFile(String name) {
        File file = HySk.getInstance().getDataDirectory().resolve("docs/" + name + ".md").toFile();
        if (!file.exists()) {
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                if (!parentFile.mkdirs()) {
                    throw new RuntimeException("Unable to create docs directory.");
                } else {
                    Utils.log("Created docs directory.");
                }
            }
            try {
                if (!file.createNewFile()) {
                    Utils.error("Failed to create " + name + ".md file!");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return file;
    }

    private void printInfo(CommandSender sender) {
        Utils.sendMessage(sender, "HySkript Version: %s", HySk.getInstance().getManifest().getVersion());
        Utils.sendMessage(sender, "Hytale Version: %s", ManifestUtil.getImplementationVersion());
        Utils.sendMessage(sender, "Java Version: %s", System.getProperty("java.version"));

        Message link = Message.raw("https://github.com/SkriptDev/HySkript")
            .link("https://github.com/SkriptDev/HySkript")
            .color("#0CE8C3");
        Message website = Message.raw("Website: ").insert(link);
        sender.sendMessage(website);
    }

    private void printDocs() {
        Skript skript = HySk.getInstance().getSkript();
        SkriptRegistration registration = skript.getRegistration();

        try {
            Utils.log("Printing documentation");

            // EXPRESSIONS
            Utils.log("Printing expressions and conditions");
            File file = getFile("expressions");
            PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8);
            PrintWriter condWriter = new PrintWriter(getFile("conditions"), StandardCharsets.UTF_8);
            printExpressions(writer, condWriter, registration);
            printExpressions(writer, condWriter, Parser.getMainRegistration());

            condWriter.close();
            writer.close();

            // EFFECTS
            Utils.log("Printing effects");
            file = getFile("effects");

            writer = new PrintWriter(file, StandardCharsets.UTF_8);
            printEffects(writer, registration);
            printEffects(writer, Parser.getMainRegistration());

            writer.close();

            // EVENTS
            Utils.log("Printing events");
            file = getFile("events");
            writer = new PrintWriter(file, StandardCharsets.UTF_8);
            printEvents(writer, registration);
            printEvents(writer, Parser.getMainRegistration());
            writer.close();

            // TYPES
            Utils.log("Printing types");
            file = getFile("types");
            writer = new PrintWriter(file, StandardCharsets.UTF_8);
            printTypes(writer, registration);
            printTypes(writer, Parser.getMainRegistration());
            writer.close();

            // SECTIONS
            Utils.log("Printing sections");
            file = getFile("sections");
            writer = new PrintWriter(file, StandardCharsets.UTF_8);
            printSections(writer, registration);
            printSections(writer, Parser.getMainRegistration());
            writer.close();

            Utils.log("Documentation printed!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void printEvents(PrintWriter writer, SkriptRegistration registration) {
        List<ContextValue<?, ?>> contextValues = registration.getContextValues();

        registration.getEvents().forEach(event -> {
            Documentation documentation = event.getDocumentation();
            printDocumentation("Event", writer, documentation, event.getPatterns());

            List<ContextValue<?, ?>> valuesForThisEvent = new ArrayList<>();
            contextValues.forEach(contextValue -> {
                if (event.getContexts().contains(contextValue.getContext())) {
                    valuesForThisEvent.add(contextValue);
                }
            });
            if (!valuesForThisEvent.isEmpty()) {
                writer.println("- **ContextValues**:");
                valuesForThisEvent.forEach(contextValue -> writer.println("   - `context-" + contextValue.getPattern() + "`"));
            }
        });
    }

    private void printExpressions(PrintWriter exprWriter, PrintWriter condWriter, SkriptRegistration registration) {
        List<List<ExpressionInfo<?, ?>>> values = new ArrayList<>(registration.getExpressions().values());
        values.sort(Comparator.comparing(k -> k.getFirst().getSyntaxClass().getSimpleName()));

        for (List<ExpressionInfo<?, ?>> expressionInfos : values) {
            expressionInfos.forEach(expressionInfo -> {
                Documentation documentation = expressionInfo.getDocumentation();
                if (expressionInfo.getSyntaxClass().getSimpleName().startsWith("Cond")) {
                    printDocumentation("Condition", condWriter, documentation, expressionInfo.getPatterns());
                    condWriter.println("- **Return Type**: " + expressionInfo.getReturnType());
                } else {
                    printDocumentation("Expression", exprWriter, documentation, expressionInfo.getPatterns());
                    exprWriter.println("- **Return Type**: " + expressionInfo.getReturnType());
                }
            });
            exprWriter.println();
        }
    }

    private void printEffects(PrintWriter writer, SkriptRegistration registration) {
        for (SyntaxInfo<? extends Effect> effect : registration.getEffects()) {
            Documentation documentation = effect.getDocumentation();
            printDocumentation("Effect", writer, documentation, effect.getPatterns());
        }
    }

    private void printTypes(PrintWriter writer, SkriptRegistration registration) {
        List<Type<?>> types = new ArrayList<>(registration.getTypes());
        types.sort(Comparator.comparing(Type::getBaseName));
        types.forEach(type -> {
            Documentation documentation = type.getDocumentation();
            printDocumentation("Type", writer, documentation, List.of());
        });
    }

    private void printSections(PrintWriter writer, SkriptRegistration registration) {
        List<SyntaxInfo<? extends CodeSection>> sections = new ArrayList<>(registration.getSections());
        sections.sort(Comparator.comparing(info -> info.getSyntaxClass().getSimpleName()));
        for (SyntaxInfo<? extends CodeSection> section : sections) {
            Documentation documentation = section.getDocumentation();
            printDocumentation("Section", writer, documentation, section.getPatterns());
        }
    }

    private void printDocumentation(String type, PrintWriter writer, Documentation documentation, List<PatternElement> patterns) {
        writer.println("### " + type + ": " + documentation.getName());
        String[] description = documentation.getDescription();
        if (description != null) {
            writer.println("- **Description**:");
            for (String s : description) {
                if (s.contains("\n")) {
                    for (String string : s.split("\\n")) {
                        writer.println("  " + string + "  ");
                    }
                } else {
                    writer.println("  " + s + "  ");
                }
            }
        }
        if (documentation.getUsage() != null) {
            writer.println("- **Usage**:  ");
            String usage = documentation.getUsage();
            if (usage.length() > 200) {
                // Asset store stuff gets really long, so plop them on another page
                // GitHub's wiki pages seem to have a limit
                Utils.log("Creating asset store link for: " + documentation.getName());
                writer.println("[Click Here](https://github.com/SkriptDev/HySkript/wiki/usage-" + documentation.getName().replace(" ", "-") + ")");
                File usageFile = getFile("usage-" + documentation.getName());
                try {
                    PrintWriter usageWriter = new PrintWriter(usageFile, StandardCharsets.UTF_8);
                    usageWriter.println("### Usage: " + documentation.getName());
                    usageWriter.println("```");
                    for (String s : usage.split(", ")) {
                        usageWriter.println(s);
                    }
                    usageWriter.println("```");
                    usageWriter.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                writer.println("`");
                writer.println(documentation.getUsage());
                writer.println("`");
            }
        }
        if (!patterns.isEmpty()) {
            writer.println("- **Patterns**:");
            patterns.forEach(pattern -> writer.println("   - `" + pattern + "`"));
        }
        if (documentation.getExamples() != null) {
            writer.println("- **Examples**:  ");
            writer.println("```applescript");
            for (String s : documentation.getExamples()) {
                writer.println(s);
            }
            writer.println("```");
        }
        if (documentation.getSince() != null) {
            writer.println("- **Since**: " + documentation.getSince());
        }
    }

}
