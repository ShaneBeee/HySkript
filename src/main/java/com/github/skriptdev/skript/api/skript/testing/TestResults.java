package com.github.skriptdev.skript.api.skript.testing;

import com.github.skriptdev.skript.api.utils.Utils;
import com.github.skriptdev.skript.plugin.HySk;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Handles the results of tests.
 */
public class TestResults {

    private boolean success = true;
    private int failCount = 0;
    private final Map<String, List<String>> successMap = new TreeMap<>();
    private final Map<String, List<String>> failureMap = new TreeMap<>();

    public boolean isSuccess() {
        return this.success;
    }

    public int getFailCount() {
        return this.failCount;
    }

    public Map<String, List<String>> getSuccessMap() {
        return this.successMap;
    }

    public Map<String, List<String>> getFailureMap() {
        return this.failureMap;
    }

    public void addSuccess(String test, String value) {
        this.successMap.computeIfAbsent(test, _ -> new ArrayList<>()).add(value);
    }

    public void addFailure(String test, String value) {
        this.success = false;
        this.failCount++;
        this.failureMap.computeIfAbsent(test, _ -> new ArrayList<>()).add(value);
    }

    public void process() {
        this.failureMap.forEach((test, _) -> {
            // We don't care about success if other tests failed in that test
            this.successMap.remove(test);
        });

    }

    public void clear() {
        this.success = true;
        this.successMap.clear();
        this.failureMap.clear();
    }

    @SuppressWarnings({"CallToPrintStackTrace"})
    public void printToJsonFile() {
        Path resolve = HySk.getInstance().getDataDirectory().resolve("test-results.json");

        try {
            Files.createDirectories(resolve.getParent());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directories for " + resolve.toAbsolutePath(), e);
        }

        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

        try (BufferedWriter writer = Files.newBufferedWriter(resolve, StandardCharsets.UTF_8)) {
            gson.toJson(this, writer);
            Utils.log("Test-Results successfully written to " + resolve.toAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
