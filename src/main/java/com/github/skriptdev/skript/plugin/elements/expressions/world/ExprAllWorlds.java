package com.github.skriptdev.skript.plugin.elements.expressions.world;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ExprAllWorlds implements Expression<Object> {

    public static void register(SkriptRegistration reg) {
        reg.newExpression(ExprAllWorlds.class, Object.class, false,
                "all worlds",
                "all world names",
                "all world keys")
            .name("All Worlds")
            .description("Get all worlds or their names.")
            .since("1.2.0")
            .register();
    }

    private int pattern;

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.pattern = matchedPattern;
        return true;
    }

    @Override
    public Object[] getValues(@NotNull TriggerContext ctx) {
        Map<String, World> worlds = Universe.get().getWorlds();
        if (this.pattern == 0) {
            return worlds.values().toArray(new World[0]);
        } else {
            return worlds.keySet().toArray(new String[0]);
        }
    }

    @Override
    public Class<?> getReturnType() {
        if (this.pattern == 0) return World.class;
        return String.class;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        if (this.pattern == 0) return "all worlds";
        return "all world names";
    }

}
