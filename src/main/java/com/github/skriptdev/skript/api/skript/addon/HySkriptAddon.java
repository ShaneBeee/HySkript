package com.github.skriptdev.skript.api.skript.addon;

import com.github.skriptdev.skript.api.utils.Utils;
import com.github.skriptdev.skript.plugin.HySk;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandRegistry;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fi.sulku.hytale.TinyMsg;
import io.github.syst3ms.skriptparser.registration.SkriptAddon;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for addons for HySkript.
 */
@SuppressWarnings("unused")
public abstract class HySkriptAddon extends SkriptAddon {

    private final HySk hySkInstance;
    private Manifest manifest;
    private final HytaleLogger hytaleLogger;

    public HySkriptAddon(String name) {
        super(name);
        this.hytaleLogger = Utils.getAddonLogger(name);
        this.hySkInstance = HySk.getInstance();
    }

    /**
     * Called when the addon starts to load.
     * This is a good time to set up your syntaxes.
     */
    abstract public void start();

    /**
     * Called when the addon is shutting down.
     * This is a good time to clean up resources.
     */
    abstract public void shutdown();

    /**
     * Get the {@link CommandRegistry Hytale CommandRegistry}.
     *
     * @return Hytale CommandRegistry
     */
    public final CommandRegistry getHytaleCommandRegistry() {
        return this.hySkInstance.getCommandRegistry();
    }

    /**
     * Get the {@link ComponentRegistryProxy<EntityStore> Hytale EntityStoreRegistry}.
     *
     * @return Hytale EntityStoreRegistry
     */
    public final ComponentRegistryProxy<EntityStore> getHytaleEntityStoreRegistry() {
        return this.hySkInstance.getEntityStoreRegistry();
    }

    /**
     * Get the {@link ComponentRegistryProxy<ChunkStore> Hytale ChunkStoreRegistry}.
     *
     * @return Hytale ChunkStoreRegistry
     */
    public final ComponentRegistryProxy<ChunkStore> getHytaleChunkStoreRegistry() {
        return this.hySkInstance.getChunkStoreRegistry();
    }

    /**
     * Get the {@link EventRegistry Hytale EventRegistry}.
     *
     * @return Hytale EventRegistry
     */
    public final EventRegistry getHytaleEventRegistry() {
        return this.hySkInstance.getEventRegistry();
    }

    /**
     * Get your addon instance of the {@link HytaleLogger}.
     * <br>You can override this if you wish to create your own logger instance
     *
     * @return Addon instance of HytaleLogger
     */
    public HytaleLogger getHytaleLogger() {
        return this.hytaleLogger;
    }

    /**
     * Get the instance of HySkript.
     *
     * @return Instance of HySkript
     */
    public final HySk getHySkriptInstance() {
        return this.hySkInstance;
    }

    @ApiStatus.Internal
    final void setManifest(Manifest manifest) {
        this.manifest = manifest;
    }

    /**
     * Get the addon Manifest
     *
     * @return Addon Manifest
     */
    public final Manifest getManifest() {
        return manifest;
    }

    /**
     * @hidden
     */
    @ApiStatus.Internal
    public final Message[] getInfo() {
        List<Message> info = new ArrayList<>();
        info.add(TinyMsg.parse("Version: <color:#FADD89>" + this.manifest.getVersion()));

        String description = this.manifest.getDescription();
        if (description != null) info.add(TinyMsg.parse("Description: <color:#FADD89>" + description));

        @Nullable String[] authors = this.manifest.getAuthors();
        if (authors != null)
            info.add(TinyMsg.parse("Authors: <color:#FADD89>" + String.join("<reset>, <color:#FADD89>", authors)));

        String website = this.manifest.getWebsite();
        if (website != null) info.add(TinyMsg.parse("Website: <color:#0CE8C3><link:" + website + ">" + website));

        return info.toArray(Message[]::new);
    }

}
