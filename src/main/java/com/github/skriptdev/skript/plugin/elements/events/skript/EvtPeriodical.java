package com.github.skriptdev.skript.plugin.elements.events.skript;

import com.github.skriptdev.skript.api.skript.event.WorldContext;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Literal;
import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.TriggerMap;
import io.github.syst3ms.skriptparser.lang.VariableString;
import io.github.syst3ms.skriptparser.lang.event.SkriptEvent;
import io.github.syst3ms.skriptparser.lang.event.StartOnLoadEvent;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class EvtPeriodical extends SkriptEvent implements StartOnLoadEvent {

    public static void register(SkriptRegistration reg) {
        reg.newEvent(EvtPeriodical.class,
                "*every %*duration%",
                "*every %*duration% in world %*string%")
            .setHandledContexts(PeriodicalContext.class)
            .name("Periodical")
            .description("Triggered every interval of a certain duration.",
                "You can optionally include a world to ensure the code in the event is executed in that world.")
            .examples("every 10 seconds in world \"default\":",
                "\tsend \"HELLO FELLOW WORLDERS\" to all players in event-world")
            .since("1.0.0")
            .register();
    }

    private Literal<Duration> duration;
    private World world;
    private ScheduledFuture<?> scheduledFuture;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        this.duration = (Literal<Duration>) expressions[0];
        if (expressions.length > 1) {
            VariableString name = (VariableString) expressions[1];
            String worldName = name.getSingle(TriggerContext.DUMMY).orElse(null);
            this.world = Universe.get().getWorld(worldName);
            if (this.world == null) {
                parseContext.getLogger().error("Invalid world: '" + worldName + "'", ErrorType.SEMANTIC_ERROR);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean check(TriggerContext ctx) {
        return ctx instanceof PeriodicalContext && this.duration.getSingle(ctx).isPresent();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        String world = this.world != null ? " in world \"" + this.world.getName() : "\"";
        return "every " + this.duration.toString(ctx, debug) + world;
    }

    @Override
    public void onInitialLoad(Trigger trigger) {
        Duration dur = this.duration.getSingle().orElseThrow(AssertionError::new);
        long durationMillis = dur.toMillis();

        Runnable runTrigger = () -> TriggerMap.callTriggersByContext(new PeriodicalContext(this.world));

        this.scheduledFuture = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
            if (this.world != null) {
                this.world.execute(runTrigger);
            } else {
                runTrigger.run();
            }
        }, durationMillis, durationMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void unload() {
        if (this.scheduledFuture == null) return;
        this.scheduledFuture.cancel(true);
    }

    public record PeriodicalContext(World world) implements WorldContext {

        @Override
        public World getWorld() {
            return this.world;
        }

        @Override
        public String getName() {
            return "periodical context";
        }

    }

}
