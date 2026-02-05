package com.github.skriptdev.skript.api.skript.addon;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import java.util.Arrays;

public class Manifest {

    private static final BuilderCodec.Builder<Manifest> BUILDER = BuilderCodec.builder(Manifest.class, Manifest::new);
    public static final Codec<Manifest> CODEC = BUILDER
        .append(new KeyedCodec<>("Main", Codec.STRING),
            ((manifest, string) -> manifest.mainClass = string),
            (manifest -> manifest.mainClass))
        .add().append(new KeyedCodec<>("Name", Codec.STRING),
            ((manifest, string) -> manifest.name = string),
            (manifest -> manifest.name))
        .add().append(new KeyedCodec<>("Version", Codec.STRING),
            ((manifest, string) -> manifest.version = string),
            (manifest -> manifest.version))
        .add().append(new KeyedCodec<>("Description", Codec.STRING),
            ((manifest, string) -> manifest.description = string),
            (manifest -> manifest.description))
        .add().append(new KeyedCodec<>("Authors", Codec.STRING_ARRAY),
            ((manifest, strings) -> manifest.authors = strings),
            (manifest -> manifest.authors))
        .add().append(new KeyedCodec<>("Website", Codec.STRING),
            ((manifest, string) -> manifest.website = string),
            (manifest -> manifest.website))
        .add().build();
    private String mainClass;
    private String name;
    private String version;
    private String description;
    private String[] authors;
    private String website;

    public String getMainClass() {
        return this.mainClass;
    }

    public String getName() {
        return this.name;
    }

    public String getVersion() {
        return this.version;
    }

    public String getDescription() {
        return this.description;
    }

    public String[] getAuthors() {
        return this.authors;
    }

    public String getWebsite() {
        return this.website;
    }

    @Override
    public String toString() {
        return "Manifest{" +
            "name='" + name + '\'' +
            ", version='" + version + '\'' +
            ", description='" + description + '\'' +
            ", authors=" + Arrays.toString(authors) +
            ", website='" + website + '\'' +
            '}';
    }

}
