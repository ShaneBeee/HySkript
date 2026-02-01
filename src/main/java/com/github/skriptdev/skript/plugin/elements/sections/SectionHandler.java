package com.github.skriptdev.skript.plugin.elements.sections;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;

public class SectionHandler {

    public static void register(SkriptRegistration registration) {
        SecDropItem.register(registration);
        SecExecuteInWorld.register(registration);
        SecSpawnNPC.register(registration);
    }

}
