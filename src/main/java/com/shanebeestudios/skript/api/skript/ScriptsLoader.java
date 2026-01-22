package com.shanebeestudios.skript.api.skript;

import com.shanebeestudios.skript.api.utils.Utils;
import com.shanebeestudios.skript.plugin.Skript;
import com.shanebeestudios.skript.plugin.elements.ElementRegistration;
import io.github.syst3ms.skriptparser.log.LogEntry;
import io.github.syst3ms.skriptparser.parsing.ScriptLoader;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class ScriptsLoader {

    private final ElementRegistration elementRegistration;
    private int loadedScriptCount = 0;

    public ScriptsLoader(Skript skript) {
        this.elementRegistration = skript.getElementRegistration();
    }

    public void loadScripts(Path directory, boolean reload) {
        ScriptLoader.getTriggerMap().clear();
        this.loadedScriptCount = 0;
        Utils.log((reload ? "Reloading" : "Loading") + " scripts...");
        long start = System.currentTimeMillis();

        File directoryFile = directory.toFile();
        if (!directoryFile.isDirectory()) {
            if (!directoryFile.mkdirs()) {
                Utils.error("Failed to create scripts directory!");
            }
        }

        loadScriptsInDirectory(directoryFile);

        long end = System.currentTimeMillis() - start;
        Utils.log((reload ? "Reloaded" : "Loaded") + " %s scripts in %sms", this.loadedScriptCount, end);

        // Call load event and start periodical events
        this.elementRegistration.finishedLoading();
    }

    public void loadScriptsInDirectory(File directory) {
        if (directory == null) return;

        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                loadScriptsInDirectory(file);
            } else {
                if (!file.getName().endsWith(".sk")) continue;
                Utils.log("Loading script " + file.getName() + "...");
                List<LogEntry> logEntries = ScriptLoader.loadScript(file.toPath(), false);
                this.loadedScriptCount++;
                for (LogEntry logEntry : logEntries) {
                    Utils.log(logEntry.getMessage());
                }
            }
        }
    }

}
