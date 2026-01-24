package com.github.skriptdev.skript.plugin.elements.effects;

import io.github.syst3ms.skriptparser.registration.SkriptRegistration;

public class EffectHandler {

    public static void register(SkriptRegistration registration) {
        EffBroadcast.register(registration);
        EffCancelEvent.register(registration);
        EffSendMessage.register(registration);
        EffTeleport.register(registration);
    }

}
