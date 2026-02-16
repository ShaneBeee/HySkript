package com.github.skriptdev.skript.plugin.elements.expressions.world;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public class ExprWorldTimeDurations extends PropertyExpression<World, Duration> {

    public static void register(SkriptRegistration reg) {
        reg.newPropertyExpression(ExprWorldTimeDurations.class, Duration.class,
                "world (daytime|1:nighttime|2:total[ ]time) duration", "worlds")
            .name("World Time Durations")
            .description("Represents the daytime/nighttime durations of a world.",
                "These values are from a GamePlayConfig but can be overridden in your World config.",
                "**Note**: Currently these cannot be set via code.")
            .examples("set {_Day} to world daytime duration of world of player")
            .since("INSERT VERSION")
            .register();
    }

    private int pattern;

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, ParseContext parseContext) {
        this.pattern = parseContext.getNumericMark();
        return super.init(expressions, matchedPattern, parseContext);
    }

    @Override
    public @Nullable Duration getProperty(@NotNull World world) {
        int seconds = switch (this.pattern) {
            case 0 -> world.getDaytimeDurationSeconds();
            case 1 -> world.getNighttimeDurationSeconds();
            case 2 -> world.getDaytimeDurationSeconds() + world.getNighttimeDurationSeconds();
            default -> 0;
        };
        return Duration.ofSeconds(seconds);
    }

}
