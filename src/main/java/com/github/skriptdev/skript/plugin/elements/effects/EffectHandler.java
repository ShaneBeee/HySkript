package com.github.skriptdev.skript.plugin.elements.effects;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.plugin.elements.effects.block.EffBreakBlock;
import com.github.skriptdev.skript.plugin.elements.effects.block.EffDamageBlock;
import com.github.skriptdev.skript.plugin.elements.effects.entity.EffDamage;
import com.github.skriptdev.skript.plugin.elements.effects.entity.EffDropItem;
import com.github.skriptdev.skript.plugin.elements.effects.entity.EffEntityEffect;
import com.github.skriptdev.skript.plugin.elements.effects.entity.EffFreeze;
import com.github.skriptdev.skript.plugin.elements.effects.entity.EffKill;
import com.github.skriptdev.skript.plugin.elements.effects.entity.EffRemoveStatModifier;
import com.github.skriptdev.skript.plugin.elements.effects.entity.EffRide;
import com.github.skriptdev.skript.plugin.elements.effects.entity.EffShoot;
import com.github.skriptdev.skript.plugin.elements.effects.entity.EffSpawnEntity;
import com.github.skriptdev.skript.plugin.elements.effects.entity.EffTame;
import com.github.skriptdev.skript.plugin.elements.effects.player.EffConnect;
import com.github.skriptdev.skript.plugin.elements.effects.other.EffExecuteCommand;
import com.github.skriptdev.skript.plugin.elements.effects.other.EffSendMessage;
import com.github.skriptdev.skript.plugin.elements.effects.entity.EffTeleport;
import com.github.skriptdev.skript.plugin.elements.effects.player.EffBan;
import com.github.skriptdev.skript.plugin.elements.effects.other.EffBroadcast;
import com.github.skriptdev.skript.plugin.elements.effects.other.EffCancelEvent;
import com.github.skriptdev.skript.plugin.elements.effects.other.EffDelay;
import com.github.skriptdev.skript.plugin.elements.effects.player.EffKick;
import com.github.skriptdev.skript.plugin.elements.effects.other.EffSendTitle;
import com.github.skriptdev.skript.plugin.elements.effects.player.EffOpenItemContainer;
import com.github.skriptdev.skript.plugin.elements.effects.server.EffServerShutdown;
import com.github.skriptdev.skript.plugin.elements.effects.world.EffChunkLoadAsync;
import com.github.skriptdev.skript.plugin.elements.effects.world.EffChunkRegenerate;
import com.github.skriptdev.skript.plugin.elements.effects.world.EffExplosion;
import com.github.skriptdev.skript.plugin.elements.effects.entity.EffInteraction;
import com.github.skriptdev.skript.plugin.elements.effects.world.EffParticle;

public class EffectHandler {

    public static void register(SkriptRegistration registration) {
        // BLOCK
        EffBreakBlock.register(registration);
        EffDamageBlock.register(registration);

        // ENTITY
        EffDamage.register(registration);
        EffDropItem.register(registration);
        EffEntityEffect.register(registration);
        EffFreeze.register(registration);
        EffInteraction.register(registration);
        EffKill.register(registration);
        EffRemoveStatModifier.register(registration);
        EffRide.register(registration);
        EffShoot.register(registration);
        EffSpawnEntity.register(registration);
        EffTame.register(registration);
        EffTeleport.register(registration);

        // OTHER
        EffBroadcast.register(registration);
        EffCancelEvent.register(registration);
        EffDelay.register(registration);
        EffExecuteCommand.register(registration);
        EffSendMessage.register(registration);
        EffSendTitle.register(registration);

        // PLAYER
        EffBan.register(registration);
        EffConnect.register(registration);
        EffKick.register(registration);
        EffOpenItemContainer.register(registration);

        // SERVER
        EffServerShutdown.register(registration);

        // WORLD
        EffChunkLoadAsync.register(registration);
        EffChunkRegenerate.register(registration);
        EffExplosion.register(registration);
        EffParticle.register(registration);
    }

}
