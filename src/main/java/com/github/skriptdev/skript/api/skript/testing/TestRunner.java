package com.github.skriptdev.skript.api.skript.testing;

import com.github.skriptdev.skript.api.skript.testing.elements.EvtTest.TestContext;
import com.github.skriptdev.skript.api.utils.Utils;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.util.MessageUtil;
import fi.sulku.hytale.TinyMsg;
import io.github.syst3ms.skriptparser.lang.TriggerMap;
import io.github.syst3ms.skriptparser.log.LogEntry;
import io.github.syst3ms.skriptparser.log.LogType;
import io.github.syst3ms.skriptparser.parsing.ScriptLoader;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TestRunner {

    private static final TestResults TEST_RESULTS = new TestResults();

    @SuppressWarnings("DataFlowIssue")
    public static void start() {
        Runnable runTestsRunnable = () -> {
            Utils.log("Running tests in world 'default'...");
            runTests();
            Utils.log("Finished running tests!");
        };
        Runnable loadTestsRunnable = () -> {
            Utils.log("Loading test scripts...");
            loadTests();
            Utils.log("Finished loading test scripts!");

            World world = Universe.get().getWorld("default");
            if (world.isPaused()) world.setPaused(false);

            world.execute(runTestsRunnable);
        };
        HytaleServer.SCHEDULED_EXECUTOR.schedule(loadTestsRunnable, 2, TimeUnit.SECONDS);
    }

    private static void loadTests() {
        Path path = Path.of(TestProperties.TEST_SCRIPTS_FOLDER);
        loadScripts(path);
    }

    private static void runTests() {
        TestContext testContext = new TestContext(TEST_RESULTS);
        TriggerMap.callTriggersByContext(testContext);

        if (TEST_RESULTS.isSuccess()) {
            Message message = TinyMsg.parse("<green>All tests passed!");
            Utils.log(MessageUtil.toAnsiString(message).toAnsi());
        } else {
            Utils.error("Some tests failed!");
            TEST_RESULTS.getFailureMap().forEach((test, failure) -> {
                Utils.error(" - [" + test + "]: " + failure);
            });
        }

        TEST_RESULTS.printToProperties();
        TEST_RESULTS.clear();

        Runnable shutdownServer = () -> HytaleServer.get().shutdownServer();
        HytaleServer.SCHEDULED_EXECUTOR.schedule(shutdownServer, 2, TimeUnit.SECONDS);
    }

    private static void loadScripts(Path directory) {
        File scriptsDirectory = directory.toFile();
        Utils.log("Loading test directory '" + scriptsDirectory.getAbsolutePath() + "'...");
        List<String> scriptNames = loadScriptsInDirectory(scriptsDirectory);
        Utils.log("Loaded " + scriptNames.size() + " scripts!");
    }

    private static List<String> loadScriptsInDirectory(File directory) {
        if (directory == null || !directory.isDirectory()) return List.of();

        List<String> loadedScripts = new ArrayList<>();

        File[] files = directory.listFiles();
        if (files == null) return loadedScripts;

        Arrays.sort(files,
            Comparator.comparing(File::isDirectory).reversed() // Directories first
                .thenComparing(File::getName, String.CASE_INSENSITIVE_ORDER)); // Then sort by name alphabetically

        for (File file : files) {
            // Skip disabled files and hidden files
            String fileName = file.getName();
            if (fileName.startsWith("-") || fileName.startsWith(".")) continue;
            if (file.isDirectory()) {
                loadedScripts.addAll(loadScriptsInDirectory(file));
            } else {
                if (!fileName.endsWith(".sk")) continue;
                Utils.log("Loading script '" + fileName + "'...");
                List<LogEntry> logEntries = ScriptLoader.loadScript(file.toPath(), false);
                for (LogEntry logEntry : logEntries) {
                    Utils.log(null, logEntry);
                    if (logEntry.getType()  == LogType.ERROR) {
                        TEST_RESULTS.addFailure("Parsing Error:" + fileName,  logEntry.getMessage());
                    }
                }
                loadedScripts.add(fileName.substring(0, fileName.length() - 3));
            }
        }
        return loadedScripts;
    }

}
