package com.github.skriptdev.skript.plugin.elements.sections;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;

public class SectionHandler {

    public static void register(SkriptRegistration registration) {
        SecApplyStatModifier.register(registration);
        SecDropItem.register(registration);
        SecExecuteInWorld.register(registration);
        SecPlaySound.register(registration);
        SecSendNotification.register(registration);
        SecSendTitle.register(registration);
        SecSpawnNPC.register(registration);
    }

}
