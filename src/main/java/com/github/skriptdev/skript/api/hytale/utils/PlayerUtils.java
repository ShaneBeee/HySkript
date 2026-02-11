package com.github.skriptdev.skript.api.hytale.utils;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
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

}
