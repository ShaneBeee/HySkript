package com.github.skriptdev.skript.plugin.elements.types;

import com.github.skriptdev.skript.api.skript.registration.EnumRegistry;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class TypesPlayer {

    public static void register(SkriptRegistration reg) {
        // Keep these in alphabetical order
        EnumRegistry.register(reg, GameMode.class, "gamemode", "gamemode@s")
            .name("GameMode")
            .description("Represents a game mode.")
            .since("INSERT VERSION")
            .register();
        reg.newType(Player.class, "player", "player@s")
            .name("Player")
            .description("Represents a player in the game.")
            .since("1.0.0")
            .toStringFunction(Player::getDisplayName)
            .register();
        reg.newType(PlayerRef.class, "playerref", "playerRef@s")
            .name("Player Ref")
            .description("Represents a reference to a player in the game.")
            .since("1.0.0")
            .toStringFunction(PlayerRef::getUsername)
            .register();
    }

}
