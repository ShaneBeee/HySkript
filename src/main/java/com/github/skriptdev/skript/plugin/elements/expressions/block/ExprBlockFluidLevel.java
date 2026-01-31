package com.github.skriptdev.skript.plugin.elements.expressions.block;

import com.github.skriptdev.skript.api.hytale.Block;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.math.vector.Location;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExprBlockFluidLevel implements Expression<Number> {

    public static void register(SkriptRegistration reg) {
        reg.newExpression(ExprBlockFluidLevel.class, Number.class, true,
                "[block] fluid level of %locations/blocks%")
            .name("Block Fluid Level")
            .description("Get.set the fluid level of a block.")
            .examples("set fluid level of block at player's location to 8")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<?> locations;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.locations = expressions[0];
        return true;
    }

    @Override
    public Number[] getValues(@NotNull TriggerContext ctx) {
        List<Number> levels = new ArrayList<>();

        for (Object o : this.locations.getArray(ctx)) {
            if (o instanceof Block block) {
                levels.add(block.getFluidLevel());
            } else if (o instanceof Location location) {
                Block block = new Block(location);
                levels.add(block.getFluidLevel());
            }
        }

        return levels.toArray(Number[]::new);
    }

    @Override
    public Optional<Class<?>[]> acceptsChange(@NotNull ChangeMode mode) {
        if (mode == ChangeMode.SET) return Optional.of(new Class<?>[]{Number.class});
        return Optional.empty();
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public void change(@NotNull TriggerContext ctx, @NotNull ChangeMode changeMode, Object @NotNull [] changeWith) {
        if (changeWith == null) return;
        if (!(changeWith[0] instanceof Number level)) return;

        for (Object o : this.locations.getArray(ctx)) {
            if (o instanceof Block block) {
                block.setFluidLevel(level.byteValue());
            } else if (o instanceof Location location) {
                Block block = new Block(location);
                block.setFluidLevel(level.byteValue());
            }
        }
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "fluid level of " + this.locations.toString(ctx, debug);
    }

}
