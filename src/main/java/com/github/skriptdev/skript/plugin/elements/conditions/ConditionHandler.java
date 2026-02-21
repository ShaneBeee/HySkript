package com.github.skriptdev.skript.plugin.elements.conditions;


import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.plugin.elements.conditions.block.CondBlockIsSolid;
import com.github.skriptdev.skript.plugin.elements.conditions.entity.CondEntityIsAlive;
import com.github.skriptdev.skript.plugin.elements.conditions.entity.CondEntityIsFrozen;
import com.github.skriptdev.skript.plugin.elements.conditions.entity.CondEntityIsTameable;
import com.github.skriptdev.skript.plugin.elements.conditions.entity.CondEntityIsTamed;
import com.github.skriptdev.skript.plugin.elements.conditions.item.CondInventoryCanHold;
import com.github.skriptdev.skript.plugin.elements.conditions.other.CondObjectOfType;
import com.github.skriptdev.skript.plugin.elements.conditions.player.CondPlayerHasPermission;
import com.github.skriptdev.skript.plugin.elements.conditions.player.CondPlayerHasPlayedBefore;
import com.github.skriptdev.skript.plugin.elements.conditions.player.CondPlayerMovementCrouching;
import com.github.skriptdev.skript.plugin.elements.conditions.player.CondPlayerMovementFalling;
import com.github.skriptdev.skript.plugin.elements.conditions.player.CondPlayerMovementFlying;
import com.github.skriptdev.skript.plugin.elements.conditions.player.CondPlayerMovementIdle;
import com.github.skriptdev.skript.plugin.elements.conditions.player.CondPlayerMovementJumping;
import com.github.skriptdev.skript.plugin.elements.conditions.player.CondPlayerMovementOnGround;
import com.github.skriptdev.skript.plugin.elements.conditions.player.CondPlayerMovementCanFly;
import com.github.skriptdev.skript.plugin.elements.conditions.player.CondPlayerMovementRunning;
import com.github.skriptdev.skript.plugin.elements.conditions.player.CondPlayerMovementSitting;
import com.github.skriptdev.skript.plugin.elements.conditions.player.CondPlayerMovementSleeping;
import com.github.skriptdev.skript.plugin.elements.conditions.player.CondPlayerMovementSprinting;
import com.github.skriptdev.skript.plugin.elements.conditions.player.CondPlayerMovementSwimming;
import com.github.skriptdev.skript.plugin.elements.conditions.player.CondPlayerMovementWalking;
import com.github.skriptdev.skript.plugin.elements.conditions.world.CondChunkIsLoaded;
import com.github.skriptdev.skript.plugin.elements.conditions.world.CondWorldTimePaused;

public class ConditionHandler {

    public static void register(SkriptRegistration registration) {
        // Please keep in alphabetical order

        // BLOCK
        CondBlockIsSolid.register(registration);

        // ENTITY
        CondEntityIsAlive.register(registration);
        CondEntityIsFrozen.register(registration);
        CondEntityIsTameable.register(registration);
        CondEntityIsTamed.register(registration);

        // ITEM
        CondInventoryCanHold.register(registration);

        // OTHER
        CondObjectOfType.register(registration);

        // PLAYER
        CondPlayerHasPermission.register(registration);
        CondPlayerHasPlayedBefore.register(registration);
        CondPlayerMovementCanFly.register(registration);
        CondPlayerMovementCrouching.register(registration);
        CondPlayerMovementFalling.register(registration);
        CondPlayerMovementFlying.register(registration);
        CondPlayerMovementIdle.register(registration);
        CondPlayerMovementJumping.register(registration);
        CondPlayerMovementOnGround.register(registration);
        CondPlayerMovementRunning.register(registration);
        CondPlayerMovementSitting.register(registration);
        CondPlayerMovementSleeping.register(registration);
        CondPlayerMovementSprinting.register(registration);
        CondPlayerMovementSwimming.register(registration);
        CondPlayerMovementWalking.register(registration);

        // WORLD
        CondChunkIsLoaded.register(registration);
        CondWorldTimePaused.register(registration);
    }

}
