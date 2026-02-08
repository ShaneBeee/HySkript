package com.github.skriptdev.skript.plugin.command;

import com.github.skriptdev.skript.api.skript.addon.HySkriptAddon;
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
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.entity.entities.Player;
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
        super("skript", "HySkript commands");
        addAliases("sk", "hyskript", "hysk");

        // Keep these in alphabetical order
        addSubCommand(addonsCommand());
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
            addSubCommand(new AbstractCommand("scripts", "Reloads all scripts.") {
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
                    for (SkriptAddon addon : SkriptAddon.getAddons().stream()
                        .filter(addon -> !addon.getAddonName().equalsIgnoreCase("skript-parser"))
                        .toList()) {
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

    private AbstractCommand addonsCommand() {
        return new AbstractCommand("addons", "Get info about loaded addons.") {
            @Override
            protected CompletableFuture<Void> execute(@NotNull CommandContext commandContext) {
                return CompletableFuture.runAsync(() -> {
                    List<SkriptAddon> addons = SkriptAddon.getAddons().stream()
                        .filter(addon -> !addon.getAddonName().equalsIgnoreCase("skript-parser") &&
                            !addon.getAddonName().equalsIgnoreCase("HySkript"))
                        .toList();
                    if (addons.isEmpty()) return;
                    CommandSender sender = commandContext.sender();
                    Utils.sendMessage(sender, "Loaded Addons:");
                    addons.forEach(addon -> {
                        if (addon instanceof HySkriptAddon hySkriptAddon) {
                            Utils.sendMessage(sender, "  - %s:", hySkriptAddon.getAddonName());
                            for (Message s : hySkriptAddon.getInfo()) {
                                sender.sendMessage(Message.raw("     ").insert(s));
                            }
                        }
                    });
                });
            }
        };
    }

    private void printInfo(IMessageReceiver sender) {
        Utils.sendTinyMessage(sender, "HySkript Version: <color:#FADD89>%s",
            HySk.getInstance().getManifest().getVersion());
        Utils.sendTinyMessage(sender, "Hytale Version: <color:#FADD89>%s <reset>(<color:#8BFA89>%s<reset>)",
            ManifestUtil.getImplementationVersion(), ManifestUtil.getPatchline());
        Utils.sendTinyMessage(sender, "Java Version: <color:#FADD89>%s", System.getProperty("java.version"));

        List<SkriptAddon> addons = SkriptAddon.getAddons().stream()
            .filter(addon -> !addon.getAddonName().equalsIgnoreCase("skript-parser")
                && !addon.getAddonName().equalsIgnoreCase("HySkript"))
            .toList();

        if (!addons.isEmpty()) {
            Utils.sendTinyMessage(sender, "Loaded Addons (<color:#8BFA89>%s<reset>):", addons.size());
            addons.forEach(addon -> {
                if (addon instanceof HySkriptAddon hySkriptAddon) {
                    Utils.sendTinyMessage(sender, " - <color:#F598F2>%s:", hySkriptAddon.getAddonName());
                    for (Message s : hySkriptAddon.getInfo()) {
                        sender.sendMessage(Message.raw("     ").insert(s));
                    }
                } else {
                    Utils.sendTinyMessage(sender, " - <color:#F598F2>%s", addon.getAddonName());
                }
            });
        }

        // Website
        if (sender instanceof Player) {
            Utils.sendTinyMessage(sender, "Website: <color:#0CE8C3><link:https://github.com/SkriptDev/HySkript>HySkript on GitHub");
        } else {
            Utils.sendTinyMessage(sender, "Website: <color:#0CE8C3>https://github.com/SkriptDev/HySkript");
        }

        // Issue Tracker
        if (sender instanceof Player) {
            Utils.sendTinyMessage(sender, "Issues: <color:#F06626><link:https://github.com/SkriptDev/HySkript/issues>GitHub Issue Tracker");
        } else {
            Utils.sendTinyMessage(sender, "Issues: <color:#F06626>https://github.com/SkriptDev/HySkript/issues");
        }

        // Docs
        if (sender instanceof Player) {
            Utils.sendTinyMessage(sender, "Docs: <color:#0CE8C3><link:https://skripthub.net/hyskript/docs/>SkriptHub Docs");
        } else {
            Utils.sendTinyMessage(sender, "Docs: <color:#0CE8C3>https://skripthub.net/hyskript/docs/");
        }

    }

}
