package com.github.skriptdev.skript.plugin.elements.types;

import com.github.skriptdev.skript.api.skript.registration.NPCRegistry;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

public class TypesEntity {

    static void register(SkriptRegistration registration) {
        registration.newType(NPCRegistry.NPCRole.class, "npcrole", "npcrole@s")
            .name("NPC Role")
            .description("Represents the type of NPCs in the game.")
            .examples("coming soon") // TODO
            .usage(NPCRegistry.getTypeUsage())
            .since("1.0.0")
            .toStringFunction(NPCRegistry.NPCRole::name)
            .literalParser(NPCRegistry::parse)
            .register();
        registration.newType(Entity.class, "entity", "entit@y@ies")
            .toStringFunction(Entity::toString) // TODO get its name or something
            .name("Entity")
            .description("Represents any entity in the game, including players and mobs.")
            .since("1.0.0")
            .register();
        registration.newType(LivingEntity.class, "livingentity", "livingEntit@y@ies")
            .name("Living Entity")
            .description("Represents any living entity in the game, including players and mobs.")
            .since("1.0.0")
            .toStringFunction(LivingEntity::getLegacyDisplayName)
            .register();
        registration.newType(NPCEntity.class, "npcentity", "npcEntit@y@ies")
            .name("NPC Entity")
            .description("Represents an NPC entity in the game.")
            .since("1.0.0")
            .toStringFunction(NPCRegistry::stringify)
            .register();
        registration.newType(Player.class, "player", "player@s")
            .name("Player")
            .description("Represents a player in the game.")
            .since("1.0.0")
            .toStringFunction(Player::getDisplayName)
            .register();
        registration.newType(PlayerRef.class, "playerref", "playerRef@s")
            .name("Player Ref")
            .description("Represents a reference to a player in the game.")
            .since("1.0.0")
            .toStringFunction(PlayerRef::getUsername)
            .register();
    }

}
