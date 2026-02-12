package com.github.skriptdev.skript.api.skript.testing.elements;

import com.github.skriptdev.skript.api.skript.event.WorldContext;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.api.skript.testing.TestResults;
import com.github.skriptdev.skript.api.utils.Utils;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.VariableString;
import io.github.syst3ms.skriptparser.lang.event.SkriptEvent;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

public class EvtTest extends SkriptEvent {

    public static void register(SkriptRegistration reg) {
        reg.newEvent(EvtTest.class, "test %*string%")
            .setHandledContexts(TestContext.class)
            .noDoc()
            .register();
    }

    private String testSubject;

    @SuppressWarnings({"OptionalGetWithoutIsPresent"})
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        this.testSubject = ((VariableString) expressions[0]).getSingle(TriggerContext.DUMMY).get();
        return true;
    }

    @Override
    public boolean check(TriggerContext triggerContext) {
        if (!(triggerContext instanceof TestContext context)) return false;
        context.setTestSubject(this.testSubject);
        Utils.logColored("<light_purple>Running test: <white>'<gray>" + this.testSubject + "<white>'");
        return true;
    }

    @Override
    public String toString(@NotNull TriggerContext triggerContext, boolean b) {
        return "test " + this.testSubject;
    }

    public static final class TestContext implements WorldContext {
        private final TestResults testResults;
        private String testSubject;
        private final World world;

        public TestContext(TestResults testResults, World world) {
            this.testResults = testResults;
            this.world = world;
        }

        public void setTestSubject(String testSubject) {
            this.testSubject = testSubject;
        }

        public String getTestSubject() {
            return testSubject;
        }

        public TestResults getTestResults() {
            return this.testResults;
        }

        @Override
        public World getWorld() {
            return this.world;
        }

        @Override
        public String getName() {
            return "test context";
        }
    }

}
