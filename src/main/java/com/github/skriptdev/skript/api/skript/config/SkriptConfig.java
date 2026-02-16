package com.github.skriptdev.skript.api.skript.config;

import com.github.skriptdev.skript.api.utils.Utils;
import com.github.skriptdev.skript.plugin.Skript;
import com.hypixel.hytale.common.semver.Semver;
import io.github.syst3ms.skriptparser.config.Config;
import io.github.syst3ms.skriptparser.config.Config.ConfigSection;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.LogEntry;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.util.SkriptDate;
import io.github.syst3ms.skriptparser.util.Time;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Config for HySkript
 */
public class SkriptConfig {

    private Config config;
    private final boolean debug;
    private final int maxTargetBlockDistance;
    private final ConfigSection effectCommands;
    private final ConfigSection databases;
    private final boolean commandsGeneratePermissions;

    public SkriptConfig(Skript skript) {
        Path skriptConfigPath = skript.getPlugin().getDataDirectory().resolve("config.sk");
        SkriptLogger logger = new SkriptLogger(true);
        this.config = new Config(skriptConfigPath, "/config.sk", logger);

        // Set up debug mode
        this.debug = this.config.getBoolean("debug");
        Utils.setDebug(this.debug);

        // Check for update
        String string = this.config.getString("hyskript-version");
        if (string != null) {
            Semver configVersion = Semver.fromString(string);
            logger.debug("Checking for update from: " + configVersion);
            Semver hySkriptVersion = skript.getPlugin().getManifest().getVersion();
            if (configVersion.compareTo(hySkriptVersion) < 0) {
                logger.info("Updating config to version: " + hySkriptVersion);
                try {
                    // Update the config from the default config
                    updateConfig(skriptConfigPath, hySkriptVersion.toString());
                    // Reload the config so we have updated values
                    this.config = new Config(skriptConfigPath, "/config.sk", logger);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                logger.debug("Config is up to date");
            }
        } else {
            logger.error("hyskript-version not found in config.sk, no update checking can be performed.", ErrorType.STRUCTURE_ERROR);
        }

        // Set up max-target-block-distance
        this.maxTargetBlockDistance = this.config.getInt("max-target-block-distance");
        if (this.maxTargetBlockDistance < 0) {
            logger.error("max-target-block-distance must be greater than or equal to 0", ErrorType.STRUCTURE_ERROR);
        }

        // Set up effect commands
        this.effectCommands = this.config.getConfigSection("effect-commands");
        if (this.effectCommands == null) {
            logger.error("Effect commands section not found in config.sk", ErrorType.STRUCTURE_ERROR);
        }

        // Set up databases
        this.databases = this.config.getConfigSection("databases");
        if (this.databases == null) {
            logger.error("Databases section not found in config.sk", ErrorType.STRUCTURE_ERROR);
        }

        // Set up commands generate permissions
        this.commandsGeneratePermissions = this.config.getBoolean("commands-generate-permissions", true);

        // Setup Date/Time formats
        String defaultTime = "HH:mm:ss";
        String time = this.config.getString("default-time-format", defaultTime);
        if (time != null) {
            try {
                // Validate the pattern before using it
                DateTimeFormatter.ofPattern(time);
                Time.setDefaultTimeFormat(time);
            } catch (IllegalArgumentException e) {
                logger.error("Invalid default time format: '" + time + "' Reason: '" + e.getMessage() + "'",
                    ErrorType.STRUCTURE_ERROR);
                Time.setDefaultTimeFormat(defaultTime);
            }
        }
        String defaultDate = "EEEE dd MMMM yyyy HH:mm:ss";
        String date = this.config.getString("default-date-format", defaultDate);
        if (date != null) {
            try {
                // Validate the pattern before using it
                DateTimeFormatter.ofPattern(date);
                SkriptDate.setDefaultDateFormat(date);
            } catch (IllegalArgumentException e) {
                logger.error("Invalid default date format: '" + date + "' Reason: '" + e.getMessage() + "'",
                    ErrorType.STRUCTURE_ERROR);
                SkriptDate.setDefaultDateFormat(defaultDate);
            }
        }

        logger.finalizeLogs();
        for (LogEntry logEntry : logger.close()) {
            Utils.log(null, logEntry);
        }
    }

    @SuppressWarnings("unused")
    public Config getBaseConfig() {
        return this.config;
    }

    /**
     * Get whether debug mode is enabled
     *
     * @return Whether debug mode is enabled
     */
    public boolean getDebug() {
        return this.debug;
    }

    /**
     * Get the maximum target block distance.
     *
     * @return Maximum target block distance.
     */
    public int getMaxTargetBlockDistance() {
        return this.maxTargetBlockDistance;
    }

    /**
     * Get the effect commands section.
     *
     * @return Effect commands section.
     */
    public @Nullable ConfigSection getEffectCommands() {
        return this.effectCommands;
    }

    /**
     * Get the databases section.
     *
     * @return Databases section.
     */
    public @Nullable ConfigSection getDatabases() {
        return this.databases;
    }

    /**
     * Get whether commands should generate permissions.
     *
     * @return Whether commands should generate permissions.
     */
    public boolean getCommandsGeneratePermissions() {
        return this.commandsGeneratePermissions;
    }

    @SuppressWarnings("resource")
    private void updateConfig(Path userPath, String version) throws IOException {
        Set<String> userKeys = Files.lines(userPath)
            .map(String::trim)
            .filter(l -> !l.startsWith("#") && l.contains(":"))
            .map(l -> l.split(":")[0].trim())
            .collect(Collectors.toSet());

        InputStream stream = SkriptConfig.class.getResourceAsStream("/config.sk");
        assert stream != null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
             BufferedWriter writer = Files.newBufferedWriter(userPath, StandardOpenOption.APPEND)) {

            List<String> commentBuffer = new ArrayList<>();
            String line;

            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();

                if (trimmed.startsWith("#")) {
                    // It's a comment, save it for the next key we find
                    commentBuffer.add(line);
                } else if (trimmed.contains(":")) {
                    String key = trimmed.split(":")[0].trim();

                    if (!userKeys.contains(key)) {
                        // User is missing this key!
                        writer.newLine(); // Add spacing for readability

                        // Write the buffered comments first
                        for (String comment : commentBuffer) {
                            writer.write(comment);
                            writer.newLine();
                        }
                        // Write the actual key-value pair
                        writer.write(line);
                        writer.newLine();
                    }
                    // Clear buffer regardless of whether we wrote it or not
                    commentBuffer.clear();
                } else if (trimmed.isEmpty()) {
                    commentBuffer.clear();
                }
            }
        }

        updateConfigVersion(userPath, version);
    }

    private void updateConfigVersion(Path userPath, String newVersion) throws IOException {
        List<String> lines = Files.readAllLines(userPath);
        boolean updated = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.startsWith("hyskript-version:")) {
                lines.set(i, "hyskript-version: " + newVersion);
                updated = true;
                break;
            }
        }

        if (updated) {
            Files.write(userPath, lines);
        }
    }

}
