package com.github.skriptdev.skript.plugin.elements.expressions.entity;

import com.github.skriptdev.skript.api.hytale.utils.PlayerUtils;
import com.github.skriptdev.skript.api.skript.registration.NPCRegistry;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class ExprEntitiesInRadius implements Expression<Entity> {

    public static void register(SkriptRegistration reg) {
        reg.newExpression(ExprEntitiesInRadius.class, Entity.class, false,
                "[all] (entities|:players) in radius %number% (around|of) %location%",
                "[all] entities of type[s] %npcroles% in radius %number% (around|of) %location%",
                "[all] (entities|:players) within %location% and %location%",
                "[all] entities of type[s] %npcroles% within %location% and %location%")
            .name("Entities in Radius/Cuboid")
            .description("Get all entities/players within a radius around a location or within a cuboid.",
                "You can optionally include an NPCRole to filter the entities by type.")
            .examples("loop entities in radius 10 around player:",
                "\tif npctype of loop-entity is sheep:",
                "\t\tkill loop-entity",
                "loop entities of type cow in radius 5 around {_loc}:",
                "loop entities within {_loc1} and {_loc2}:",
                "loop entities of type sheep within {_loc1} and {_loc2}",
                "send \"hi\" to all players in radius 10 of player")
            .since("1.0.0")
            .register();
    }

    private int pattern;
    private boolean players;
    private Expression<Number> radius;
    private Expression<Location> location;
    private Expression<Location> loc1, loc2;
    private Expression<NPCRegistry.NPCRole> roles;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.pattern = matchedPattern;
        this.players = parseContext.hasMark("players");
        switch (matchedPattern) {
            case 0 -> {
                this.radius = (Expression<Number>) expressions[0];
                this.location = (Expression<Location>) expressions[1];
            }
            case 1 -> {
                this.roles = (Expression<NPCRegistry.NPCRole>) expressions[0];
                this.radius = (Expression<Number>) expressions[1];
                this.location = (Expression<Location>) expressions[2];
            }
            case 2 -> {
                this.loc1 = (Expression<Location>) expressions[0];
                this.loc2 = (Expression<Location>) expressions[1];
            }
            case 3 -> {
                this.roles = (Expression<NPCRegistry.NPCRole>) expressions[0];
                this.loc1 = (Expression<Location>) expressions[1];
                this.loc2 = (Expression<Location>) expressions[2];
            }
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Entity[] getValues(@NotNull TriggerContext ctx) {
        List<Entity> entities = new ArrayList<>();

        List<NPCRegistry.NPCRole> roles = new ArrayList<>();
        if (this.roles != null) {
            roles.addAll(Arrays.asList(this.roles.getArray(ctx)));
        }

        Predicate<Entity> rolePredicate = entity -> {
            if (roles.isEmpty()) return true;
            if (entity instanceof NPCEntity npcEntity) {
                return roles.contains(NPCRegistry.getByIndex(npcEntity.getRoleIndex()));
            }
            return false;
        };

        if (this.location != null) {
            Location loc = this.location.getSingle(ctx).orElse(null);
            if (loc == null) return null;

            Number number = this.radius.getSingle(ctx).orElse(null);
            if (number == null) return null;

            double radius = number.doubleValue();

            String worldName = loc.getWorld();
            if (worldName == null) return null;

            World world = Universe.get().getWorld(worldName);
            if (world == null) return null;
            Store<EntityStore> store = world.getEntityStore().getStore();

            if (this.players) {
                return PlayerUtils.getPlayersInRadius(loc, radius).toArray(Player[]::new);
            }

            for (Ref<EntityStore> entityStoreRef : TargetUtil.getAllEntitiesInSphere(loc.getPosition(), radius, store)) {
                Entity entity = EntityUtils.getEntity(entityStoreRef, store);
                if (entity == null) continue;
                if (!rolePredicate.test(entity)) continue;
                entities.add(entity);
            }
        } else {
            Location loc1 = this.loc1.getSingle(ctx).orElse(null);
            Location loc2 = this.loc2.getSingle(ctx).orElse(null);
            if (loc1 == null || loc2 == null) return null;

            String worldName = loc1.getWorld();
            if (worldName == null) return null;

            World world = Universe.get().getWorld(worldName);
            if (world == null) return null;
            Store<EntityStore> store = world.getEntityStore().getStore();

            if (this.players) {
                return PlayerUtils.getPlayersInCuboid(loc1, loc2).toArray(Player[]::new);
            }

            Vector3d min = Vector3d.min(loc1.getPosition(), loc2.getPosition());
            Vector3d max = Vector3d.max(loc1.getPosition(), loc2.getPosition());

            for (Ref<EntityStore> allEntitiesInBox : TargetUtil.getAllEntitiesInBox(min, max, store)) {
                Entity entity = EntityUtils.getEntity(allEntitiesInBox, store);
                if (entity == null) continue;
                if (!rolePredicate.test(entity)) continue;
                entities.add(entity);
            }
        }

        return entities.toArray(Entity[]::new);
    }

    @Override
    public Class<? extends Entity> getReturnType() {
        return this.players ? Player.class : Entity.class;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String type = this.players ? "players" : "entities";
        return switch (this.pattern) {
            case 0 ->
                type + "in radius " + this.radius.toString(ctx, debug) + " around " + this.location.toString(ctx, debug);
            case 1 ->
                "entities of type " + this.roles.toString(ctx, debug) + " in radius " + this.radius.toString(ctx, debug) +
                    " around " + this.location.toString(ctx, debug);
            case 2 -> type + " within " + this.loc1.toString(ctx, debug) + " and " + this.loc2.toString(ctx, debug);
            case 3 ->
                "entities of type " + this.roles.toString(ctx, debug) + " within " + this.loc1.toString(ctx, debug) +
                    " and " + this.loc2.toString(ctx, debug);
            default -> null;
        };
    }

}
