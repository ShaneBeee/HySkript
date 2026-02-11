package com.github.skriptdev.skript.api.skript.testing;

import com.github.skriptdev.skript.api.skript.testing.elements.EvtTest.TestContext;
import com.github.skriptdev.skript.api.utils.Utils;
import com.github.skriptdev.skript.plugin.Skript;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.util.MessageUtil;
import fi.sulku.hytale.TinyMsg;
import io.github.syst3ms.skriptparser.lang.TriggerMap;
import io.github.syst3ms.skriptparser.log.LogEntry;
import io.github.syst3ms.skriptparser.parsing.ScriptLoader;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TestingThingy {

    private static Skript skript;

    @SuppressWarnings("DataFlowIssue")
    public static void start(Skript skript) {
        TestingThingy.skript = skript;

        Runnable runTestsInWorld = () -> {
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

            world.execute(runTestsInWorld);
        };
        HytaleServer.SCHEDULED_EXECUTOR.schedule(loadTestsRunnable, 2, TimeUnit.SECONDS);
    }

    private static void loadTests() {
        Path path = Path.of("/Users/ShaneBee/IdeaProjects/HySkript/HySkript/src/test/skript/tests");
        loadScripts(path);
    }

    private static void runTests() {

        TestResults testResults = new TestResults();

        TestContext testContext = new TestContext(testResults);
        TriggerMap.callTriggersByContext(testContext);

        if (testResults.isSuccess()) {
            Message message = TinyMsg.parse("<green>All tests passed!");
            Utils.log(MessageUtil.toAnsiString(message).toAnsi());
        } else {
            Utils.error("Some tests failed!");
        }

        testResults.printToJsonFile();

        // Figure out how to fail the build on GitHub
        Runnable shutdownServer = () -> HytaleServer.get().shutdownServer();
        HytaleServer.SCHEDULED_EXECUTOR.schedule(shutdownServer, 5, TimeUnit.SECONDS);
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
                }
                loadedScripts.add(fileName.substring(0, fileName.length() - 3));
            }
        }
        return loadedScripts;
    }

}
