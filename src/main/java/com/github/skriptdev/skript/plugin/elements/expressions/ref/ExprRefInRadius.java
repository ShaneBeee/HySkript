package com.github.skriptdev.skript.plugin.elements.expressions.ref;

import com.github.skriptdev.skript.api.hytale.utils.EntityReferenceUtils;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class ExprRefInRadius implements Expression<Ref> {

    public static void register(SkriptRegistration reg) {
        reg.newExpression(ExprRefInRadius.class, Ref.class, false,
                "[all] (refs|references) in radius %number% (around|of) %location%",
                "[all] (refs|references) within %location% and %location%")
            .name("References in Radius/Cuboid")
            .description("Get all references within a radius around a location or within a cuboid.")
            .examples("loop refs in radius 10 around player:",
                "\tif item component of loop-value is set:",
                "\t\tteleport loop-ref to player")
            .since("1.2.0")
            .register();
    }

    private Expression<Number> radius;
    private Expression<Location> location;
    private Expression<Location> loc1, loc2;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        if (matchedPattern == 0) {
            this.radius = (Expression<Number>) expressions[0];
            this.location = (Expression<Location>) expressions[1];
        } else if (matchedPattern == 1) {
            this.loc1 = (Expression<Location>) expressions[0];
            this.loc2 = (Expression<Location>) expressions[1];
        } else {
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Ref<EntityStore>[] getValues(@NotNull TriggerContext ctx) {
        List<Ref<EntityStore>> refs = new ArrayList<>();
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

            List<Ref<EntityStore>> refsInSphere = EntityReferenceUtils.getRefsInSphere(loc.getPosition(), radius, store);
            refs.addAll(refsInSphere);
        } else {
            Location loc1 = this.loc1.getSingle(ctx).orElse(null);
            Location loc2 = this.loc2.getSingle(ctx).orElse(null);
            if (loc1 == null || loc2 == null) return null;

            String worldName = loc1.getWorld();
            if (worldName == null) return null;

            World world = Universe.get().getWorld(worldName);
            if (world == null) return null;
            Store<EntityStore> store = world.getEntityStore().getStore();

            Vector3d min = Vector3d.min(loc1.getPosition(), loc2.getPosition());
            Vector3d max = Vector3d.max(loc1.getPosition(), loc2.getPosition());

            List<Ref<EntityStore>> refsInBox = EntityReferenceUtils.getRefsInBox(min, max, store);
            refs.addAll(refsInBox);
        }

        return refs.toArray(new Ref[0]);
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        if (this.location != null) {
            return "all refs in radius " + this.radius.toString(ctx, debug) + " around " + this.location.toString(ctx, debug);
        } else {
            return "all refs within " + this.loc1.toString(ctx, debug) + " and " + this.loc2.toString(ctx, debug);
        }
    }

}
