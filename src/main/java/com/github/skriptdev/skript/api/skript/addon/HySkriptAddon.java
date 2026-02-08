package com.github.skriptdev.skript.api.skript.addon;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import fi.sulku.hytale.TinyMsg;
import io.github.syst3ms.skriptparser.registration.SkriptAddon;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for addons for HySkript.
 */
public abstract class HySkriptAddon extends SkriptAddon {

    private Manifest manifest;
    private final HytaleLogger hytaleLogger;

    public HySkriptAddon(String name) {
        super(name);
        this.hytaleLogger = HytaleLogger.get("HySkript|" + name + "|A");
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

    public HytaleLogger getHytaleLogger() {
        return this.hytaleLogger;
    }

    final void setManifest(Manifest manifest) {
        this.manifest = manifest;
    }

    public final Manifest getManifest() {
        return manifest;
    }

    public final Message[] getInfo() {
        List<Message> info = new ArrayList<>();
        info.add(TinyMsg.parse("Version: <color:#FADD89>" + this.manifest.getVersion()));

        String description = this.manifest.getDescription();
        if (description != null) info.add(TinyMsg.parse("Description: <color:#FADD89>" + description));

        @Nullable String[] authors = this.manifest.getAuthors();
        if (authors != null) info.add(TinyMsg.parse("Authors: <color:#FADD89>" + String.join("<reset>, <color:#FADD89>", authors)));

        String website = this.manifest.getWebsite();
        if (website != null) info.add(TinyMsg.parse("Website: <color:#0CE8C3><link:" + website + ">" + website));

        return info.toArray(Message[]::new);
    }

}
