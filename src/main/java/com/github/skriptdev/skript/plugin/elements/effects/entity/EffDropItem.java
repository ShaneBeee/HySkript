package com.github.skriptdev.skript.plugin.elements.effects.entity;

import com.github.skriptdev.skript.api.hytale.utils.EntityUtils;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EffDropItem extends Effect {

    public static void register(SkriptRegistration reg) {
        reg.newEffect(EffDropItem.class,
                "drop %items/itemstacks% at %location%",
                "drop %items/itemstacks% at %location% with pickup delay %duration%",
                "drop %items/itemstacks% at %location% with velocity %vector3f%",
                "drop %items/itemstacks% at %location% with velocity %vector3f% [and] [with] pickup delay %duration%")
            .name("Drop Item")
            .description("Drops the specified items.")
            .examples("drop ingredient_poop at location of player",
                "drop {_itemstack} at location of player with pickup delay 10 seconds",
                "drop {_i} at location of player with velocity vector3f(0,1,0) and with pickup delay 5 seconds")
            .since("1.0.0")
            .register();
    }

    private Expression<?> items;
    private Expression<Location> location;
    private Expression<Duration> pickupDelay;
    private Expression<Vector3f> velocity;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.items = expressions[0];
        this.location = (Expression<Location>) expressions[1];
        if (matchedPattern == 1) {
            this.pickupDelay = (Expression<Duration>) expressions[2];
        } else if (matchedPattern == 2) {
            this.velocity = (Expression<Vector3f>) expressions[2];
        } else if (matchedPattern == 3) {
            this.velocity = (Expression<Vector3f>) expressions[2];
            this.pickupDelay = (Expression<Duration>) expressions[3];
        }
        return true;
    }

    @Override
    protected void execute(@NotNull TriggerContext ctx) {
        Location location = this.location.getSingle(ctx).orElse(null);
        if (location == null) return;

        String worldName = location.getWorld();
        World world = Universe.get().getWorld(worldName);
        if (world == null) return;

        Store<EntityStore> store = world.getEntityStore().getStore();

        List<ItemStack> itemStacks = new ArrayList<>();
        for (Object o : this.items.getArray(ctx)) {
            if (o instanceof ItemStack itemStack) {
                itemStacks.add(itemStack);
            } else if (o instanceof Item item) {
                ItemStack itemStack = new ItemStack(item.getId());
                itemStacks.add(itemStack);
            }
        }

        Vector3f velocity;
        if (this.velocity != null) {
            Optional<? extends Vector3f> single = this.velocity.getSingle(ctx);
            if (single.isPresent()) {
                velocity = single.get();
            } else {
                velocity = Vector3f.ZERO;
            }
        } else {
            velocity = Vector3f.ZERO;
        }

        float pickupDelay;
        if (this.pickupDelay != null) {
            Optional<? extends Duration> single = this.pickupDelay.getSingle(ctx);
            if (single.isPresent()) {
                Duration duration = single.get();
                pickupDelay = duration.toMillis() / 1000.0f;
            } else {
                pickupDelay = 0;
            }
        } else {
            pickupDelay = 0f;
        }

        if (world.isInThread()) {
            world.execute(() -> {
                for (ItemStack itemStack : itemStacks) {
                    EntityUtils.dropItem(store, itemStack, location, velocity, pickupDelay);
                }
            });
        } else {
            for (ItemStack itemStack : itemStacks) {
                EntityUtils.dropItem(store, itemStack, location, velocity, pickupDelay);
            }
        }
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String velocity = this.velocity != null ? " with velocity " + this.velocity.toString(ctx, debug) : "";
        String pickupDelay = this.pickupDelay != null ? " with pickup delay " + this.pickupDelay.toString(ctx, debug) : "";
        return "drop " + this.items.toString(ctx, debug) + " at " + this.location.toString(ctx, debug) + velocity + pickupDelay;
    }

}
