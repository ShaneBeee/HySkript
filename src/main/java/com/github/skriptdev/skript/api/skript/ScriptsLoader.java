package com.github.skriptdev.skript.api.skript;

import com.github.skriptdev.skript.api.utils.Utils;
import com.github.skriptdev.skript.plugin.Skript;
import com.hypixel.hytale.server.core.receiver.IMessageReceiver;
import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.log.LogEntry;
import io.github.syst3ms.skriptparser.parsing.ScriptLoader;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ScriptsLoader {

    private final Skript skript;
    private int loadedScriptCount = 0;

    public ScriptsLoader(Skript skript) {
        this.skript = skript;
    }

    public void loadScripts(IMessageReceiver receiver, Path directory, boolean reload) {
        this.loadedScriptCount = 0;
        Utils.log(receiver, (reload ? "Reloading" : "Loading") + " scripts...");
        long start = System.currentTimeMillis();

        File directoryFile = directory.toFile();
        if (!directoryFile.isDirectory()) {
            if (!directoryFile.mkdirs()) {
                Utils.error("Failed to create scripts directory!");
            }
        }

        List<String> scriptNames = loadScriptsInDirectory(receiver, directoryFile);

        long end = System.currentTimeMillis() - start;
        Utils.log(receiver, (reload ? "Reloaded" : "Loaded") + " %s scripts in %sms", this.loadedScriptCount, end);

        if (reload) {
            // Run load events after reloading scripts
            for (String scriptName : scriptNames) {
                Parser.getMainRegistration().getRegisterer().finishedLoading(scriptName);
            }
        }
    }

    @SuppressWarnings("DataFlowIssue")
    public List<String> loadScriptsInDirectory(IMessageReceiver receiver, File directory) {
        if (directory == null || !directory.isDirectory()) return List.of();
        List<String> loadedScripts = new ArrayList<>();

        File[] files = directory.listFiles();
        if (files == null) return loadedScripts;

        Arrays.sort(files,
            Comparator.comparing(File::isDirectory).reversed() // Directories first
            .thenComparing(File::getName, String.CASE_INSENSITIVE_ORDER)); // Then sort by name alphabetically

        for (File file : files) {
            if (file.isDirectory()) {
                loadedScripts.addAll(loadScriptsInDirectory(receiver, file));
            } else {
                if (!file.getName().endsWith(".sk")) continue;
                Utils.log(receiver, "Loading script '" + file.getName() + "'...");
                List<LogEntry> logEntries = ScriptLoader.loadScript(file.toPath(), false);
                this.loadedScriptCount++;
                for (LogEntry logEntry : logEntries) {
                    Utils.log(receiver, logEntry);
                }
                loadedScripts.add(file.getName().substring(0, file.getName().length() - 3));
            }
        }
        return loadedScripts;
    }

    public void reloadScript(IMessageReceiver receiver, String name) {
        long start = System.currentTimeMillis();
        Path path = this.skript.getScriptsPath().resolve(name);
        if (path.toFile().isDirectory()) {
            this.loadedScriptCount = 0;
            Utils.log(receiver, "Reloading scripts in path '%s'...", name);
            List<String> scriptNames = loadScriptsInDirectory(receiver, path.toFile());
            long fin = System.currentTimeMillis() - start;
            Utils.log(receiver, "Reloaded %s scripts in %sm.", this.loadedScriptCount, fin);

            // Run load events after reloading scripts
            for (String scriptName : scriptNames) {
                Parser.getMainRegistration().getRegisterer().finishedLoading(scriptName);
            }

        } else {
            path = this.skript.getScriptsPath().resolve(name + (name.endsWith(".sk") ? "" : ".sk"));
            if (!path.toFile().exists()) {
                Utils.error("Script '%s' does not exist!", name);
                return;
            }

            Utils.log(receiver, "Reloading script '%s'...", name);
            List<LogEntry> logEntries = ScriptLoader.loadScript(path, false);
            for (LogEntry logEntry : logEntries) {
                Utils.log(receiver, logEntry);
            }
            long fin = System.currentTimeMillis() - start;
            Utils.log(receiver, "Reloaded script '%s' in %sms.", name, fin);

            String scriptFileName = path.toFile().getName();
            String scriptName = scriptFileName.substring(0, scriptFileName.length() - 3);

            // Run load events after reloading a script
            Parser.getMainRegistration().getRegisterer().finishedLoading(scriptName);
        }
    }

}
