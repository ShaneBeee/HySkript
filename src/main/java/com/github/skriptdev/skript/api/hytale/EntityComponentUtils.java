package com.github.skriptdev.skript.api.hytale;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jetbrains.annotations.Nullable;

/**
 * Quick utility class for accessing entity components.
 */
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

}
