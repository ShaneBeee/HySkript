package com.github.skriptdev.skript.plugin.elements.expressions.block;

import com.github.skriptdev.skript.api.hytale.Block;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExprBlockFluid implements Expression<Fluid> {

    public static void register(SkriptRegistration reg) {
        reg.newExpression(ExprBlockFluid.class, Fluid.class, true,
                "fluid (at|of) %locations/blocks%")
            .name("Block Fluid")
            .description("Get the fluid at a location/block.")
            .examples("set {_fluid} to fluid at player's location",
                "set fluid of block at player's location to slime_red")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<?> locations;

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.locations = expressions[0];
        return true;
    }

    @Override
    public Fluid[] getValues(@NotNull TriggerContext ctx) {
        List<Fluid> fluids = new ArrayList<>();

        for (Object o : this.locations.getArray(ctx)) {
            if (o instanceof Block block) {
                fluids.add(block.getFluid());
            } else if (o instanceof Location location) {
                Block block = new Block(location);
                fluids.add(block.getFluid());
            }
        }

        return fluids.toArray(Fluid[]::new);
    }

    @Override
    public Optional<Class<?>[]> acceptsChange(@NotNull ChangeMode mode) {
        if (mode == ChangeMode.SET) return Optional.of(new Class<?>[]{Fluid.class});
        return Optional.empty();
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public void change(@NotNull TriggerContext ctx, @NotNull ChangeMode changeMode, Object @NotNull [] changeWith) {
        if (changeWith == null) return;

        if (!(changeWith[0] instanceof Fluid fluid)) return;
        for (Object o : this.locations.getArray(ctx)) {
            if (o instanceof Block block) {
                block.setFluid(fluid);
            } else if (o instanceof Location location) {
                Block block = new Block(location);
                block.setFluid(fluid);
            }
        }
    }

    @Override
    public boolean isSingle() {
        return this.locations.isSingle();
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "fluid of " + this.locations.toString(ctx, debug);
    }

}
