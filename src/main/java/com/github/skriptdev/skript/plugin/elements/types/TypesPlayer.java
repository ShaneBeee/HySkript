package com.github.skriptdev.skript.plugin.elements.types;

import com.github.skriptdev.skript.api.hytale.utils.EntityUtils;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.UUID;

public class TypesPlayer {

    public static void register(SkriptRegistration reg) {
        // Keep these in alphabetical order
        reg.newEnumType(GameMode.class, "gamemode", "gamemode@s")
            .name("GameMode")
            .description("Represents a game mode.")
            .since("1.0.0")
            .register();
        reg.newType(Player.class, "player", "player@s")
            .name("Player")
            .description("Represents a player in the game.")
            .since("1.0.0")
            .toStringFunction(Player::getDisplayName)
            .toVariableNameFunction(player -> {
                UUID uuid = EntityUtils.getUUID(player);
                assert uuid != null; // A player's UUID should never be null
                return uuid.toString();
            })
            .register();
        reg.newType(PlayerRef.class, "playerref", "playerRef@s")
            .name("Player Ref")
            .description("Represents a reference to a player in the game.")
            .since("1.0.0")
            .toStringFunction(PlayerRef::getUsername)
            .register();
    }

}
