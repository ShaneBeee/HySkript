package com.github.skriptdev.skript.plugin.elements.effects.entity;

import com.github.skriptdev.skript.api.skript.registration.NPCRegistry.NPCRole;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class EffSpawnEntity extends Effect {

    public static void register(SkriptRegistration registration) {
        registration.newEffect(EffSpawnEntity.class, "spawn [a|an] %npcrole% at %location%")
            .name("Spawn NPC")
            .description("Spawn an npc at a location.")
            .examples("player command /sheep:",
                "\ttrigger:",
                "\t\tspawn a sheep at location of player")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<NPCRole> npcRole;
    private Expression<Location> location;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.npcRole = (Expression<NPCRole>) expressions[0];
        this.location = (Expression<Location>) expressions[1];
        return true;
    }

    @Override
    protected void execute(@NotNull TriggerContext ctx) {
        Optional<? extends Location> locSingle = this.location.getSingle(ctx);
        Optional<? extends NPCRole> roleSingle = this.npcRole.getSingle(ctx);
        if (locSingle.isEmpty() || roleSingle.isEmpty()) return;

        Location location = locSingle.get();
        String worldName = location.getWorld();
        if (worldName == null) return;

        World world = Universe.get().getWorld(worldName);
        if (world == null) return;

        Store<EntityStore> store = world.getEntityStore().getStore();

        Vector3f rotation = location.getRotation().clone();
        if (Float.isNaN(rotation.getX())) rotation = Vector3f.ZERO;

        NPCPlugin.get().spawnEntity(store, roleSingle.get().index(), location.getPosition().clone(), rotation, null, null, null);
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "spawn " + this.npcRole.toString(ctx, debug) + " at " + this.location.toString(ctx, debug);
    }

}
