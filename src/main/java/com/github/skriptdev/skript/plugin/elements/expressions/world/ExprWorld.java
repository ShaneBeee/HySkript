package com.github.skriptdev.skript.plugin.elements.expressions.world;

import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class ExprWorld implements Expression<World> {

    public static void register(SkriptRegistration registration) {
        registration.newExpression(ExprWorld.class, World.class, false,
                "world [named|with uuid|from] %string/uuid%")
            .name("World")
            .description("Get a world by name or UUID.")
            .examples("set {_world} to world \"default\"")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<?> name;

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.name = expressions[0];
        return true;
    }

    @Override
    public World[] getValues(@NotNull TriggerContext ctx) {
        Optional<?> single = this.name.getSingle(ctx);
        if (single.isEmpty()) return null;

        World world;
        Object o = single.get();
        if (o instanceof String s) world = Universe.get().getWorld(s);
        else if (o instanceof UUID uuid) world = Universe.get().getWorld(uuid);
        else return null;

        return new World[]{world};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "world from " + this.name.toString(ctx, debug);
    }

}
