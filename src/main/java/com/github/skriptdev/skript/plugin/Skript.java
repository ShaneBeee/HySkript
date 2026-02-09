package com.github.skriptdev.skript.plugin;

import com.github.skriptdev.skript.api.skript.ErrorHandler;
import com.github.skriptdev.skript.api.skript.ScriptsLoader;
import com.github.skriptdev.skript.api.skript.addon.AddonLoader;
import com.github.skriptdev.skript.api.skript.command.ArgUtils;
import com.github.skriptdev.skript.api.skript.config.SkriptConfig;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.api.skript.variables.JsonVariableStorage;
import com.github.skriptdev.skript.api.utils.ReflectionUtils;
import com.github.skriptdev.skript.api.utils.Utils;
import com.github.skriptdev.skript.plugin.command.EffectCommands;
import com.github.skriptdev.skript.plugin.elements.ElementRegistration;
import com.github.skriptdev.skript.plugin.elements.events.EventHandler;
import com.hypixel.hytale.server.core.event.events.BootEvent;
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
    private final SkriptConfig skriptConfig;
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
        this.skriptConfig = new SkriptConfig(this);

        // SETUP SKRIPT
        setupSkript();

        // ALL DONE
        Utils.log(" ");
        long fin = System.currentTimeMillis() - start;
        Utils.log("HySkript loading completed in %sms!", fin);
        Utils.log(" ");
    }

    private void setupSkript() {
        long start = System.currentTimeMillis();

        // INITIALIZE UTILITIES
        Utils.debug("Initializing utilities...");
        ReflectionUtils.init();
        ArgUtils.init();

        // SETUP REGISTRATION
        Utils.debug("Setting up registration...");
        this.registration = new SkriptRegistration(this);
        this.elementRegistration = new ElementRegistration(this.registration);

        // REGISTER ELEMENTS
        Utils.debug("Registering elements...");
        this.elementRegistration.registerElements();

        // SETUP EFFECT COMMANDS
        Utils.debug("Setting up effect commands...");
        setupEffectCommands();

        // FINISH SETUP
        long fin = System.currentTimeMillis() - start;
        Utils.log("HySkript setup completed in %sms!", fin);

        // LOAD ADDONS
        this.addonLoader = new AddonLoader();
        this.addonLoader.loadAddonsFromFolder();

        // SETUP ERROR HANDLER
        Utils.debug("Setting up error handler...");
        ErrorHandler.init();

        // LOAD VARIABLES
        loadVariables();

        // LOAD SCRIPTS
        this.scriptsLoader = new ScriptsLoader(this);
        this.scriptsLoader.loadScripts(null, this.scriptsPath, false);

        // FINALIZE SCRIPT LOADING
        this.hySk.getEventRegistry().register(BootEvent.class, event -> {
            Utils.debug("Hytale finished booting, starting post-load triggers...");
            // Start any post-load triggers after Hytale finishes booting.
            getAddons().forEach(SkriptAddon::finishedLoading);
        });
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
        ConfigSection effectCommandSection = this.skriptConfig.getEffectCommands();
        if (effectCommandSection != null) {
            if (effectCommandSection.getBoolean("enabled")) {
                EffectCommands.register(this,
                    effectCommandSection.getString("token"),
                    effectCommandSection.getBoolean("allow-ops"),
                    effectCommandSection.getString("required-permission"));
            }
        } else {
            Utils.debug("Effect commands section is missing in config.sk");
        }
    }

    private void loadVariables() {
        long start = System.currentTimeMillis();
        Utils.log("Loading variables...");
        Variables.registerStorage(JsonVariableStorage.class, "json-database");
        ConfigSection databases = this.skriptConfig.getDatabases();
        if (databases == null) {
            return;
        }
        SkriptLogger skriptLogger = new SkriptLogger(true);
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
    public @NotNull SkriptConfig getSkriptConfig() {
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
