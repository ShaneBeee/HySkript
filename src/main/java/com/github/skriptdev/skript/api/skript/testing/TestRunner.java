package com.github.skriptdev.skript.api.skript.testing;

import com.github.skriptdev.skript.api.skript.testing.elements.EvtTest.TestContext;
import com.github.skriptdev.skript.api.utils.Utils;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.util.MessageUtil;
import fi.sulku.hytale.TinyMsg;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.lang.TriggerMap;
import io.github.syst3ms.skriptparser.log.LogEntry;
import io.github.syst3ms.skriptparser.log.LogType;
import io.github.syst3ms.skriptparser.parsing.ScriptLoader;
import io.github.syst3ms.skriptparser.registration.SkriptAddon;
import io.github.syst3ms.skriptparser.variables.Variables;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TestRunner {

    private static final ScheduledExecutorService SCHEDULED = HytaleServer.SCHEDULED_EXECUTOR;
    private final TestResults testResults = new TestResults();
    private final World world = Universe.get().getWorld("default");

    @SuppressWarnings("DataFlowIssue")
    public void start() {
        Runnable runTestsRunnable = () -> {
            Utils.log("Running tests in world 'default'...");
            runTests();
        };
        Runnable loadTestsRunnable = () -> {
            Utils.log("Testing has started!");
            Utils.log("Loading test scripts...");
            loadTests();
            Utils.log("Finished loading test scripts!");

            // Make sure the world isn't paused
            if (this.world.isPaused()) this.world.setPaused(false);

            // Run our tests in the world to make sure we have access to blocks/entities
            this.world.execute(runTestsRunnable);
        };

        // Delay start to make sure the server has finished loading
        SCHEDULED.schedule(loadTestsRunnable, 1, TimeUnit.SECONDS);
    }

    private void loadTests() {
        Path path = Path.of(TestProperties.TEST_SCRIPTS_FOLDER);
        loadScripts(path);
    }

    private void runTests() {
        // Catch exceptions and treat them as failures
        Statement.setExceptionHandler(e ->
            this.testResults.addFailure("Exception",
                e.getClass().getSimpleName() + ": " + e.getMessage()));

        // Run all the test triggers
        for (Trigger allTrigger : TriggerMap.getAllTriggers()) {
            TestContext context = new TestContext(this.testResults, this.world);
            Statement.runAll(allTrigger, context);
            Variables.clearLocalVariables(context);
        }

        Runnable finishingRunnable = () -> {
            // Process results
            this.testResults.process();

            // Print results
            if (this.testResults.isSuccess()) {
                Message message = TinyMsg.parse("<green>All tests passed!");
                Utils.log(MessageUtil.toAnsiString(message).toAnsi());
            } else {
                Utils.error(this.testResults.getFailCount() + " tests failed!");
                this.testResults.getFailureMap().forEach((test, failure) ->
                    Utils.error(" - [" + test + "]: " + failure));
            }

            Utils.log("Finished running tests!");

            // Print results to file
            this.testResults.printToJsonFile();
            this.testResults.clear();

            // Shutdown server
            // Give it a bit of delay to really make sure we're finished
            SCHEDULED.schedule(() -> HytaleServer.get().shutdownServer(), 1, TimeUnit.SECONDS);
        };

        // Delay finish to allow waits in tests
        SCHEDULED.schedule(finishingRunnable, 1, TimeUnit.SECONDS);
    }

    private void loadScripts(Path directory) {
        File scriptsDirectory = directory.toFile();
        Utils.log("Loading test directory '" + scriptsDirectory.getAbsolutePath() + "'...");
        List<String> scriptNames = loadScriptsInDirectory(scriptsDirectory);
        SkriptAddon.getAddons().forEach(SkriptAddon::finishedLoading);

        Utils.log("Loaded " + scriptNames.size() + " scripts!");
    }

    private List<String> loadScriptsInDirectory(File directory) {
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
                    if (logEntry.getType() == LogType.ERROR) {
                        this.testResults.addFailure("Parsing Error:" + fileName, logEntry.getMessage());
                    }
                }
                loadedScripts.add(fileName.substring(0, fileName.length() - 3));
            }
        }
        return loadedScripts;
    }

}
