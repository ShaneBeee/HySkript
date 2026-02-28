package com.github.skriptdev.skript.plugin;

import com.github.skriptdev.skript.api.utils.Utils;
import com.github.skriptdev.skript.plugin.command.SkriptCommand;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import org.jetbrains.annotations.NotNull;

/**
 * Main class for HySkript as a Hytale plugin.
 */
public class HySk extends JavaPlugin {

    private static HySk INSTANCE;
    private Skript skript;

    /**
     * @hidden
     */
    public HySk(@NotNull JavaPluginInit init) {
        super(init);
        if (INSTANCE != null) {
            throw new IllegalStateException("HySkript is already initialized!");
        }
        INSTANCE = this;
    }

    /**
     * @hidden
     */
    @Override
    protected void setup() {
        // Setup early so addons can register from this
        this.skript = new Skript(this);
    }

    /**
     * @hidden
     */
    @Override
    protected void start() {
        // After addons load, startup Skript
        this.skript.start();
        new SkriptCommand(getCommandRegistry());
        BstatsMetrics.registerMetrics(this);
    }

    /**
     * @hidden
     */
    @Override
    protected void shutdown() {
        Utils.log("Shutting down HySkript...");
        this.skript.shutdown();
        Utils.log("HySkript shutdown complete!");
        INSTANCE = null;
        this.skript = null;
    }

    /**
     * Get the Skript instance.
     *
     * @return The instance of Skript.
     */
    public Skript getSkript() {
        return this.skript;
    }

    /**
     * Get the instance of HySkript.
     *
     * @return The instance of HySkript.
     */
    public static HySk getInstance() {
        return INSTANCE;
    }

}
