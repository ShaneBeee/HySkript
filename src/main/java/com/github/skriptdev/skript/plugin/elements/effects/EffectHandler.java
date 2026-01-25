package com.github.skriptdev.skript.plugin.elements.effects;

import io.github.syst3ms.skriptparser.registration.SkriptRegistration;

public class EffectHandler {

    public static void register(SkriptRegistration registration) {
        EffBan.register(registration);
        EffBroadcast.register(registration);
        EffCancelEvent.register(registration);
        EffKick.register(registration);
        EffKill.register(registration);
        EffSendMessage.register(registration);
        EffTeleport.register(registration);
    }

}
