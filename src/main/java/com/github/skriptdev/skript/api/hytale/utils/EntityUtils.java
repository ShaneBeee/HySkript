package com.github.skriptdev.skript.api.hytale.utils;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.syst3ms.skriptparser.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Quick utility class for accessing entity components.
 */
@SuppressWarnings("UnusedReturnValue")
public class EntityUtils {

    /**
     * Get the UUID of an {@link Entity}
     *
     * @param entity Entity to get UUID from
     * @return UUID of the entity, or null if the entity has no UUID component
     */
    public static @Nullable UUID getUUID(@NotNull Entity entity) {
        Ref<EntityStore> reference = entity.getReference();
        if (reference == null) return null;

        Store<EntityStore> store = reference.getStore();
        UUIDComponent component = store.getComponent(reference, UUIDComponent.getComponentType());
        if (component == null) return null;
        return component.getUuid();
    }

    /**
     * Get the name of an {@link Entity}.
     *
     * @param entity Entity to get name from
     * @return Name of the entity, or null if the entity has no name component
     */
    @SuppressWarnings("removal")
    public static @NotNull String getName(Entity entity) {
        Ref<EntityStore> reference = entity.getReference();
        if (reference == null) return "no-reference";

        Store<EntityStore> store = reference.getStore();
        Nameplate component = store.getComponent(reference, Nameplate.getComponentType());
        if (component != null) {
            return component.getText();
        }
        // REMOVAL (we shouldn't be using this as a backup)
        return entity.getLegacyDisplayName();
    }

    public static @NotNull String getVariableName(Entity entity) {
        UUID uuid = getUUID(entity);
        if (uuid == null) return "<unknown>";
        return uuid.toString();
    }

    /**
     * Set the name of an {@link Entity}.
     *
     * @param entity Entity to set name on
     * @param name   New name for the entity
     */
    public static void setNameplateName(Entity entity, @Nullable String name) {
        Ref<EntityStore> reference = entity.getReference();
        if (reference == null) return;

        Store<EntityStore> store = reference.getStore();
        if (name == null) {
            store.removeComponent(reference, Nameplate.getComponentType());
            return;
        }
        Nameplate component = store.getComponent(reference, Nameplate.getComponentType());
        if (component != null) {
            component.setText(name);
        } else {
            Nameplate n = new Nameplate(name);
            store.addComponent(reference, Nameplate.getComponentType(), n);
        }
    }

    /**
     * Get a component from an Entity
     *
     * @param entity     Entity to get component from
     * @param type       Component type to get
     * @param <ECS_TYPE> EntityStore Type
     * @param <T>        Type of returned component
     * @return Component from entity if available otherwise null
     */
    @SuppressWarnings("unchecked")
    public static <ECS_TYPE, T extends Component<ECS_TYPE>> @Nullable T getComponent(Entity entity, ComponentType<ECS_TYPE, T> type) {
        Ref<ECS_TYPE> reference = (Ref<ECS_TYPE>) entity.getReference();
        if (reference == null) return null;

        Store<ECS_TYPE> store = reference.getStore();
        return store.getComponent(reference, type);
    }

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
    public static @NotNull Pair<Entity, ItemComponent> dropItem(Store<EntityStore> store, ItemStack itemStack,
                                                                Location location, Vector3f velocity, float pickupDelay) {
        if (itemStack.isEmpty() || !itemStack.isValid()) {
            return new Pair<>(null, null);
        }

        Vector3d position = location.getPosition();
        Vector3f rotation = location.getRotation();

        Holder<EntityStore> itemEntityHolder = ItemComponent.generateItemDrop(store, itemStack, position, rotation,
            velocity.getX(), velocity.getY(), velocity.getZ());
        if (itemEntityHolder == null) {
            return new Pair<>(null, null);
        }

        ItemComponent itemComponent = itemEntityHolder.getComponent(ItemComponent.getComponentType());
        if (itemComponent != null) {
            itemComponent.setPickupDelay(pickupDelay);
        }

        store.addEntity(itemEntityHolder, AddReason.SPAWN);

        return new Pair<>(com.hypixel.hytale.server.core.entity.EntityUtils.getEntity(itemEntityHolder), itemComponent);
    }

}
