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
import io.github.syst3ms.skriptparser.lang.Structure;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.LogEntry;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.registration.SkriptAddon;
import io.github.syst3ms.skriptparser.registration.SkriptEventInfo;
import io.github.syst3ms.skriptparser.structures.functions.Functions;
import io.github.syst3ms.skriptparser.variables.Variables;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Main class for the Skript aspects of HySkript.
 */
@SuppressWarnings("unused")
public class Skript extends SkriptAddon {

    public static Skript INSTANCE;
    private final HySk hySk;
    private final Config skriptConfig;
    private final Path scriptsPath;
    private final SkriptLogger logger;
    private SkriptRegistration registration;
    private ElementRegistration elementRegistration;
    private ScriptsLoader scriptsLoader;

    Skript(HySk hySk) {
        super("HySkript");
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
        this.elementRegistration = new ElementRegistration(this.registration);
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
        var mainRegistration = Parser.getMainRegistration();

        int structureSize = 0;
        int eventSize = 0;
        for (SkriptEventInfo<?> event : this.registration.getEvents()) {
            if (Structure.class.isAssignableFrom(event.getSyntaxClass())) {
                structureSize++;
            } else {
                eventSize++;
            }
        }
        for (SkriptEventInfo<?> event : mainRegistration.getEvents()) {
            if (Structure.class.isAssignableFrom(event.getSyntaxClass())) {
                structureSize++;
            } else {
                eventSize++;
            }
        }
        int effectSize = this.registration.getEffects().size() + mainRegistration.getEffects().size();
        int expsSize = this.registration.getExpressions().size() + mainRegistration.getExpressions().size();
        int secSize = this.registration.getSections().size() + mainRegistration.getSections().size();
        int typeSize = this.registration.getTypes().size() + mainRegistration.getTypes().size();
        int funcSize = Functions.getGlobalFunctions().size();

        int total = structureSize + eventSize + effectSize + expsSize + secSize + typeSize + funcSize;

        Utils.log("Loaded HySkript %s elements:", total);
        Utils.log("- Types: %s", typeSize);
        Utils.log("- Structures: %s", structureSize);
        Utils.log("- Events: %s ", eventSize);
        Utils.log("- Effects: %s", effectSize);
        Utils.log("- Expressions: %s", expsSize);
        Utils.log("- Sections: %s", secSize);
        Utils.log("- Functions: %s", funcSize);
    }

    private void loadVariables() {
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

    /**
     * Get the instance of the HySkript plugin.
     *
     * @return The instance of HySkript.
     */
    public @NotNull HySk getPlugin() {
        return this.hySk;
    }

    /**
     * Get the Skript configuration.
     *
     * @return The Skript configuration.
     */
    public @NotNull Config getSkriptConfig() {
        return this.skriptConfig;
    }

    /**
     * Get the path where scripts are stored.
     *
     * @return The path to the scripts directory.
     */
    public @NotNull Path getScriptsPath() {
        return this.scriptsPath;
    }

    public SkriptLogger getLogger() {
        return this.logger;
    }

    /**
     * Get the registration for Skript elements.
     *
     * @return The Skript registration.
     */
    public @NotNull io.github.syst3ms.skriptparser.registration.SkriptRegistration getSkriptRegistration() {
        return this.registration;
    }

    public ElementRegistration getElementRegistration() {
        return this.elementRegistration;
    }

    public ScriptsLoader getScriptsLoader() {
        return this.scriptsLoader;
    }

}
