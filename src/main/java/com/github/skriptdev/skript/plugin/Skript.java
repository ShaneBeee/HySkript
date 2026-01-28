package com.github.skriptdev.skript.plugin;

import com.github.skriptdev.skript.api.skript.ScriptsLoader;
import com.github.skriptdev.skript.api.skript.command.ArgUtils;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.api.skript.variables.JsonVariableStorage;
import com.github.skriptdev.skript.api.utils.ReflectionUtils;
import com.github.skriptdev.skript.api.utils.Utils;
import com.github.skriptdev.skript.plugin.elements.ElementRegistration;
import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.file.FileElement;
import io.github.syst3ms.skriptparser.file.FileParser;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.log.LogEntry;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.registration.SkriptAddon;
import io.github.syst3ms.skriptparser.util.FileUtils;
import io.github.syst3ms.skriptparser.variables.Variables;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Skript extends SkriptAddon {

    public static Skript INSTANCE;
    private final HySk hySk;
    private final Path scriptsPath;
    private final SkriptLogger logger;
    private SkriptRegistration registration;
    private ElementRegistration elementRegistration;
    private ScriptsLoader scriptsLoader;

    public Skript(HySk hySk) {
        INSTANCE = this;
        this.hySk = hySk;
        this.scriptsPath = hySk.getDataDirectory().resolve("scripts");
        this.logger = new SkriptLogger(true);

        Utils.log("Setting up HySkript!");
        setup();
    }

    private void setup() {
        ReflectionUtils.init();
        ArgUtils.init();
        this.registration = new SkriptRegistration(this);
        this.elementRegistration = new ElementRegistration(this);
        this.elementRegistration.registerElements();

        // FINALIZE SETUP
        this.registration.register();

        printSyntaxCount();
        Utils.log("HySkript setup complete!");

        // LOAD VARIABLES
        loadVariables();

        // LOAD SCRIPTS
        this.scriptsLoader = new ScriptsLoader(this);
        this.scriptsLoader.loadScripts(null, this.scriptsPath, false);

        // FINALIZE SCRIPT LOADING
        Parser.getMainRegistration().getRegisterer().finishedLoading();
    }

    public void shutdown() {
        Utils.log("Saving variables...");
        Variables.shutdown();
        Utils.log("Variable saving complete!");
    }

    private void printSyntaxCount() {
        io.github.syst3ms.skriptparser.registration.SkriptRegistration mainRegistration = Parser.getMainRegistration();

        int eventSize = this.registration.getEvents().size() + mainRegistration.getEvents().size();
        int effectSize = this.registration.getEffects().size() + mainRegistration.getEffects().size();
        int expsSize = this.registration.getExpressions().size() + mainRegistration.getExpressions().size();
        int secSize = this.registration.getSections().size() + mainRegistration.getSections().size();
        int typeSize = this.registration.getTypes().size() + mainRegistration.getTypes().size();

        int total = eventSize + effectSize + expsSize + secSize + typeSize;

        Utils.log("Loaded HySkript %s elements:", total);
        Utils.log("- Types: %s", typeSize);
        Utils.log("- Events: %s ", eventSize);
        Utils.log("- Effects: %s", effectSize);
        Utils.log("- Expressions: %s", expsSize);
        Utils.log("- Sections: %s", secSize);
    }

    public HySk getPlugin() {
        return this.hySk;
    }

    public Path getScriptsPath() {
        return this.scriptsPath;
    }

    public SkriptLogger getLogger() {
        return this.logger;
    }

    public SkriptRegistration getRegistration() {
        return this.registration;
    }

    public ElementRegistration getElementRegistration() {
        return this.elementRegistration;
    }

    public ScriptsLoader getScriptsLoader() {
        return this.scriptsLoader;
    }

    public void loadVariables() {
        Utils.log("Loading variables...");
        Variables.registerStorage(JsonVariableStorage.class, "json-database");
        Path configPath = this.hySk.getDataDirectory().resolve("config.sk");
        if (!configPath.toFile().exists()) {
            InputStream resourceAsStream = this.getClass().getResourceAsStream("/config.sk");
            try {
                Files.copy(resourceAsStream, configPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        SkriptLogger logger = new SkriptLogger(false);
        List<String> strings;
        try {
            strings = FileUtils.readAllLines(configPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<FileElement> fileElements = FileParser.parseFileLines("config.sk", strings, 0, 1, logger);
        for (FileElement fileElement : fileElements) {
            if (fileElement instanceof FileSection sec && fileElement.getLineContent().equals("databases")) {
                Variables.load(logger, sec);
                logger.finalizeLogs();
                for (LogEntry logEntry : logger.close()) {
                    Utils.log(logEntry.getMessage());
                }
            }
        }
        Utils.log("Finished loading variables!");

    }

}
