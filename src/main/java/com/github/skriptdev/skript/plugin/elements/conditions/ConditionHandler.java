package com.github.skriptdev.skript.plugin.elements.conditions;


import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;

public class ConditionHandler {

    public static void register(SkriptRegistration registration) {
        CondHasPermission.register(registration);
        CondPlayerIsCrouching.register(registration);
    }

}
