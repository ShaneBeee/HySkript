package com.github.skriptdev.skript.plugin.elements.events;


import com.github.skriptdev.skript.api.hytale.Block;
import com.github.skriptdev.skript.api.skript.event.BlockContext;
import com.github.skriptdev.skript.api.skript.event.PlayerContext;
import com.github.skriptdev.skript.api.skript.event.PlayerRefContext;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.plugin.elements.events.entity.EvtEntityDamage;
import com.github.skriptdev.skript.plugin.elements.events.entity.EvtEntityDeath;
import com.github.skriptdev.skript.plugin.elements.events.entity.EvtEntityPickupItem;
import com.github.skriptdev.skript.plugin.elements.events.entity.EvtEntityRemove;
import com.github.skriptdev.skript.plugin.elements.events.entity.EvtLivingEntityInvChange;
import com.github.skriptdev.skript.plugin.elements.events.player.EvtPlayerAddToWorld;
import com.github.skriptdev.skript.plugin.elements.events.player.EvtPlayerBreakBlock;
import com.github.skriptdev.skript.plugin.elements.events.player.EvtPlayerChangeGameMode;
import com.github.skriptdev.skript.plugin.elements.events.player.EvtPlayerChat;
import com.github.skriptdev.skript.plugin.elements.events.player.EvtPlayerDamageBlock;
import com.github.skriptdev.skript.plugin.elements.events.player.EvtPlayerDrainFromWorld;
import com.github.skriptdev.skript.plugin.elements.events.player.EvtPlayerDropItem;
import com.github.skriptdev.skript.plugin.elements.events.player.EvtPlayerDropItemRequest;
import com.github.skriptdev.skript.plugin.elements.events.player.EvtPlayerJoin;
import com.github.skriptdev.skript.plugin.elements.events.player.EvtPlayerMouseClick;
import com.github.skriptdev.skript.plugin.elements.events.player.EvtPlayerMouseMove;
import com.github.skriptdev.skript.plugin.elements.events.player.EvtPlayerPlaceBlock;
import com.github.skriptdev.skript.plugin.elements.events.player.EvtPlayerSetupConnect;
import com.github.skriptdev.skript.plugin.elements.events.player.EvtPlayerSetupDisconnect;
import com.github.skriptdev.skript.plugin.elements.events.player.EvtPlayerUseBlock;
import com.github.skriptdev.skript.plugin.elements.events.server.EvtBoot;
import com.github.skriptdev.skript.plugin.elements.events.server.EvtShutdown;
import com.github.skriptdev.skript.plugin.elements.events.skript.EvtLoad;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import io.github.syst3ms.skriptparser.registration.context.ContextValue.Usage;

public class EventHandler {

    public static void register(SkriptRegistration registration) {
        // ENTITY
        EvtEntityDamage.register(registration);
        EvtEntityDeath.register(registration);
        EvtEntityPickupItem.register(registration);
        EvtEntityRemove.register(registration);
        EvtLivingEntityInvChange.register(registration);

        // PLAYER
        EvtPlayerAddToWorld.register(registration);
        EvtPlayerBreakBlock.register(registration);
        EvtPlayerChangeGameMode.register(registration);
        EvtPlayerChat.register(registration);
        EvtPlayerDamageBlock.register(registration);
        EvtPlayerDrainFromWorld.register(registration);
        EvtPlayerDropItem.register(registration);
        EvtPlayerDropItemRequest.register(registration);
        EvtPlayerJoin.register(registration);
        EvtPlayerMouseClick.register(registration);
        EvtPlayerMouseMove.register(registration);
        EvtPlayerPlaceBlock.register(registration);
        EvtPlayerSetupConnect.register(registration);
        EvtPlayerSetupDisconnect.register(registration);
        EvtPlayerUseBlock.register(registration);

        // SERVER
        EvtBoot.register(registration);
        EvtShutdown.register(registration);

        // SKRIPT
        EvtLoad.register(registration);

        // CONTEXT
        registerGlobalContexts(registration);
    }

    private static void registerGlobalContexts(SkriptRegistration reg) {
        reg.newSingleContextValue(BlockContext.class, Block.class,
                "block", BlockContext::getBlock)
            .register();
        reg.newSingleContextValue(PlayerContext.class, Player.class,
                "player", PlayerContext::getPlayer)
            .setUsage(Usage.EXPRESSION_OR_ALONE)
            .register();
        reg.addSingleContextValue(PlayerRefContext.class, PlayerRef.class,
                "playerref", PlayerRefContext::getPlayerRef);
        reg.addSingleContextValue(PlayerRefContext.class, PlayerRef.class,
                "player-ref", PlayerRefContext::getPlayerRef);
    }

}
