package com.github.skriptdev.skript.plugin.elements.effects;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.plugin.elements.effects.block.EffBreakBlock;
import com.github.skriptdev.skript.plugin.elements.effects.entity.EffDamage;
import com.github.skriptdev.skript.plugin.elements.effects.entity.EffDropItem;
import com.github.skriptdev.skript.plugin.elements.effects.entity.EffEntityEffect;
import com.github.skriptdev.skript.plugin.elements.effects.entity.EffKill;
import com.github.skriptdev.skript.plugin.elements.effects.entity.EffSpawnEntity;
import com.github.skriptdev.skript.plugin.elements.effects.player.EffConnect;
import com.github.skriptdev.skript.plugin.elements.effects.other.EffExecuteCommand;
import com.github.skriptdev.skript.plugin.elements.effects.other.EffSendMessage;
import com.github.skriptdev.skript.plugin.elements.effects.entity.EffTeleport;
import com.github.skriptdev.skript.plugin.elements.effects.player.EffBan;
import com.github.skriptdev.skript.plugin.elements.effects.other.EffBroadcast;
import com.github.skriptdev.skript.plugin.elements.effects.other.EffCancelEvent;
import com.github.skriptdev.skript.plugin.elements.effects.other.EffDelay;
import com.github.skriptdev.skript.plugin.elements.effects.player.EffKick;
import com.github.skriptdev.skript.plugin.elements.effects.world.EffChunkRegenerate;
import com.github.skriptdev.skript.plugin.elements.effects.world.EffExplosion;
import com.github.skriptdev.skript.plugin.elements.effects.entity.EffInteraction;

public class EffectHandler {

    public static void register(SkriptRegistration registration) {
        // BLOCK
        EffBreakBlock.register(registration);

        // ENTITY
        EffDamage.register(registration);
        EffDropItem.register(registration);
        EffEntityEffect.register(registration);
        EffInteraction.register(registration);
        EffKill.register(registration);
        EffSpawnEntity.register(registration);
        EffTeleport.register(registration);

        // OTHER
        EffBroadcast.register(registration);
        EffCancelEvent.register(registration);
        EffDelay.register(registration);
        EffExecuteCommand.register(registration);
        EffSendMessage.register(registration);

        // PLAYER
        EffBan.register(registration);
        EffConnect.register(registration);
        EffKick.register(registration);

        // WORLD
        EffChunkRegenerate.register(registration);
        EffExplosion.register(registration);
    }

}
