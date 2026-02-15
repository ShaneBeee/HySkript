package com.github.skriptdev.skript.plugin.elements.expressions.world;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ExprWorldTPS extends PropertyExpression<World, Number> {

    public static void register(SkriptRegistration reg) {
        reg.newPropertyExpression(ExprWorldTPS.class, Number.class,
                "world tps", "worlds")
            .name("World TPS")
            .description("Get/set the ticks per second of a world.",
                "This is the rate your world will tick at. This value is not persistent and will reset after restart.",
                "Default = 30 TPS, min = 1, max = 2048")
            .examples("set world tps of world of player to 20",
                "add 5 to world tps of {_world}",
                "remove 10 from world tps of world of player",
                "reset world tps of all worlds")
            .since("INSERT VERSION")
            .register();
    }

    @Override
    public @Nullable Number getProperty(World world) {
        return world.getTps();
    }

    @Override
    public Optional<Class<?>[]> acceptsChange(@NotNull ChangeMode mode) {
        if (mode == ChangeMode.SET || mode == ChangeMode.ADD || mode == ChangeMode.REMOVE || mode == ChangeMode.RESET) {
            return Optional.of(new Class<?>[]{Number.class});
        }
        return Optional.empty();
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public void change(@NotNull TriggerContext ctx, @NotNull ChangeMode changeMode, Object @NotNull [] changeWith) {
        if (changeMode == ChangeMode.RESET) {
            for (World world : getOwner().getArray(ctx)) {
                world.setTps(30);
            }
            return;
        }
        if (changeWith == null || changeWith.length == 0 || !(changeWith[0] instanceof Number number)) {
            return;
        }

        for (World world : getOwner().getArray(ctx)) {
            int oldValue = world.getTps();
            int change = number.intValue();
            int newValue = switch (changeMode) {
                case ADD -> oldValue + change;
                case REMOVE -> oldValue - change;
                default -> change;
            };

            world.setTps(Math.clamp(newValue, 1, 2048));
        }
    }

}
