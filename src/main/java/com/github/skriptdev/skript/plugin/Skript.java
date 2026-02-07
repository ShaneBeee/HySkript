package com.github.skriptdev.skript.plugin;

import com.github.skriptdev.skript.api.skript.ErrorHandler;
import com.github.skriptdev.skript.api.skript.ScriptsLoader;
import com.github.skriptdev.skript.api.skript.addon.AddonLoader;
import com.github.skriptdev.skript.api.skript.command.ArgUtils;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.api.skript.variables.JsonVariableStorage;
import com.github.skriptdev.skript.api.utils.ReflectionUtils;
import com.github.skriptdev.skript.api.utils.Utils;
import com.github.skriptdev.skript.plugin.command.EffectCommands;
import com.github.skriptdev.skript.plugin.elements.ElementRegistration;
import com.github.skriptdev.skript.plugin.elements.events.EventHandler;
import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.config.Config;
import io.github.syst3ms.skriptparser.config.Config.ConfigSection;
import io.github.syst3ms.skriptparser.log.LogEntry;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.registration.SkriptAddon;
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
    private SkriptRegistration registration;
    private ElementRegistration elementRegistration;
    private AddonLoader addonLoader;
    private ScriptsLoader scriptsLoader;

    Skript(HySk hySk) {
        super("HySkript");
        long start = System.currentTimeMillis();
        INSTANCE = this;
        this.hySk = hySk;
        this.scriptsPath = hySk.getDataDirectory().resolve("scripts");

        Utils.log(" ");
        Utils.log("Setting up HySkript!");
        Utils.log(" ");

        // LOAD CONFIG
        Path skriptConfigPath = hySk.getDataDirectory().resolve("config.sk");
        SkriptLogger logger = new SkriptLogger();
        this.skriptConfig = new Config(skriptConfigPath, "/config.sk", logger);
        logger.finalizeLogs();
        for (LogEntry logEntry : logger.close()) {
            Utils.log(null, logEntry);
        }

        // SETUP SKRIPT
        setup();

        // ALL DONE
        Utils.log(" ");
        long fin = System.currentTimeMillis() - start;
        Utils.log("HySkript loading completed in %sms!", fin);
        Utils.log(" ");
    }

    private void setup() {
        long start = System.currentTimeMillis();
        preSetup();
        this.registration = new SkriptRegistration(this);
        this.elementRegistration = new ElementRegistration(this.registration);
        this.elementRegistration.registerElements();

        // SETUP EFFECT COMMANDS
        setupEffectCommands();

        // FINISH SETUP
        long fin = System.currentTimeMillis() - start;
        Utils.log("HySkript setup completed in %sms!", fin);

        // LOAD ADDONS
        this.addonLoader = new AddonLoader();
        this.addonLoader.loadAddonsFromFolder();

        // SETUP ERROR HANDLER
        ErrorHandler.init();

        // LOAD VARIABLES
        loadVariables();

        // LOAD SCRIPTS
        this.scriptsLoader = new ScriptsLoader(this);
        this.scriptsLoader.loadScripts(null, this.scriptsPath, false);

        // FINALIZE SCRIPT LOADING
        Parser.getMainRegistration().getRegisterer().finishedLoading();
    }

    private void preSetup() {
        ReflectionUtils.init();
        ArgUtils.init();
    }

    public void shutdown() {
        // SHUTDOWN LISTENERS
        EventHandler.shutdown();

        // SHUTDOWN SCRIPTS
        this.scriptsLoader.shutdown();

        // SHUTDOWN VARIABLES
        Utils.log("Saving variables...");
        Variables.shutdown();
        Utils.log("Variable saving complete!");

        // SHUTDOWN ADDONS
        this.addonLoader.shutdownAddons();
    }

    private void setupEffectCommands() {
        ConfigSection effectCommandSection = this.skriptConfig.getConfigSection("effect-commands");
        if (effectCommandSection != null) {
            if (effectCommandSection.getBoolean("enabled")) {
                EffectCommands.register(this,
                    effectCommandSection.getString("token"),
                    effectCommandSection.getBoolean("allow-ops"),
                    effectCommandSection.getString("required-permission"));
            }
        }
    }

    private void loadVariables() {
        long start = System.currentTimeMillis();
        Utils.log("Loading variables...");
        Variables.registerStorage(JsonVariableStorage.class, "json-database");
        ConfigSection databases = this.skriptConfig.getConfigSection("databases");
        if (databases == null) {
            Utils.error("Databases section not found in config.sk");
            return;
        }
        SkriptLogger skriptLogger = new SkriptLogger();
        Variables.load(skriptLogger, databases);
        skriptLogger.finalizeLogs();
        for (LogEntry logEntry : skriptLogger.close()) {
            Utils.log(null, logEntry);
        }
        long fin = System.currentTimeMillis() - start;
        Utils.log("Finished loading variables in %sms!", fin);
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

    /**
     * Get an instance of Skript.
     *
     * @return Instance of Skript.
     */
    public static Skript getInstance() {
        return INSTANCE;
    }

}
