package com.github.skriptdev.skript.plugin.elements.conditions;


import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.plugin.elements.conditions.entity.CondEntityIsAlive;
import com.github.skriptdev.skript.plugin.elements.conditions.entity.CondEntityIsFrozen;
import com.github.skriptdev.skript.plugin.elements.conditions.item.CondInventoryCanHold;
import com.github.skriptdev.skript.plugin.elements.conditions.player.CondPlayerHasPermission;
import com.github.skriptdev.skript.plugin.elements.conditions.player.CondPlayerIsCrouching;

public class ConditionHandler {

    public static void register(SkriptRegistration registration) {
        // ENTITY
        CondEntityIsAlive.register(registration);
        CondEntityIsFrozen.register(registration);

        // ITEM
        CondInventoryCanHold.register(registration);

        // PLAYER
        CondPlayerHasPermission.register(registration);
        CondPlayerIsCrouching.register(registration);
    }

}
