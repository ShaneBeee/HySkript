package com.github.skriptdev.skript.plugin.elements.effects;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.plugin.elements.effects.block.EffBreakBlock;
import com.github.skriptdev.skript.plugin.elements.effects.entity.EffDropItem;
import com.github.skriptdev.skript.plugin.elements.effects.entity.EffKill;
import com.github.skriptdev.skript.plugin.elements.effects.entity.EffSendMessage;
import com.github.skriptdev.skript.plugin.elements.effects.entity.EffTeleport;
import com.github.skriptdev.skript.plugin.elements.effects.player.EffBan;
import com.github.skriptdev.skript.plugin.elements.effects.player.EffBroadcast;
import com.github.skriptdev.skript.plugin.elements.effects.other.EffCancelEvent;
import com.github.skriptdev.skript.plugin.elements.effects.player.EffDelay;
import com.github.skriptdev.skript.plugin.elements.effects.player.EffKick;

public class EffectHandler {

    public static void register(SkriptRegistration registration) {
        EffBan.register(registration);
        EffBreakBlock.register(registration);
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
