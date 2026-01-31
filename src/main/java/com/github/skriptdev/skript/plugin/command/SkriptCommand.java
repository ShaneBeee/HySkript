package com.github.skriptdev.skript.plugin.command;

import com.github.skriptdev.skript.api.skript.docs.JsonDocPrinter;
import com.github.skriptdev.skript.api.skript.docs.MarkdownDocPrinter;
import com.github.skriptdev.skript.api.utils.Utils;
import com.github.skriptdev.skript.plugin.HySk;
import com.github.skriptdev.skript.plugin.Skript;
import com.hypixel.hytale.common.util.java.ManifestUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandRegistry;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.receiver.IMessageReceiver;
import io.github.syst3ms.skriptparser.registration.SkriptAddon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Main command for HySkript.
 */
public class SkriptCommand extends AbstractCommandCollection {

    /**
     * @param registry Plugin command registry
     * @hidden
     */
    public SkriptCommand(CommandRegistry registry) {
        super("skript", "Skript commands");
        addAliases("sk");

        // Keep these in alphabetical order
        addSubCommand(new DocsCommand());
        addSubCommand(infoCommand());
        addSubCommand(new ReloadCommand());

        registry.registerCommand(this);
    }

    private static class ReloadCommand extends AbstractCommand {

        private final RequiredArg<String> stringRequiredArg;

        protected ReloadCommand() {
            super("reload", "Reloads scripts.");
            this.stringRequiredArg = withRequiredArg("script", "A script to reload", ArgTypes.STRING);
            addSubCommand(new AbstractCommand("all", "Reloads all scripts.") {
                @Override
                protected CompletableFuture<Void> execute(@NotNull CommandContext commandContext) {
                    return CompletableFuture.runAsync(() -> {
                        Skript skript = HySk.getInstance().getSkript();
                        Path scriptsPath = skript.getScriptsPath();
                        skript.getScriptsLoader().loadScripts(commandContext.sender(), scriptsPath, true);
                    });
                }
            });
        }

        @Override
        protected @Nullable CompletableFuture<Void> execute(@NotNull CommandContext commandContext) {
            return CompletableFuture.runAsync(() -> {
                Skript skript = HySk.getInstance().getSkript();
                String s = this.stringRequiredArg.get(commandContext);
                skript.getScriptsLoader().reloadScript(commandContext.sender(), s);
            });
        }
    }

    private static class DocsCommand extends AbstractCommandCollection {
        protected DocsCommand() {
            super("docs", "Print docs to file.");
            addSubCommand(mdDocsSubCommand());
            addSubCommand(new JsonDocsCommand());
        }

        private AbstractCommand mdDocsSubCommand() {
            AbstractCommand abstractCommand = new AbstractCommand("markdown", "Print Markdown docs to file.") {
                @Override
                protected CompletableFuture<Void> execute(@NotNull CommandContext commandContext) {
                    return CompletableFuture.runAsync(MarkdownDocPrinter::printDocs);
                }
            };
            abstractCommand.addAliases("md");
            return abstractCommand;
        }
    }

    private static class JsonDocsCommand extends AbstractCommand {

        OptionalArg<String> addonArg;
        FlagArg all;

        protected JsonDocsCommand() {
            super("json", "Print JSON docs to file.");
            this.addonArg = withOptionalArg("addon", "An addon to print docs for.", ArgTypes.STRING);
            this.all = withFlagArg("all", "Print docs for all addons.");
        }

        @Override
        protected @Nullable CompletableFuture<Void> execute(@NotNull CommandContext commandContext) {
            if (this.all.provided(commandContext)) {
                return CompletableFuture.runAsync(() -> {
                    for (SkriptAddon addon : SkriptAddon.getAddons().stream().filter(addon -> !addon.getAddonName().equalsIgnoreCase("skript-parser")).toList()) {
                        JsonDocPrinter jsonDocPrinter = new JsonDocPrinter(commandContext.sender(), addon);
                        jsonDocPrinter.printDocs();
                    }
                });
            }
            SkriptAddon addon = HySk.getInstance().getSkript();
            if (this.addonArg.provided(commandContext)) {
                String s = this.addonArg.get(commandContext);
                SkriptAddon skriptAddon = SkriptAddon.getAddon(s);
                if (s.equalsIgnoreCase("skript-parser") || skriptAddon == null) {
                    Utils.log(commandContext.sender(), Level.SEVERE, "Addon '%s' not found.", s);
                    return CompletableFuture.completedFuture(null);
                }
                addon = skriptAddon;
            }
            JsonDocPrinter jsonDocPrinter = new JsonDocPrinter(commandContext.sender(), addon);
            return CompletableFuture.runAsync(jsonDocPrinter::printDocs);
        }
    }

    private AbstractCommand infoCommand() {
        return new AbstractCommand("info", "Get info about HySkript.") {
            @Override
            protected CompletableFuture<Void> execute(@NotNull CommandContext commandContext) {
                return CompletableFuture.runAsync(() -> printInfo(commandContext.sender()));
            }
        };
    }

    private void printInfo(IMessageReceiver sender) {
        Utils.sendMessage(sender, "HySkript Version: %s", HySk.getInstance().getManifest().getVersion());
        Utils.sendMessage(sender, "Hytale Version: %s (%s)", ManifestUtil.getImplementationVersion(), ManifestUtil.getPatchline());
        Utils.sendMessage(sender, "Java Version: %s", System.getProperty("java.version"));

        List<SkriptAddon> addons = SkriptAddon.getAddons().stream()
            .filter(addon -> !addon.getAddonName().equalsIgnoreCase("skript-parser")
                && !addon.getAddonName().equalsIgnoreCase("HySkript"))
            .toList();

        if (!addons.isEmpty()) {
            Utils.sendMessage(sender, "Loaded Addons: %s");
            addons.forEach(addon -> Utils.sendMessage(sender, " - %s", addon.getAddonName()));
        }

        Message link = Message.raw("https://github.com/SkriptDev/HySkript")
            .link("https://github.com/SkriptDev/HySkript")
            .color("#0CE8C3");
        Message website = Message.raw("Website: ").insert(link);
        sender.sendMessage(website);
    }

}
