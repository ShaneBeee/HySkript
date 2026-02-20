package com.github.skriptdev.skript.plugin.elements.effects.entity;

import com.github.skriptdev.skript.api.hytale.Block;
import com.github.skriptdev.skript.api.hytale.utils.EntityUtils;
import com.github.skriptdev.skript.api.hytale.utils.StoreUtils;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.MountController;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

public class EffRide extends Effect {

    public static void register(SkriptRegistration reg) {
        reg.newEffect(EffRide.class, "make %livingentity% (ride|mount) %entity/block%",
                "make %livingentity% (ride|mount) %entity/block% with offset %vector3f%",
                "dismount %livingentity%")
            .name("Mount/Dismount Entity")
            .description("Make an entity ride/mount an entity/block, or make them stop riding.")
            .experimental("Currently block mounting is not working. " +
                "Steering does not appear to work (At least on the sheep I tested on).")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<LivingEntity> passenger;
    private Expression<?> vehicle;
    private Expression<Vector3f> offset;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.passenger = (Expression<LivingEntity>) expressions[0];
        if (matchedPattern < 2) {
            this.vehicle = expressions[1];
        }
        if (matchedPattern == 1) {
            this.offset = (Expression<Vector3f>) expressions[2];
        }
        return true;
    }

    @Override
    protected void execute(@NotNull TriggerContext ctx) {
        LivingEntity passenger = this.passenger.getSingle(ctx).orElse(null);
        if (passenger == null) {
            return;
        }
        if (this.vehicle == null) {
            EntityUtils.tryRemoveComponent(passenger, MountedComponent.getComponentType());
            return;
        }
        Object vehicle = this.vehicle.getSingle(ctx).orElse(null);
        if (vehicle == null) {
            return;
        }

        MountedComponent component = EntityUtils.getComponent(passenger, MountedComponent.getComponentType());
        // Already mounted
        if (component != null) return;

        Vector3f offset = Vector3f.ZERO;
        if (this.offset != null) {
            Vector3f vector3f = this.offset.getSingle(ctx).orElse(null);
            if (vector3f != null) offset = vector3f;
        }

        if (vehicle instanceof Entity entity) {
            Ref<EntityStore> vehicleRef = entity.getReference();

            MountedComponent comp = new MountedComponent(vehicleRef, offset, MountController.Minecart);
            EntityUtils.putComponent(passenger, MountedComponent.getComponentType(), comp);
        } else if (vehicle instanceof Block block) {

            Ref<EntityStore> passengerRef = passenger.getReference();
            if (passengerRef == null) return;

            Store<EntityStore> store = passengerRef.getStore();
            CommandBuffer<EntityStore> commandBuffer = StoreUtils.getCommandBuffer(store);

            Vector3i pos = block.getPos();
            Vector3f add = pos.toVector3f().add(offset);

            // TODO broken, figure this out
            //BlockMountAPI.mountOnBlock(passengerRef, commandBuffer, pos, add);
        }
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String offset = this.offset != null ? " with offset " + this.offset.toString(ctx, debug) : "";
        return "make " + this.passenger.toString(ctx, debug) + " ride " + this.vehicle.toString(ctx, debug) + offset;
    }

}
