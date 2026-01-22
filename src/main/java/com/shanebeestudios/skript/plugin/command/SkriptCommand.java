package com.shanebeestudios.skript.plugin.command;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandRegistry;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.shanebeestudios.skript.api.utils.Utils;
import com.shanebeestudios.skript.plugin.HySk;
import com.shanebeestudios.skript.plugin.Skript;
import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.registration.SyntaxInfo;
import io.github.syst3ms.skriptparser.registration.context.ContextValue;
import io.github.syst3ms.skriptparser.types.Type;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SkriptCommand extends AbstractCommandCollection {

    public SkriptCommand(CommandRegistry registry) {
        super("skript", "Skript commands");
        addAliases("sk");

        addSubCommand(reloadCommand());
        addSubCommand(docsCommand());

        registry.registerCommand(this);
    }

    private AbstractCommand reloadCommand() {
        return new AbstractCommand("reload", "Reloads all scripts.") {
            @Override
            protected CompletableFuture<Void> execute(@NotNull CommandContext commandContext) {
                return CompletableFuture.runAsync(() -> {
                    Skript skript = HySk.getInstance().getSkript();
                    skript.getElementRegistration().clearTriggers();
                    skript.getScriptsLoader().loadScripts(skript.getScriptsPath(), true);
                });
            }
        };
    }

    private AbstractCommand docsCommand() {
        return new AbstractCommand("docs", "Print docs to file.") {
            @Override
            protected CompletableFuture<Void> execute(@NotNull CommandContext commandContext) {
                return CompletableFuture.runAsync(() -> print());
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

    private void print() {
        Skript skript = HySk.getInstance().getSkript();
        SkriptRegistration registration = skript.getRegistration();

        try {
            // EXPRESSIONS
            File file = getFile("expressions");

            PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8);
            printExpressions(writer, registration);
            printExpressions(writer, Parser.getMainRegistration());

            writer.close();

            // EFFECTS
            file = getFile("effects");

            writer = new PrintWriter(file, StandardCharsets.UTF_8);
            printEffects(writer, registration);
            printEffects(writer, Parser.getMainRegistration());

            writer.close();

            // EVENTS
            file = getFile("events");
            writer = new PrintWriter(file, StandardCharsets.UTF_8);
            printEvents(writer, registration);
            printEvents(writer, Parser.getMainRegistration());
            writer.close();

            // TYPES
            file = getFile("types");
            writer = new PrintWriter(file, StandardCharsets.UTF_8);
            printTypes(writer, registration);
            printTypes(writer, Parser.getMainRegistration());
            writer.close();

            // SECTIONS
            file = getFile("sections");
            writer = new PrintWriter(file, StandardCharsets.UTF_8);
            printSections(writer, registration);
            printSections(writer, Parser.getMainRegistration());
            writer.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void printEvents(PrintWriter writer, SkriptRegistration registration) {
        List<ContextValue<?, ?>> contextValues = registration.getContextValues();

        registration.getEvents().forEach(event -> {
            writer.println("### Event: " + event.getSyntaxClass().getSimpleName());
            writer.println("- **Patterns**:");
            event.getPatterns().forEach(pattern -> writer.println("   - `" + pattern + "`"));

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

    private void printExpressions(PrintWriter writer, SkriptRegistration registration) {
        registration.getExpressions().forEach((aClass, expressionInfos) -> {
            writer.println("### Expression: " + aClass.getSimpleName());
            expressionInfos.forEach(expressionInfo -> {
                writer.println("- **Return Type**: " + expressionInfo.getReturnType());
                writer.println("- **Patterns**:");
                expressionInfo.getPatterns().forEach(pattern -> writer.println("   - `" + pattern + "`"));
            });
            writer.println();
        });
    }

    private void printEffects(PrintWriter writer, SkriptRegistration registration) {
        for (SyntaxInfo<? extends Effect> effect : registration.getEffects()) {
            writer.println("### Effect: " + effect.getSyntaxClass().getSimpleName());
            writer.println("- **Patterns**:");
            effect.getPatterns().forEach(pattern -> writer.println("   - `" + pattern + "`"));
        }
    }

    private void printTypes(PrintWriter writer, SkriptRegistration registration) {
        List<Type<?>> types = new ArrayList<>(registration.getTypes());
        types.sort(Comparator.comparing(Type::getBaseName));
        types.forEach(type -> {
            writer.println("### Type: " + type.getBaseName());
            writer.println();
        });
    }

    private void printSections(PrintWriter writer, SkriptRegistration registration) {
        for (SyntaxInfo<? extends CodeSection> section : registration.getSections()) {
            writer.println("### Section: " + section.getSyntaxClass().getSimpleName());
            writer.println("- **Patterns**:");
            section.getPatterns().forEach(pattern -> writer.println("   - `" + pattern + "`"));
        }
    }

}
