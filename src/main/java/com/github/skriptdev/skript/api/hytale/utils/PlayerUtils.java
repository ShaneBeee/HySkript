package com.github.skriptdev.skript.api.hytale.utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Utilities for {@link Player Players}
 */
public class PlayerUtils {

    /**
     * Get all players in a specific world or all worlds.
     *
     * @param world World to get players from (can be null to get players from all worlds)
     * @return List of players
     */
    public static List<Player> getPlayers(@Nullable World world) {
        if (world != null) {
            if (!world.isInThread()) {
                return !world.isStarted() ? List.of() : CompletableFuture.supplyAsync(() -> getPlayers(world), world).join();
            } else {
                List<Player> players = new ArrayList<>();
                world.getEntityStore().getStore().forEachChunk(Player.getComponentType(), (archetypeChunk, commandBuffer) -> {
                    for (int index = 0; index < archetypeChunk.size(); ++index) {
                        players.add(archetypeChunk.getComponent(index, Player.getComponentType()));
                    }
                });
                return players;
            }
        } else {
            List<Player> players = new ArrayList<>();
            Universe.get().getWorlds().forEach((s, world1) -> players.addAll(getPlayers(world1)));
            return players;
        }
    }

    /**
     * Get all PlayerRefs in a specific world or all worlds.
     *
     * @param world World to get PlayerRefs from (can be null to get PlayerRefs from all worlds)
     * @return List of PlayerRefs
     */
    public static List<PlayerRef> getPlayerRefs(@Nullable World world) {
        if (world != null) {
            if (!world.isInThread()) {
                return !world.isStarted() ? List.of() : CompletableFuture.supplyAsync(() -> getPlayerRefs(world), world).join();
            } else {
                return new ArrayList<>(world.getPlayerRefs());
            }
        } else {
            List<PlayerRef> players = new ArrayList<>();
            Universe.get().getWorlds().forEach((s, world1) -> players.addAll(getPlayerRefs(world1)));
            return players;
        }
    }

    /**
     * Get a PlayerRef from a Player.
     *
     * @param player Player to get PlayerRef from
     * @return PlayerRef from Player or null if not found
     */
    public static @Nullable PlayerRef getPlayerRef(Player player) {
        World world = player.getWorld();
        assert world != null;

        Ref<EntityStore> reference = player.getReference();
        assert reference != null;

        Store<EntityStore> store = world.getEntityStore().getStore();
        return store.getComponent(reference, PlayerRef.getComponentType());
    }

    /**
     * Get a Player from a PlayerRef.
     *
     * @param playerRef PlayerRef to get Player from
     * @param world     World to get Player from
     * @return Player from PlayerRef or null if not found
     */
    public static @Nullable Player getPlayer(PlayerRef playerRef, World world) {
        Ref<EntityStore> reference = playerRef.getReference();
        if (reference == null) return null;

        Store<EntityStore> store = world.getEntityStore().getStore();
        return store.getComponent(reference, Player.getComponentType());
    }

    /**
     * Get all players within a radius of a location.
     *
     * @param location Location to get players from
     * @param radius   Radius to get players from
     * @return List of players within radius of location
     */
    public static @NotNull List<Player> getPlayersInRadius(Location location, double radius) {
        World world = Universe.get().getWorld(location.getWorld());
        if (world == null) return List.of();

        Store<EntityStore> store = world.getEntityStore().getStore();
        if (store == null) return List.of();

        ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
        SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = store.getResource(EntityModule.get()
            .getPlayerSpatialResourceType());
        playerSpatialResource.getSpatialStructure().collect(location.getPosition(), (float) radius, results);

        List<Player> players = new ArrayList<>();
        for (Ref<EntityStore> result : results) {
            Player player = store.getComponent(result, Player.getComponentType());
            if (player != null) players.add(player);
        }
        return players;
    }

    /**
     * Get all players within a cuboid.
     *
     * @param loc1 First location of cuboid
     * @param loc2 Second location of cuboid
     * @return List of players within cuboid
     */
    public static @NotNull List<Player> getPlayersInCuboid(Location loc1, Location loc2) {
        World world = Universe.get().getWorld(loc1.getWorld());
        if (world == null) return List.of();

        Store<EntityStore> store = world.getEntityStore().getStore();
        if (store == null) return List.of();

        Vector3d min = Vector3d.min(loc1.getPosition(), loc2.getPosition());
        Vector3d max = Vector3d.max(loc1.getPosition(), loc2.getPosition());

        ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
        SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = store.getResource(EntityModule.get()
            .getPlayerSpatialResourceType());
        playerSpatialResource.getSpatialStructure().collectBox(min, max, results);

        List<Player> players = new ArrayList<>();
        for (Ref<EntityStore> result : results) {
            Player player = store.getComponent(result, Player.getComponentType());
            if (player != null) players.add(player);
        }
        return players;
    }

}
