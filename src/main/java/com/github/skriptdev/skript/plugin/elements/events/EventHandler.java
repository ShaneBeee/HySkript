package com.github.skriptdev.skript.plugin.elements.events;

import io.github.syst3ms.skriptparser.registration.SkriptRegistration;

public class EventHandler {

    public static void register(SkriptRegistration registration) {
        EvtLoad.register(registration);
        EvtPlayerChat.register(registration);
        EvtPlayerJoin.register(registration);
    }

}
