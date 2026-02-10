package com.github.skriptdev.skript.plugin.elements.expressions.player;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerConfigData;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerRespawnPointData;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerWorldData;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ExprPlayerRespawnLocations implements Expression<Location> {

    public static void register(SkriptRegistration reg) {
        reg.newExpression(ExprPlayerRespawnLocations.class, Location.class, false,
                "[player] respawn locations of %player%",
                "[player] respawn locations of %player% in %worlds%")
            .name("Player Respawn Locations")
            .description("Get the respawn locations of a player.",
                "If world is not specified, locations in all worlds are returned.")
            .examples("set {_locs::*} to respawn locations of player",
                "set {_locs::*} to respawn locations of player in world of player",
                "teleport player to random element out of respawn locations of player in world of player")
            .since("1.0.0")
            .register();
    }

    private Expression<Player> player;
    private Expression<World> worlds;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.player = (Expression<Player>) expressions[0];
        if (matchedPattern == 1) this.worlds = (Expression<World>) expressions[1];
        return true;
    }

    @Override
    public Location[] getValues(@NotNull TriggerContext ctx) {
        Optional<? extends Player> single = this.player.getSingle(ctx);
        if (single.isEmpty()) return null;

        Player player = single.get();
        PlayerConfigData playerConfigData = player.getPlayerConfigData();
        Map<String, PlayerWorldData> perWorldData = playerConfigData.getPerWorldData();

        List<World> worlds = new ArrayList<>();
        if (this.worlds != null) {
            Collections.addAll(worlds, this.worlds.getArray(ctx));
        } else {
            Universe.get().getWorlds().forEach((_, world) -> worlds.add(world));
        }

        List<Location> spawns = new ArrayList<>();
        for (World world : worlds) {
            PlayerWorldData worldData = perWorldData.get(world.getName());
            if (worldData != null) {
                PlayerRespawnPointData[] respawnPoints = worldData.getRespawnPoints();
                if (respawnPoints == null || respawnPoints.length == 0) continue;
                for (PlayerRespawnPointData respawnPoint : worldData.getRespawnPoints()) {
                    Vector3d respawnPosition = respawnPoint.getRespawnPosition();
                    Location location = new Location(world.getName(), respawnPosition);
                    spawns.add(location);
                }
            }
        }

        return spawns.toArray(new Location[0]);
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String world = this.worlds == null ? "" : " in " + this.worlds.toString(ctx, debug);
        return "player respawn locations of " + this.player.toString(ctx, debug) + world;
    }

}
