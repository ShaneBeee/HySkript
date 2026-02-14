package com.github.skriptdev.skript.plugin.elements.effects.world;

import com.github.skriptdev.skript.api.hytale.utils.StoreUtils;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemTool;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemToolSpec;
import com.hypixel.hytale.server.core.entity.ExplosionConfig;
import com.hypixel.hytale.server.core.entity.ExplosionUtils;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.ExplodeInteraction;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class EffExplosion extends Effect {

    public static void register(SkriptRegistration reg) {
        reg.newEffect(EffExplosion.class,
                "create explosion at %locations%",
                "create explosion with radius %number% at %locations%",
                "create explosion with block radius %number% [and] with entity radius %number% at %locations%")
            .name("Explosion")
            .description("Creates an explosion at the specified location(s) with " +
                "customizable block and entity damage radii.")
            .examples("create explosion at {location}",
                "create explosion with radius 10 at {location}",
                "create explosion with block radius 10 and with entity radius 15 at {location}")
            .experimental("Currently no sound/particles.")
            .since("1.0.0")
            .register();
    }

    private int pattern;
    private Expression<Location> locations;
    private Expression<Number> blockRadius;
    private Expression<Number> entityRadius;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.pattern = matchedPattern;
        this.locations = (Expression<Location>) expressions[matchedPattern];
        if (matchedPattern == 1) {
            this.blockRadius = (Expression<Number>) expressions[0];
        } else if (matchedPattern == 2) {
            this.blockRadius = (Expression<Number>) expressions[0];
            this.entityRadius = (Expression<Number>) expressions[1];
        }
        return true;
    }

    @Override
    protected void execute(@NotNull TriggerContext ctx) {
        Number blockRadius = 3.0f;
        Number entityRadius = 5.0f;
        if (this.blockRadius != null) {
            Optional<? extends Number> single = this.blockRadius.getSingle(ctx);
            if (single.isPresent()) {
                blockRadius = single.get();

                // If no entity radius is provided, we just add 2 to the bock radius
                // This will be fixed below if an entity radius IS provided
                entityRadius = blockRadius.floatValue() + 2.0f;
            }
        }

        if (this.entityRadius != null) {
            Optional<? extends Number> single = this.entityRadius.getSingle(ctx);
            if (single.isPresent()) {
                entityRadius = single.get();
            }
        }

        Config config = new Config(blockRadius, entityRadius);

        for (Location location : this.locations.getArray(ctx)) {
            String worldName = location.getWorld();
            if (worldName == null) continue;

            World world = Universe.get().getWorld(worldName);
            if (world == null) continue;

            Runnable worldRunnable = () -> {
                Store<EntityStore> entityStore = world.getEntityStore().getStore();
                Store<ChunkStore> chunkStoreStore = world.getChunkStore().getStore();
                CommandBuffer<EntityStore> commandBuffer = StoreUtils.getCommandBuffer(entityStore);

                ExplosionUtils.performExplosion(ExplodeInteraction.DAMAGE_SOURCE_EXPLOSION,
                    location.getPosition(),
                    config,
                    null,
                    commandBuffer,
                    chunkStoreStore);
            };

            if (world.isInThread()) {
                worldRunnable.run();
            } else {
                world.execute(worldRunnable);
            }
        }
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return switch (this.pattern) {
            case 1 ->
                "create explosion with radius " + this.blockRadius.toString(ctx, debug) + " at " + this.locations.toString(ctx, debug);
            case 2 ->
                "create explosion with block radius " + this.blockRadius.toString(ctx, debug) + " and entity radius "
                    + this.entityRadius.toString(ctx, debug) + " at " + this.locations.toString(ctx, debug);
            default -> "create explosion at " + this.locations.toString(ctx, debug);
        };
    }

    private static class Config extends ExplosionConfig {

        private Config(Number blockRadius, Number entityRadius) {
            this.blockDamageRadius = blockRadius.intValue();
            this.entityDamageRadius = entityRadius.floatValue();
            // The stuff you have to do to make things work in Hytale...
            ItemToolSpec[] specs = new ItemToolSpec[]{
                new ItemToolSpec("SoftBlocks", 2, 1),
                new ItemToolSpec("Soils", 2, 1),
                new ItemToolSpec("Woods", 2, 1),
                new ItemToolSpec("Rocks", 2, 1),
                new ItemToolSpec("Benches", 2, 1),
                new ItemToolSpec("VolcanicRocks", 0.001f, 1),
            };
            this.itemTool = new ItemTool(specs, 1, null);
        }
    }

}
