package com.github.skriptdev.skript.api.skript.addon;

import com.hypixel.hytale.server.core.Message;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class HySkriptAddon extends io.github.syst3ms.skriptparser.registration.SkriptAddon {

    private Manifest manifest;

    public HySkriptAddon(String name) {
        super(name);
    }

    /**
     * Called when the addon starts to load.
     * This is a good time to set up your syntaxes.
     */
    abstract public void onLoad();

    void setManifest(Manifest manifest) {
        this.manifest = manifest;
    }

    public Manifest getManifest() {
        return manifest;
    }

    public Message[] getInfo() {
        List<Message> info = new ArrayList<>();
        info.add(Message.raw("Version: " + this.manifest.getVersion()));

        String description = this.manifest.getDescription();
        if (description != null) info.add(Message.raw("Description: " + description));

        @Nullable String[] authors = this.manifest.getAuthors();
        if (authors != null) info.add(Message.raw("Authors: " + String.join(", ", authors)));

        String website = this.manifest.getWebsite();
        if (website != null) info.add(Message.raw("Website: ").insert(Message.raw(website).link(website)));

        return info.toArray(Message[]::new);
    }

}
