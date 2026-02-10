package com.github.skriptdev.skript.plugin.elements.effects.entity;

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

        World world = Universe.get().getWorld(location.getWorld());

        for (Entity entity : entities) {
            Ref<EntityStore> reference = entity.getReference();
            assert reference != null;
            Store<EntityStore> store = reference.getStore();

            Teleport teleport = Teleport.createForPlayer(world, location.getPosition(), location.getRotation());
            store.addComponent(reference, Teleport.getComponentType(), teleport);
        }
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "teleport " + this.entities.toString(ctx, debug) + " to " + this.location.toString(ctx, debug);
    }

}
