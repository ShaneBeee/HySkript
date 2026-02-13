package com.github.skriptdev.skript.plugin.elements.conditions;


import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;

public class ConditionHandler {

    public static void register(SkriptRegistration registration) {
        CondEntityIsFrozen.register(registration);
        CondHasPermission.register(registration);
        CondInventoryCanHold.register(registration);
        CondPlayerIsCrouching.register(registration);
    }

}
