package com.github.skriptdev.skript.api.hytale;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.syst3ms.skriptparser.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Quick utility class for accessing entity components.
 */
@SuppressWarnings("UnusedReturnValue")
public class EntityComponentUtils {

    /**
     * Get the EntityStatMap component of an entity.
     *
     * @param entity Entity to get component from
     * @return EntityStatMap component of the entity, or null if not found
     */
    public static @Nullable EntityStatMap getEntityStatMap(LivingEntity entity) {
        World world = entity.getWorld();
        if (world == null) return null;

        Store<EntityStore> store = world.getEntityStore().getStore();
        Ref<EntityStore> reference = entity.getReference();
        if (reference == null) return null;

        return store.getComponent(reference, EntityStatsModule.get().getEntityStatMapComponentType());
    }

    /**
     * Get the MovementStatesComponent of an entity.
     *
     * @param entity Entity to get component from
     * @return MovementStatesComponent of the entity, or null if not found
     */
    public static @Nullable MovementStatesComponent getMovementStatesComponent(Entity entity) {
        Ref<EntityStore> reference = entity.getReference();
        if (reference == null) return null;

        Store<EntityStore> store = reference.getStore();
        return store.getComponent(reference, MovementStatesComponent.getComponentType());
    }

    @SuppressWarnings({"DataFlowIssue", "deprecation"})
    public static @NotNull Pair<Entity, ItemComponent> dropItem(Store<EntityStore> store, ItemStack itemStack, Location location, Vector3f velocity, float pickupDelay) {
        if (itemStack.isEmpty() || !itemStack.isValid()) {
            return new Pair<>(null, null);
        }

        Vector3d position = location.getPosition();
        Vector3f rotation = location.getRotation();

        Holder<EntityStore> itemEntityHolder = ItemComponent.generateItemDrop(store, itemStack, position, rotation, velocity.getX(), velocity.getY(), velocity.getZ());
        if (itemEntityHolder == null) {
            return new Pair<>(null, null);
        }
        ;

        ItemComponent itemComponent = itemEntityHolder.getComponent(ItemComponent.getComponentType());
        if (itemComponent != null) {
            itemComponent.setPickupDelay(pickupDelay);
        }

        store.addEntity(itemEntityHolder, AddReason.SPAWN);

        return new Pair<>(EntityUtils.getEntity(itemEntityHolder), itemComponent);
    }

}
