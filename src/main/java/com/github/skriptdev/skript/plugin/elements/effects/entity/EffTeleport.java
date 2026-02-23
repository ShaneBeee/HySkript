package com.github.skriptdev.skript.plugin.elements.effects.entity;

import com.github.skriptdev.skript.api.hytale.utils.EntityUtils;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.NotNull;

public class EffTeleport extends Effect {

    public static void register(SkriptRegistration registration) {
        registration.newEffect(EffTeleport.class,
                "teleport %entities% to %location%")
            .name("Teleport")
            .description("Teleport entities to a location.")
            .examples("teleport all players to {_location}",
                "teleport player to first element of (respawn locations of player)")
            .since("1.0.0")
            .register();
    }

    private Expression<Entity> entities;
    private Expression<Location> location;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.entities = (Expression<Entity>) expressions[0];
        this.location = (Expression<Location>) expressions[1];
        return true;
    }

    @Override
    protected void execute(@NotNull TriggerContext ctx) {
        Entity[] entities = this.entities.getArray(ctx);
        Location location = this.location.getSingle(ctx).orElse(null);
        if (location == null || entities == null) return;

        World worldTp = Universe.get().getWorld(location.getWorld());

        for (Entity entity : entities) {
            Runnable tpRunnable = () -> {
                Teleport teleport = Teleport.createForPlayer(worldTp, location.getPosition(), location.getRotation());
                EntityUtils.putComponent(entity, Teleport.getComponentType(), teleport);
            };

            World world = entity.getWorld();
            assert world != null;
            if (world.isInThread()) {
                tpRunnable.run();
            } else {
                world.execute(tpRunnable);
            }
        }
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "teleport " + this.entities.toString(ctx, debug) + " to " + this.location.toString(ctx, debug);
    }

}
