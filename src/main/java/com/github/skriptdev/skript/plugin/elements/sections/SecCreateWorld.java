package com.github.skriptdev.skript.plugin.elements.sections;

import com.github.skriptdev.skript.api.skript.event.WorldContext;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.entries.SectionConfiguration;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import io.github.syst3ms.skriptparser.variables.VariableMap;
import io.github.syst3ms.skriptparser.variables.Variables;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SecCreateWorld extends CodeSection {

    public static void register(SkriptRegistration reg) {
        reg.newSection(SecCreateWorld.class, "create world named %string%")
            .name("Create World")
            .description("Creates a new world with the specified name and some optional settings.",
                "**Entries**:",
                " - `gameplay_config` = The GameplayConfig to use for this world [optonal: GameplayConfig].",
                " - `pvp_enabled` = Whether pvp will be enabled in this world [optional: boolean].",
                " - `default_gamemode` = The default GameMode to use for this world [optional: GameMode].",
                " - `spawning_npc` = Whether to spawn NPCs in this world [optional: boolean].",
                " - `can_save_chunks` = Whether chunks will be saved in this world [optional: boolean].",
                " - `delete_on_remove` = Whether the world will be deleted when the world is removed [optional: boolean].",
                " - `gametime_paused` = Whether the gametime will be paused in this world [optional: boolean].",

                " - `run_in_world` = The code to run in the newly created world [optional: section]. " +
                    "When run in the world, `event-world` will be the newly created world.")
            .examples("create world named \"le_test_world\":",
                "\tgameplay_config: default",
                "\tpvp_enabled: true",
                "\tdefault_gamemode: Adventure",
                "\tdelete_on_remove: true",
                "\trun_in_world:",
                "\t\tset {_spawn} to world spawn of event-world",
                "\t\tteleport {_player} to {_spawn}")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<String> name;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.name = (Expression<String>) expressions[0];
        return true;
    }

    SectionConfiguration config = new SectionConfiguration.Builder()
        .addOptionalExpression("gameplay_config", GameplayConfig.class, false)
        .addOptionalLiteral("pvp_enabled", Boolean.class)
        .addOptionalLiteral("default_gamemode", GameMode.class)
        .addOptionalLiteral("spawning_npc", Boolean.class)
        .addOptionalLiteral("can_save_chunks", Boolean.class)
        .addOptionalLiteral("delete_on_remove", Boolean.class)
        .addOptionalLiteral("gametime_paused", Boolean.class)
        .addOptionalSection("run_in_world")
        .build();

    @Override
    public boolean loadSection(@NotNull FileSection section, @NotNull ParserState parserState, @NotNull SkriptLogger logger) {
        List<Class<? extends TriggerContext>> contexts = new ArrayList<>(parserState.getCurrentContexts());
        contexts.add(CreateWorldContext.class);
        parserState.setCurrentContexts(Set.copyOf(contexts));
        return this.config.loadConfiguration(null, section, parserState, logger);
    }

    @Override
    public Optional<? extends Statement> walk(@NotNull TriggerContext ctx) {
        Optional<? extends Statement> nextStatement = getNext();
        String name = this.name.getSingle(ctx).orElse(null);
        if (name == null) return nextStatement;

        Expression<GameplayConfig> configExpr = this.config.getExpression("gameplay_config", GameplayConfig.class).orElse(null);
        GameplayConfig gameplayConfig;
        if (configExpr != null) {
            gameplayConfig = configExpr.getSingle(ctx).orElse(null);
            if (gameplayConfig == null) gameplayConfig = GameplayConfig.DEFAULT;
        } else {
            gameplayConfig = GameplayConfig.DEFAULT;
        }

        Path path = Universe.get().getWorldsPath().resolve(name);
        WorldConfig worldConfig = new WorldConfig();
        worldConfig.setGameplayConfig(gameplayConfig.getId());

        this.config.getValue("pvp_enabled", Boolean.class).ifPresent(worldConfig::setPvpEnabled);
        this.config.getValue("default_gamemode", GameMode.class).ifPresent(worldConfig::setGameMode);
        this.config.getValue("spawning_npc", Boolean.class).ifPresent(worldConfig::setSpawningNPC);
        this.config.getValue("can_save_chunks", Boolean.class).ifPresent(worldConfig::setCanSaveChunks);
        this.config.getValue("delete_on_remove", Boolean.class).ifPresent(worldConfig::setDeleteOnRemove);
        this.config.getValue("gametime_paused", Boolean.class).ifPresent(worldConfig::setGameTimePaused);

        Optional<CodeSection> trigger = this.config.getSection("run_in_world");

        VariableMap variableMap = Variables.copyLocalVariables(ctx);
        Universe.get().makeWorld(name, path, worldConfig).thenApply(world -> {
            if (trigger.isPresent()) {
                CreateWorldContext createWorldContext = new CreateWorldContext(world);
                Variables.setLocalVariables(createWorldContext, variableMap);
                Statement.runAll(trigger.get(), createWorldContext);
                Variables.clearLocalVariables(createWorldContext);
            }
            return null;
        });

        return nextStatement;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "create world named " + this.name.toString(ctx, debug);
    }

    public record CreateWorldContext(World world) implements WorldContext {

        @Override
        public World getWorld() {
            return this.world;
        }

        @Override
        public String getName() {
            return "create world context";
        }
    }

}
