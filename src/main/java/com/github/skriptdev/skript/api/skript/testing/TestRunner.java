package com.github.skriptdev.skript.api.skript.testing;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public class TestRunner {

    static void main(String[] args) {
        System.out.println("Downloading Hytale Server...");
        downloadHytaleServer();
        System.out.println("Download complete!");

        // move plugin to mods file
        System.out.println("Moving plugin to mods folder...");
        movePlugin();
        System.out.println("Plugin moved!");

        // set system property for testing
        System.out.println("Starting server...");
        runServer();
        System.out.println("Server started!");
    }

    private static void downloadHytaleServer() {
        String url = "https://maven.hytale.com/release/com/hypixel/hytale/Server" +
            "/2026.02.06-aa1b071c2/Server-2026.02.06-aa1b071c2.jar"; // TODO replace with gradle replace
        String targetDir = "run/testServer/";
        String newName = "HytaleServer.jar";

        try {
            // 1. Create the directory if it doesn't exist
            Path directoryPath = Paths.get(targetDir);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            // 2. Download the file to a temporary location
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

            // We download directly to a path to save memory
            Path tempFile = Files.createTempFile("tempDownload", ".tmp");
            client.send(request, HttpResponse.BodyHandlers.ofFile(tempFile));

            // 3. Move and Rename the file
            Path finalPath = directoryPath.resolve(newName);
            Files.move(tempFile, finalPath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("File saved to: " + finalPath.toAbsolutePath());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void movePlugin() {
        File file = new File("run/testServer/mods");
        file.mkdirs();
        try {
            Files.copy(Path.of("build/libs/HySkript-1.0.0-pre-release-3.jar"),
                Path.of("run/testServer/mods/HySkript-1.0.0-pre-release-3.jar"),
                StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void runServer() {
        try {
            File serverFolder = new File("run/testServer/");
            ProcessBuilder processBuilder = new ProcessBuilder(
                "java",
                "-Xms2G",
                "-Xmx2G",
                "-jar", "HytaleServer.jar",
                "--assets", "/Users/ShaneBee/Desktop/Server/Hytale/Assets/Assets.zip"
            );
            processBuilder.inheritIO();
            processBuilder.directory(serverFolder);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            // Read results written by the plugin (no Gson required).
            Path resultsPath = Path.of("run/testServer/mods/skript_HySkript/test-results.properties");
            if (!Files.exists(resultsPath)) {
                throw new IllegalStateException(
                    "Test results file not found at " + resultsPath.toAbsolutePath() +
                        " (server exit code was " + exitCode + ")"
                );
            }

            Properties props = new Properties();
            try (var reader = Files.newBufferedReader(resultsPath, StandardCharsets.UTF_8)) {
                props.load(reader);
            }

            int failureCount = Integer.parseInt(props.getProperty("failure.count", "0"));

            System.out.println("Exited with code " + failureCount);
            System.exit(failureCount);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
