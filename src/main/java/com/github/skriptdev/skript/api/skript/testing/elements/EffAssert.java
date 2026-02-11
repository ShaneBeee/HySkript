package com.github.skriptdev.skript.api.skript.testing.elements;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.api.skript.testing.TestResults;
import com.github.skriptdev.skript.api.skript.testing.elements.EvtTest.TestContext;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class EffAssert extends Effect {

    public static void register(SkriptRegistration reg) {
        reg.newEffect(EffAssert.class, "assert %=boolean% with %*string%")
            .noDoc()
            .register();
    }

    private String fileName;
    private int lineNumber;
    private Expression<Boolean> condition;
    private Expression<String> message;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.condition = (Expression<Boolean>) expressions[0];
        this.message = (Expression<String>) expressions[1];

        SkriptLogger logger = parseContext.getLogger();
        this.fileName = logger.getFileName();
        this.lineNumber = logger.getLine() + 1; // I think it gets the last line?!?!
        return true;
    }

    @Override
    protected void execute(@NotNull TriggerContext ctx) {
        if (!(ctx instanceof TestContext context)) return;

        Optional<? extends Boolean> b = this.condition.getSingle(ctx).filter(Boolean::booleanValue);
        TestResults testResults = context.getTestResults();

        if (b.isEmpty()) {
            // Test failed
            String message = this.message.getSingle(ctx).orElseThrow();
            String failure = String.format("assert '%s' failed with message \"%s\" {%s:%d}",
                this.condition.toString(ctx, false),
                message,
                this.fileName,
                this.lineNumber);
            testResults.addFailure(context.getTestSubject(), failure);
        } else {
            String success = String.format("assert '%s' passed",
                this.condition.toString(ctx, false));
            testResults.addSuccess(context.getTestSubject(), success);
        }
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "assert " + this.condition.toString(ctx, debug) + " with " + this.message.toString(ctx, debug);
    }

}
