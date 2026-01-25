package com.github.skriptdev.skript.plugin.elements.sections;

import io.github.syst3ms.skriptparser.registration.SkriptRegistration;

public class SectionHandler {

    public static void register(SkriptRegistration registration) {
        SecSpawnNPC.register(registration);
    }

}
