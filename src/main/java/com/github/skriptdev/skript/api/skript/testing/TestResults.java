package com.github.skriptdev.skript.api.skript.testing;

import com.github.skriptdev.skript.plugin.HySk;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class TestResults {

    private boolean success = true;
    private final Map<String, String> successMap = new HashMap<>();
    private final Map<String, String> failureMap = new HashMap<>();

    public boolean isSuccess() {
        return success;
    }

    public Map<String, String> getSuccessMap() {
        return successMap;
    }

    public Map<String, String> getFailureMap() {
        return failureMap;
    }

    public void addSuccess(String test, String value) {
        this.successMap.put(test, value);
    }

    public void addFailure(String test, String value) {
        this.success = false;
        this.failureMap.put(test, value);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "CallToPrintStackTrace"})
    public void printToJsonFile() {
        Path resolve = HySk.getInstance().getDataDirectory().resolve("test-results.properties");
        try {
            Files.createDirectories(resolve.getParent());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directories for " + resolve.toAbsolutePath(), e);
        }

        Properties props = new Properties();
        props.setProperty("success", Boolean.toString(this.success));
        props.setProperty("success.count", Integer.toString(this.successMap.size()));
        props.setProperty("failure.count", Integer.toString(this.failureMap.size()));

        // Store individual entries (handy for debugging in CI logs)
        for (var e : successMap.entrySet()) {
            props.setProperty("success." + e.getKey(), e.getValue());
        }
        for (var e : failureMap.entrySet()) {
            props.setProperty("failure." + e.getKey(), e.getValue());
        }

        try (var out = new OutputStreamWriter(Files.newOutputStream(resolve), StandardCharsets.UTF_8)) {
            props.store(out, "HySkript test results");
            System.out.println("Test-Results successfully written to " + resolve.toAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
