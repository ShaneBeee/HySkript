package com.github.skriptdev.skript.plugin;

import com.github.skriptdev.skript.api.skript.ScriptsLoader;
import com.github.skriptdev.skript.api.skript.command.ArgUtils;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.api.skript.variables.JsonVariableStorage;
import com.github.skriptdev.skript.api.utils.ReflectionUtils;
import com.github.skriptdev.skript.api.utils.Utils;
import com.github.skriptdev.skript.plugin.elements.ElementRegistration;
import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.config.Config;
import io.github.syst3ms.skriptparser.config.Config.ConfigSection;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.LogEntry;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.registration.SkriptAddon;
import io.github.syst3ms.skriptparser.variables.Variables;

import java.nio.file.Path;

public class Skript extends SkriptAddon {

    public static Skript INSTANCE;
    private final HySk hySk;
    private final Config skriptConfig;
    private final Path scriptsPath;
    private final SkriptLogger logger;
    private SkriptRegistration registration;
    private ElementRegistration elementRegistration;
    private ScriptsLoader scriptsLoader;

    public Skript(HySk hySk) {
        INSTANCE = this;
        this.hySk = hySk;
        this.scriptsPath = hySk.getDataDirectory().resolve("scripts");
        this.logger = new SkriptLogger();

        Path skriptConfigPath = hySk.getDataDirectory().resolve("config.sk");
        this.skriptConfig = new Config(skriptConfigPath, "/config.sk", this.logger);
        this.logger.setDebug(this.skriptConfig.getBoolean("debug"));

        Utils.log("Setting up HySkript!");
        setup();
        this.logger.finalizeLogs();
        for (LogEntry logEntry : this.logger.close()) {
            Utils.log(null, logEntry);
        }
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
        ConfigSection databases = this.skriptConfig.getConfigSection("databases");
        if (databases == null) {
            this.logger.error("Databases section not found in config.sk", ErrorType.STRUCTURE_ERROR);
            return;
        }
        Variables.load(this.logger, databases);
        Utils.log("Finished loading variables!");
    }

}
