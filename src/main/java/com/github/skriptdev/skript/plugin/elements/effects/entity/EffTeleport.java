package com.github.skriptdev.skript.plugin.elements.effects.entity;

import com.github.skriptdev.skript.api.hytale.utils.EntityUtils;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.NotNull;

public class EffTeleport extends Effect {

    public static void register(SkriptRegistration registration) {
        registration.newEffect(EffTeleport.class,
                "teleport %refs/entities% to %location%")
            .name("Teleport")
            .description("Teleport entities/references to a location.")
            .examples("teleport all players to {_location}",
                "teleport player to first element of (respawn locations of player)")
            .since("1.0.0")
            .register();
    }

    private Expression<?> entities;
    private Expression<Location> location;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.entities = expressions[0];
        this.location = (Expression<Location>) expressions[1];
        return true;
    }

    @SuppressWarnings({"rawtypes", "unchecked", "ConstantValue"})
    @Override
    protected void execute(@NotNull TriggerContext ctx) {
        Object[] objects = this.entities.getArray(ctx);
        Location location = this.location.getSingle(ctx).orElse(null);
        if (location == null || objects == null) return;

        World worldTo = Universe.get().getWorld(location.getWorld());

        for (Object object : objects) {
            Teleport teleport = Teleport.createForPlayer(worldTo, location.getPosition(), location.getRotation());

            World worldFrom;
            if (object instanceof Entity entity) {
                worldFrom = entity.getWorld();
            } else if (object instanceof Ref ref) {
                Store store = ref.getStore();
                worldFrom = ((Store<EntityStore>) store).getExternalData().getWorld();
            } else {
                continue;
            }

            Runnable tpRunnable = () -> {
                if (object instanceof Entity entity) {
                    EntityUtils.addComponent(entity, Teleport.getComponentType(), teleport);
                } else if (object instanceof Ref ref) {
                    Store store = ref.getStore();
                    store.addComponent(ref, Teleport.getComponentType(), teleport);
                }
            };

            if (worldFrom == null) {
                continue;
            }

            if (worldFrom.isInThread()) {
                tpRunnable.run();
            } else {
                worldFrom.execute(tpRunnable);
            }
        }
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "teleport " + this.entities.toString(ctx, debug) + " to " + this.location.toString(ctx, debug);
    }

}
