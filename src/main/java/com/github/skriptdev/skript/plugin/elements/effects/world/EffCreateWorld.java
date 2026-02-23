package com.github.skriptdev.skript.plugin.elements.effects.world;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.variables.VariableMap;
import io.github.syst3ms.skriptparser.variables.Variables;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Optional;

public class EffCreateWorld extends Effect {

    public static void register(SkriptRegistration reg) {
        reg.newEffect(EffCreateWorld.class,
                "create world named %string%",
                "create world named %string% with [gameplay] config %gameplayconfig%")
            .name("Create World")
            .description("Creates a world with the specified name.",
                "You can optionally specify a gameplay config to use for the world.",
                "If no gameplay config is specified, the default gameplay config will be used.",
                "The code run after this effect will be delayed until the world is loaded.")
            .examples("create world named \"le_test_world\"",
                "create world named \"le_test_world\" with gameplay config ForgottenTemple")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<String> name;
    private Expression<GameplayConfig> config;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.name = (Expression<String>) expressions[0];
        if (matchedPattern == 1) {
            this.config = (Expression<GameplayConfig>) expressions[1];
        }
        return true;
    }

    @Override
    public Optional<? extends Statement> walk(@NotNull TriggerContext ctx) {
        Optional<? extends Statement> nextStatement = getNext();
        String name = this.name.getSingle(ctx).orElse(null);
        if (name == null) return nextStatement;

        if (Universe.get().getWorld(name) != null) {
            // World already exists
            return nextStatement;
        }

        GameplayConfig gameplayConfig = null;
        if (this.config != null) {
            gameplayConfig = this.config.getSingle(ctx).orElse(null);
        }
        if (gameplayConfig == null) {
            gameplayConfig = GameplayConfig.DEFAULT;
        }

        Path path = Universe.get().getWorldsPath().resolve(name);
        WorldConfig worldConfig = new WorldConfig();
        worldConfig.setGameplayConfig(gameplayConfig.getId());

        VariableMap variableMap = Variables.copyLocalVariables(ctx);
        Universe.get().makeWorld(name, path, worldConfig).thenApply(_ -> {
            if (nextStatement.isPresent()) {
                Variables.setLocalVariables(ctx, variableMap);
                Statement.runAll(nextStatement.get(), ctx);
                Variables.clearLocalVariables(ctx);
            }
            return null;
        });
        return Optional.empty();
    }

    @Override
    protected void execute(@NotNull TriggerContext ctx) {
        // Walk, so we can let the world load first
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String config = this.config != null ? " with gameplay config " + this.config.toString(ctx, debug) : "";
        return "create world named " + this.name.toString(ctx, debug) + config;
    }

}
