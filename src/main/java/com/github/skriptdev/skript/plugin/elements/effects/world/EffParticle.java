package com.github.skriptdev.skript.plugin.elements.effects.world;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EffParticle extends Effect {

    public static void register(SkriptRegistration reg) {
        reg.newEffect(EffParticle.class, "(draw|spawn) particle %particle% at %location%",
                "(draw|spawn) particle %particle% at %location% (to|for) %players/playerrefs%")
            .name("Particle Spawn")
            .description("Draw a particle at a location.",
                "If no receivers are specified, it will default to players in a radius of 75 of the location.",
                "If you would like more options, use the particle spawn section.")
            .examples("draw particle Want_Food_Corn at {_loc} for player",
                "draw particle Splash at location of player ~ vector3d(0, 0.5, 0)")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<ParticleSystem> particle;
    private Expression<Location> location;
    private Expression<?> receivers;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.particle = (Expression<ParticleSystem>) expressions[0];
        this.location = (Expression<Location>) expressions[1];
        if (matchedPattern == 1) {
            this.receivers = expressions[2];
        }
        return true;
    }

    @Override
    protected void execute(@NotNull TriggerContext ctx) {
        ParticleSystem particleSystem = this.particle.getSingle(ctx).orElse(null);
        if (particleSystem == null) return;

        String particleId = particleSystem.getId();

        Location location = this.location.getSingle(ctx).orElse(null);
        if (location == null) {
            return;
        }

        Vector3d pos = location.getPosition();
        Vector3f rotation = location.getRotation();
        float yaw = rotation.getYaw();
        float pitch = rotation.getPitch();
        float roll = rotation.getRoll();
        if (Float.isNaN(yaw)) yaw = 0;
        if (Float.isNaN(pitch)) pitch = 0;
        if (Float.isNaN(roll)) roll = 0;

        List<Ref<EntityStore>> receivers = new ArrayList<>();
        if (this.receivers != null) {
            for (Object o : this.receivers.getArray(ctx)) {
                if (o instanceof Player player) {
                    receivers.add(player.getReference());
                } else if (o instanceof PlayerRef playerRef) {
                    receivers.add(playerRef.getReference());
                }
            }
        }

        World world = Universe.get().getWorld(location.getWorld());
        if (world == null) return;

        Store<EntityStore> store = world.getEntityStore().getStore();

        // If no receivers were specified, use players in a radius of 75 of location
        if (receivers.isEmpty()) {
            SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = store.getResource(EntityModule.get()
                .getPlayerSpatialResourceType());
            ObjectList<Ref<EntityStore>> playerRefs = SpatialResource.getThreadLocalReferenceList();
            playerSpatialResource.getSpatialStructure().collect(pos, 75.0F, playerRefs);
            receivers.addAll(playerRefs);
        }

        ParticleUtil.spawnParticleEffect(particleId,
            pos.getX(), pos.getY(), pos.getZ(),
            yaw, pitch, roll,
            1.0f,
            null,
            null,
            receivers,
            store);
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String receives = this.receivers != null ? " to " + this.receivers.toString(ctx, debug) : "";
        return "draw particle " + this.particle.toString(ctx, debug) + " at " + this.location.toString(ctx, debug) + receives;
    }

}
