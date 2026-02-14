package com.github.skriptdev.skript.plugin.elements.sections;

import com.github.skriptdev.skript.api.skript.registration.NPCRegistry;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.registration.context.ContextValue;
import io.github.syst3ms.skriptparser.variables.VariableMap;
import io.github.syst3ms.skriptparser.variables.Variables;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class SecSpawnNPC extends CodeSection {

    public record SpawnMobContext(Entity entity) implements TriggerContext {

        @Override
            public String getName() {
                return "spawn_mob";
            }
        }

    public static void register(SkriptRegistration registration) {
        registration.newSection(SecSpawnNPC.class, "spawn [a|an] %npcrole% at %location%")
            .name("Spawn NPC")
            .description("Spawn an npc at a location.")
            .examples("player command /sheep:",
                "\ttrigger:",
                "\t\tset {_p} to player",
                "\t\tspawn a sheep at location of player:",
                "\t\t\tsend \"ooOOoo a sheep has joined you\" to {_p}")
            .since("1.0.0")
            .register();

        registration.newSingleContextValue(SpawnMobContext.class, Entity.class,
                "spawned-entity", SpawnMobContext::entity)
            .setUsage(ContextValue.Usage.EXPRESSION_OR_ALONE)
            .register();
        registration.newSingleContextValue(SpawnMobContext.class, Entity.class,
                "spawned-npc", SpawnMobContext::entity)
            .setUsage(ContextValue.Usage.EXPRESSION_OR_ALONE)
            .register();
    }

    private Expression<NPCRegistry.NPCRole> npcRole;
    private Expression<Location> location;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.npcRole = (Expression<NPCRegistry.NPCRole>) expressions[0];
        this.location = (Expression<Location>) expressions[1];

        ParserState parserState = parseContext.getParserState();
        List<Class<? extends TriggerContext>> triggerContexts = new ArrayList<>(parserState.getCurrentContexts().stream().toList());
        triggerContexts.add(SpawnMobContext.class);
        parserState.setCurrentContexts(new HashSet<>(triggerContexts));
        return true;
    }

    @Override
    public Optional<? extends Statement> walk(@NotNull TriggerContext ctx) {
        Optional<? extends Statement> nextStatement = getNext();

        Optional<? extends Location> locSingle = this.location.getSingle(ctx);
        Optional<? extends NPCRegistry.NPCRole> roleSingle = this.npcRole.getSingle(ctx);
        if (locSingle.isEmpty() || roleSingle.isEmpty()) return nextStatement;

        Location location = locSingle.get();
        String worldName = location.getWorld();
        if (worldName == null) return nextStatement;

        World world = Universe.get().getWorld(worldName);
        if (world == null) return nextStatement;

        Store<EntityStore> store = world.getEntityStore().getStore();

        Optional<? extends Statement> firstStatement = getFirst();

        Vector3f rotation = location.getRotation().clone();
        if (Float.isNaN(rotation.getX())) rotation = Vector3f.ZERO;

        AtomicReference<VariableMap> vars = new AtomicReference<>();
        VariableMap variableMap = Variables.copyLocalVariables(ctx);
        vars.set(variableMap);

        NPCPlugin.get().spawnEntity(store, roleSingle.get().index(), location.getPosition().clone(), rotation, null, (npcEntity, _, _) -> {
            SpawnMobContext spawnMobContext = new SpawnMobContext(npcEntity);

            // Copy the locals from the previous trigger into this context
            Variables.setLocalVariables(spawnMobContext, vars.get());

            setNext(null);
            firstStatement.ifPresent(statement ->
                Statement.runAll(statement, spawnMobContext));

            // After that is run, copy them back
            VariableMap map = Variables.copyLocalVariables(spawnMobContext);
            vars.set(map);

            // Clear locals from the no longer used SpawnMobContext
            Variables.clearLocalVariables(spawnMobContext);
        }, (npcEntity, entityStoreRef, entityStoreStore) -> {
            nextStatement.ifPresent(statement -> {
                // Take the previous local variables and use them again in this context
                Variables.setLocalVariables(ctx, vars.get());
                Statement.runAll(statement, ctx);

                // Now we're done, we can clear them out
                Variables.clearLocalVariables(ctx);
            });
        });

        return Optional.empty();
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "spawn " + this.npcRole.toString(ctx, debug) + " at " + this.location.toString(ctx, debug);
    }

}
