package com.github.skriptdev.skript.api.hytale.utils;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilities for {@link EntityStore} {@link Ref References}
 */
@SuppressWarnings("unused")
public class EntityReferenceUtils {

    private static final List<ReferenceType<?>> TYPES = new ArrayList<>();
    private static final Map<Class<? extends Component<?>>, ReferenceType<?>> TYPES_MAP = new HashMap<>();

    public static final ReferenceType<Player> PLAYER = new ReferenceType<>(
        "player", Player.class, Player.getComponentType());
    public static final ReferenceType<NPCEntity> NPC_ENTITY = new ReferenceType<>(
        "npcentity", NPCEntity.class, NPCEntity.getComponentType());
    public static final ReferenceType<ItemComponent> ITEM = new ReferenceType<>(
        "item", ItemComponent.class, ItemComponent.getComponentType());

    public static List<ReferenceType<?>> getTypes() {
        return TYPES;
    }

    public static ReferenceType<?> getType(Class<? extends Component<?>> componentClass) {
        return TYPES_MAP.get(componentClass);
    }

    public static List<Ref<EntityStore>> getRefsInSphere(@Nonnull Vector3d pos, double radius, @Nonnull Store<EntityStore> store) {
        ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
        EntityModule entityModule = EntityModule.get();
        SpatialResource<Ref<EntityStore>, EntityStore> entities = store.getResource(entityModule.getEntitySpatialResourceType());
        entities.getSpatialStructure().collect(pos, (float) radius, results);
        SpatialResource<Ref<EntityStore>, EntityStore> players = store.getResource(entityModule.getPlayerSpatialResourceType());
        players.getSpatialStructure().collect(pos, (float) radius, results);
        SpatialResource<Ref<EntityStore>, EntityStore> items = store.getResource(entityModule.getItemSpatialResourceType());
        items.getSpatialStructure().collect(pos, (float) radius, results);
        return results;
    }

    @Nonnull
    public static List<Ref<EntityStore>> getRefsInBox(@Nonnull Vector3d min, @Nonnull Vector3d max, @Nonnull Store<EntityStore> store) {
        ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
        EntityModule entityModule = EntityModule.get();
        SpatialResource<Ref<EntityStore>, EntityStore> entities = store.getResource(entityModule.getEntitySpatialResourceType());
        entities.getSpatialStructure().collectBox(min, max, results);
        SpatialResource<Ref<EntityStore>, EntityStore> players = store.getResource(entityModule.getPlayerSpatialResourceType());
        players.getSpatialStructure().collectBox(min, max, results);
        SpatialResource<Ref<EntityStore>, EntityStore> items = store.getResource(entityModule.getItemSpatialResourceType());
        items.getSpatialStructure().collectBox(min, max, results);
        return results;
    }

    public static class ReferenceType<E extends Component<EntityStore>> {


        private final String name;
        private final Class<E> componentClass;
        private final ComponentType<EntityStore, ?> componentType;

        public ReferenceType(String name, Class<E> componentClass, ComponentType<EntityStore, ?> componentType) {
            this.name = name;
            this.componentClass = componentClass;
            this.componentType = componentType;
            EntityReferenceUtils.TYPES.add(this);
            EntityReferenceUtils.TYPES_MAP.put(componentClass, this);
        }

        public String getName() {
            return this.name;
        }

        public Class<E> getComponentClass() {
            return this.componentClass;
        }

        @SuppressWarnings("unchecked")
        public @Nullable E getComponent(Ref<EntityStore> ref) {
            Store<EntityStore> store = ref.getStore();
            return (E) store.getComponent(ref, this.componentType);
        }
    }

}
