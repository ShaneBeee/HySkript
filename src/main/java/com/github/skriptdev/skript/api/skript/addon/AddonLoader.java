package com.github.skriptdev.skript.api.skript.addon;

import com.github.skriptdev.skript.api.utils.Utils;
import com.github.skriptdev.skript.plugin.HySk;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.common.plugin.AuthorInfo;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.LogEntry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AddonLoader {

    private final Map<String, HySkriptAddon> loadedAddons = new LinkedHashMap<>();
    private int addonCount = 0;

    public void loadAddons() {
        loadAddonsFromFolder();
        startAddons();
    }

    public void loadAddonsFromFolder() {
        Utils.log("Loading addons...");
        Path resolve = HySk.getInstance().getDataDirectory().resolve("addons");
        File addonFolder = resolve.toFile();
        if (!addonFolder.exists()) {
            if (!addonFolder.mkdirs()) {
                Utils.error("Failed to create addons folder");
                return;
            }
        } else if (!addonFolder.isDirectory()) {
            Utils.error("Addons folder is not a directory");
            return;
        }
        File[] files = addonFolder.listFiles();
        if (files == null) {
            Utils.error("Failed to list files in addons folder");
            return;
        }

        for (File file : Arrays.stream(files)
            .filter(File::isFile)
            .filter(f -> f.getName().endsWith(".jar"))
            .toList()) {
            loadAddonFromFile(file);
        }
        String plural = this.addonCount == 1 ? "" : "s";
        Utils.log("Finished loading %s addon%s!", this.addonCount, plural);
    }

    @SuppressWarnings("unchecked")
    private void loadAddonFromFile(File file) {
        try (JarFile jarFile = new JarFile(file)) {
            JarEntry jarEntry = jarFile.getJarEntry("manifest.json");
            if (jarEntry == null) {
                Utils.error("Manifest.json not found in addon " + file.getName());
                return;
            }

            InputStream inputStream = jarFile.getInputStream(jarEntry);
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            char[] buffer = RawJsonReader.READ_BUFFER.get();
            RawJsonReader rawJsonReader = new RawJsonReader(reader, buffer);

            Manifest manifest = Manifest.CODEC.decodeJson(rawJsonReader, new ExtraInfo());
            if (manifest == null) {
                Utils.error("Failed to decode manifest.json in addon " + file.getName());
                return;
            }

            URL[] urls = {file.toURI().toURL()};
            URLClassLoader classLoader = new URLClassLoader(urls, HySk.getInstance().getClassLoader());
            Class<?> externalClass;
            try {
                externalClass = classLoader.loadClass(manifest.getMainClass());
            } catch (ClassNotFoundException e) {
                Utils.error("Main class not found in addon " + file.getName());
                return;
            }
            if (!HySkriptAddon.class.isAssignableFrom(externalClass)) {
                Utils.error("Main class is not an instance of HySkriptAddon in addon " + file.getName());
                return;
            }
            initializeAddon((Class<HySkriptAddon>) externalClass, manifest);

        } catch (IOException e) {
            Utils.error("Failed to load addon " + file.getName(), ErrorType.EXCEPTION);
        }
    }

    private void startAddons() {
        this.loadedAddons.forEach((_, addon) -> {
            addon.start();
            // Finalize registration and logging
            for (LogEntry logEntry : addon.getSkriptRegistration().register()) {
                Utils.log(null, logEntry);
            }
        });
    }

    public <T extends HySkriptAddon> T registerAddon(JavaPlugin plugin, Class<T> addonClass) {
        PluginManifest pluginManifest = plugin.getManifest();
        String name = pluginManifest.getName();
        List<String> authors = new ArrayList<>();
        for (AuthorInfo author : pluginManifest.getAuthors()) {
            authors.add(author.getName());
        }
        Manifest addonManifest = new Manifest(null,
            name,
            pluginManifest.getVersion().toString(),
            pluginManifest.getDescription(),
            authors.toArray(new String[0]),
            pluginManifest.getWebsite());

        return initializeAddon(addonClass, addonManifest);
    }

    @SuppressWarnings("unchecked")
    private <T extends HySkriptAddon> T initializeAddon(Class<T> externalClass, Manifest manifest) {
        HySkriptAddon mainClassIntance;
        String name = manifest.getName();
        try {
            mainClassIntance = externalClass.getDeclaredConstructor(String.class).newInstance(name);
        } catch (ReflectiveOperationException e) {
            Utils.error("Failed to create instance of addon " + name, ErrorType.EXCEPTION);
            return null;
        }
        HySkriptAddon addon = mainClassIntance;
        addon.setManifest(manifest);
        this.loadedAddons.put(name, addon);
        this.addonCount++;
        return (T) addon;
    }

    public void shutdownAddons() {
        this.loadedAddons.forEach((name, addon) -> addon.shutdown());
        this.loadedAddons.clear();
    }

}
