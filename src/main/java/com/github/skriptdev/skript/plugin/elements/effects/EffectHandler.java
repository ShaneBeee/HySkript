package com.github.skriptdev.skript.plugin.elements.effects;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;

public class EffectHandler {

    public static void register(SkriptRegistration registration) {
        EffBan.register(registration);
        EffBroadcast.register(registration);
        EffCancelEvent.register(registration);
        EffDelay.register(registration);
        EffDropItem.register(registration);
        EffKick.register(registration);
        EffKill.register(registration);
        EffSendMessage.register(registration);
        EffTeleport.register(registration);
    }

}
