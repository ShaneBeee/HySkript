package com.github.skriptdev.skript.api.skript.addon;

import com.github.skriptdev.skript.plugin.HySk;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.util.RawJsonReader;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import org.bson.BsonDocument;
import org.bson.BsonValue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AddonLoader {

    private final SkriptLogger logger;

    public AddonLoader(SkriptLogger logger) {
        this.logger = logger;
    }

    public void loadAddonsFromFolder() {
        Path resolve = HySk.getInstance().getDataDirectory().resolve("addons");
        File addonFolder = resolve.toFile();
        if (!addonFolder.exists()) {
            if (!addonFolder.mkdirs()) {
                this.logger.error("Failed to create addons folder", ErrorType.STRUCTURE_ERROR);
                return;
            }
        } else if (!addonFolder.isDirectory()) {
            this.logger.error("Addons folder is not a directory", ErrorType.STRUCTURE_ERROR);
            return;
        }
        File[] files = addonFolder.listFiles();
        if (files == null) {
            this.logger.error("Failed to list files in addons folder", ErrorType.STRUCTURE_ERROR);
            return;
        }

        for (File file : Arrays.stream(files)
            .filter(File::isFile)
            .filter(f -> f.getName().endsWith(".jar"))
            .toList()) {
            loadAddon(file);
        }
    }

    private void loadAddon(File file) {
        try (JarFile jarFile = new JarFile(file)) {
            JarEntry jarEntry = jarFile.getJarEntry("manifest.json");
            if (jarEntry == null) {
                this.logger.error("Manifest.json not found in addon " + file.getName(), ErrorType.STRUCTURE_ERROR);
                return;
            }

            InputStream inputStream = jarFile.getInputStream(jarEntry);
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            char[] buffer = RawJsonReader.READ_BUFFER.get();
            RawJsonReader rawJsonReader = new RawJsonReader(reader, buffer);

            Manifest manifest = Manifest.CODEC.decodeJson(rawJsonReader, new ExtraInfo());
            if (manifest == null) {
                this.logger.error("Failed to decode manifest.json in addon " + file.getName(), ErrorType.STRUCTURE_ERROR);
                return;
            }

            URL[] urls = {file.toURI().toURL()};
            URLClassLoader classLoader = new URLClassLoader(urls, HySk.getInstance().getClassLoader());
            Class<?> externalClass;
            try {
                externalClass = classLoader.loadClass(manifest.getMainClass());
            } catch (ClassNotFoundException e) {
                this.logger.error("Main class not found in addon " + file.getName(), ErrorType.STRUCTURE_ERROR);
                return;
            }
            Object mainClassIntance;
            try {
                mainClassIntance = externalClass.getDeclaredConstructor(String.class).newInstance(manifest.getName());
            } catch (ReflectiveOperationException e) {
                this.logger.error("Failed to create instance of addon " + file.getName(), ErrorType.EXCEPTION);
                return;
            }
            if (mainClassIntance instanceof HySkriptAddon addon) {
                addon.setManifest(manifest);
                addon.onLoad();
            }
        } catch (IOException e) {
            this.logger.error("Failed to load addon " + file.getName(), ErrorType.EXCEPTION);
        }
    }

}
