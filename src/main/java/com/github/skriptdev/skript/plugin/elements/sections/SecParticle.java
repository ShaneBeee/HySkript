package com.github.skriptdev.skript.plugin.elements.sections;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSystem;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.entries.SectionConfiguration;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import io.github.syst3ms.skriptparser.util.color.Color;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SecParticle extends CodeSection {

    public static void register(SkriptRegistration reg) {
        reg.newSection(SecParticle.class,
                "(draw|spawn) particle %particle% at %location%")
            .name("Particle Spawn")
            .description("Draw a particle at a location.",
                "If you don't need the entries, you can use the particle spawn effect.",
                "**Entries**:",
                " - `rotation` = The rotation of the particle [optional: vector3f].",
                " - `scale` = The size of the particle [optional: number, default = 1].",
                " - `color` = The color of the particle [optional: color/string(hex)].",
                " - `source` = Who sent the particle (they wont see it) [optional: entity/player].",
                " - `receivers` = Who will receive the particle [optional: players/playerRefs, " +
                    "default: players in a radius of 75 of the location].")
            .examples("draw particle Totem_Heal_AoE at {_loc}:",
                "\trotation: vector3f(0.5, 1, 1)",
                "\tcolor: red",
                "\tscale: 0.5")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<ParticleSystem> particle;
    private Expression<Location> location;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.particle = (Expression<ParticleSystem>) expressions[0];
        this.location = (Expression<Location>) expressions[1];
        return true;
    }

    SectionConfiguration config = new SectionConfiguration.Builder()
        .addOptionalExpression("rotation", Vector3f.class, false)
        .addOptionalExpression("scale", Number.class, false)
        .addOptionalExpression("color", Object.class, false)
        .addOptionalExpression("source", Entity.class, false)
        .addOptionalExpression("receivers", Object.class, true)
        .build();

    @Override
    public boolean loadSection(@NotNull FileSection section, @NotNull ParserState parserState, @NotNull SkriptLogger logger) {
        return this.config.loadConfiguration(null, section, parserState, logger);
    }

    @Override
    public Optional<? extends Statement> walk(@NotNull TriggerContext ctx) {
        Optional<? extends Statement> nextStatement = getNext();

        ParticleSystem particleSystem = this.particle.getSingle(ctx).orElse(null);
        if (particleSystem == null) return nextStatement;

        String particleId = particleSystem.getId();

        Location location = this.location.getSingle(ctx).orElse(null);
        if (location == null) {
            return nextStatement;
        }

        Vector3d pos = location.getPosition();
        Vector3f rotation = location.getRotation();

        Expression<Vector3f> vecExpr = this.config.getExpression("rotation", Vector3f.class).orElse(null);
        if (vecExpr != null) {
            Vector3f vec = vecExpr.getSingle(ctx).orElse(null);
            if (vec != null) rotation = vec;
        }

        float scale = 1.0f;
        Expression<Number> scaleExpr = this.config.getExpression("scale", Number.class).orElse(null);
        if (scaleExpr != null) {
            Number number = scaleExpr.getSingle(ctx).orElse(null);
            if (number != null) scale = number.floatValue();
        }

        Color color = null;
        Expression<Object> colorExpr = this.config.getExpression("color", Object.class).orElse(null);
        if (colorExpr != null) {
            Object o = colorExpr.getSingle(ctx).orElse(null);
            if (o instanceof Color c) {
                color = c;
            } else if (o instanceof String s) {
                color = Color.ofHex(s).orElse(null);
            }
        }

        Ref<EntityStore> source = null;
        Expression<Entity> sourceExpr = this.config.getExpression("source", Entity.class).orElse(null);
        if (sourceExpr != null) {
            Entity entity = sourceExpr.getSingle(ctx).orElse(null);
            if (entity != null) source = entity.getReference();
        }

        List<Ref<EntityStore>> receivers = new ArrayList<>();
        Optional<Expression<Object>> receiversExpr = this.config.getExpression("receivers", Object.class);
        if (receiversExpr.isPresent()) {
            for (Object o : receiversExpr.get().getArray(ctx)) {
                if (o instanceof Player player) {
                    receivers.add(player.getReference());
                } else if (o instanceof PlayerRef playerRef) {
                    receivers.add(playerRef.getReference());
                }
            }
        }

        World world = Universe.get().getWorld(location.getWorld());
        if (world == null) return nextStatement;

        Store<EntityStore> store = world.getEntityStore().getStore();

        // If no receivers were specified, use players in a radius of 75 of location
        if (receivers.isEmpty()) {
            SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = store.getResource(EntityModule.get()
                .getPlayerSpatialResourceType());
            ObjectList<Ref<EntityStore>> playerRefs = SpatialResource.getThreadLocalReferenceList();
            playerSpatialResource.getSpatialStructure().collect(pos, 75.0F, playerRefs);
            receivers.addAll(playerRefs);
        }

        float yaw = rotation.getYaw();
        float pitch = rotation.getPitch();
        float roll = rotation.getRoll();
        if (Float.isNaN(yaw)) yaw = 0;
        if (Float.isNaN(pitch)) pitch = 0;
        if (Float.isNaN(roll)) roll = 0;

        ParticleUtil.spawnParticleEffect(particleId,
            pos.getX(), pos.getY(), pos.getZ(),
            yaw, pitch, roll,
            scale,
            getHytaleColor(color),
            source,
            receivers,
            store);

        return nextStatement;
    }

    private com.hypixel.hytale.protocol.Color getHytaleColor(@Nullable Color color) {
        if (color == null) return null;
        return new com.hypixel.hytale.protocol.Color(
            (byte) color.getRed(),
            (byte) color.getGreen(),
            (byte) color.getBlue()
        );
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "draw particle " + this.particle.toString(ctx, debug) + " at " + this.location.toString(ctx, debug);
    }

}
