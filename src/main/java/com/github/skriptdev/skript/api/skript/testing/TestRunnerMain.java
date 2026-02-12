package com.github.skriptdev.skript.api.skript.testing;

import com.google.gson.Gson;

import java.io.BufferedReader;
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

public class TestRunnerMain {

    public static final String GREEN = "\u001B[92m";
    public static final String LIGHT_GREY = "\u001B[37m";
    public static final String RED = "\u001B[91m";
    public static final String RESET = "\u001B[0m";

    private static String serverVersion;
    private static String assetPath;

    static void main(String[] args) {
        serverVersion = args[0];
        assetPath = args[1];

        System.out.println("Downloading Hytale Server...");
        downloadHytaleServer();
        System.out.println("Download complete!");

        System.out.println("Moving plugin to mods folder...");
        movePlugin();
        System.out.println("Plugin moved!");

        System.out.println("Starting Hytale server...");
        runServer();
    }

    @SuppressWarnings({"resource", "CallToPrintStackTrace"})
    private static void downloadHytaleServer() {
        String url = "https://maven.hytale.com/release/com/hypixel/hytale/Server/"
            + serverVersion + "/Server-" + serverVersion + ".jar";
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
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

    @SuppressWarnings("CallToPrintStackTrace")
    private static void runServer() {
        try {
            File serverFolder = new File("run/testServer/");
            ProcessBuilder processBuilder = new ProcessBuilder(
                "java",
                "-Xms2G",
                "-Xmx2G",
                "-Dskript.test.enabled=true",
                "-Dskript.test.scripts=../../src/test/skript/tests",
                "-jar", "HytaleServer.jar",
                "--assets", assetPath
            );
            processBuilder.inheritIO();
            processBuilder.directory(serverFolder);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            // Read results written by the plugin (no Gson required).
            Path resultsPath = Path.of("run/testServer/mods/skript_HySkript/test-results.json");
            if (!Files.exists(resultsPath)) {
                throw new IllegalStateException(
                    "Test results file not found at " + resultsPath.toAbsolutePath() +
                        " (server exit code was " + exitCode + ")"
                );
            }

            Gson gson = new Gson();
            TestResults results;

            try (BufferedReader reader = Files.newBufferedReader(resultsPath, StandardCharsets.UTF_8)) {
                results = gson.fromJson(reader, TestResults.class);

                System.out.println("Successfully loaded results from: " + resultsPath.getFileName());
            } catch (Exception e) {
                System.err.println("Could not read test results: " + e.getMessage());
                e.printStackTrace();
                return;
            }

            System.out.println("Succeeded:");
            if (!results.getSuccessMap().isEmpty()) {
                results.getSuccessMap().forEach((test, _) ->
                    System.out.println(" - " + GREEN + test + RESET));
            } else {
                System.out.println(" - none");
            }

            System.out.println("Failed:");
            if (results.getFailCount() > 0) {
                results.getFailureMap().forEach((test, errorList) ->
                    errorList.forEach(error ->
                        System.out.println(" - " + RED + test + LIGHT_GREY + ": " + error + RESET)));
            } else {
                System.out.println(" - none");
            }

            System.exit(results.getFailCount());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
