package com.github.skriptdev.skript.plugin.elements.events;


import com.github.skriptdev.skript.api.hytale.Block;
import com.github.skriptdev.skript.api.skript.event.BlockContext;
import com.github.skriptdev.skript.api.skript.event.LocationContext;
import com.github.skriptdev.skript.api.skript.event.PlayerContext;
import com.github.skriptdev.skript.api.skript.event.PlayerRefContext;
import com.github.skriptdev.skript.api.skript.event.WorldContext;
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
import com.github.skriptdev.skript.plugin.elements.events.player.EvtPlayerCraftRecipe;
import com.github.skriptdev.skript.plugin.elements.events.player.EvtPlayerDamageBlock;
import com.github.skriptdev.skript.plugin.elements.events.player.EvtPlayerDiscoverZone;
import com.github.skriptdev.skript.plugin.elements.events.player.EvtPlayerDrainFromWorld;
import com.github.skriptdev.skript.plugin.elements.events.player.EvtPlayerDropItem;
import com.github.skriptdev.skript.plugin.elements.events.player.EvtPlayerDropItemRequest;
import com.github.skriptdev.skript.plugin.elements.events.player.EvtPlayerJoin;
import com.github.skriptdev.skript.plugin.elements.events.player.EvtPlayerMouseClick;
import com.github.skriptdev.skript.plugin.elements.events.player.EvtPlayerMouseMove;
import com.github.skriptdev.skript.plugin.elements.events.player.EvtPlayerPickupItem;
import com.github.skriptdev.skript.plugin.elements.events.player.EvtPlayerPlaceBlock;
import com.github.skriptdev.skript.plugin.elements.events.player.EvtPlayerSetupConnect;
import com.github.skriptdev.skript.plugin.elements.events.player.EvtPlayerSetupDisconnect;
import com.github.skriptdev.skript.plugin.elements.events.player.EvtPlayerSwitchActiveSlot;
import com.github.skriptdev.skript.plugin.elements.events.player.EvtPlayerUseBlock;
import com.github.skriptdev.skript.plugin.elements.events.server.EvtBoot;
import com.github.skriptdev.skript.plugin.elements.events.server.EvtShutdown;
import com.github.skriptdev.skript.plugin.elements.events.skript.EvtLoad;
import com.github.skriptdev.skript.plugin.elements.events.world.EvtAtWorldTime;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
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
        EvtPlayerCraftRecipe.register(registration);
        EvtPlayerDamageBlock.register(registration);
        EvtPlayerDiscoverZone.register(registration);
        EvtPlayerDrainFromWorld.register(registration);
        EvtPlayerDropItem.register(registration);
        EvtPlayerDropItemRequest.register(registration);
        EvtPlayerJoin.register(registration);
        EvtPlayerMouseClick.register(registration);
        EvtPlayerMouseMove.register(registration);
        EvtPlayerPickupItem.register(registration);
        EvtPlayerPlaceBlock.register(registration);
        EvtPlayerSetupConnect.register(registration);
        EvtPlayerSetupDisconnect.register(registration);
        EvtPlayerSwitchActiveSlot.register(registration);
        EvtPlayerUseBlock.register(registration);

        // SERVER
        EvtBoot.register(registration);
        EvtShutdown.register(registration);

        // SKRIPT
        EvtLoad.register(registration);

        // WORLD
        EvtAtWorldTime.register(registration);

        // CONTEXT
        registerGlobalContexts(registration);
    }

    public static void shutdown() {
        // Shutdown any events that are running (such as a periodical)
        // Nothing yet for now
    }

    private static void registerGlobalContexts(SkriptRegistration reg) {
        reg.newSingleContextValue(BlockContext.class, Block.class,
                "block", BlockContext::getBlock)
            .register();
        reg.newSingleContextValue(LocationContext.class, Location.class,
                "location", LocationContext::getLocation)
            .setUsage(Usage.EXPRESSION_OR_ALONE)
            .register();
        reg.newSingleContextValue(PlayerContext.class, Player.class,
                "player", PlayerContext::getPlayer)
            .setUsage(Usage.EXPRESSION_OR_ALONE)
            .register();
        reg.newSingleContextValue(WorldContext.class, World.class,
                "world", WorldContext::getWorld)
            .setUsage(Usage.EXPRESSION_OR_ALONE)
            .register();
        reg.addSingleContextValue(PlayerRefContext.class, PlayerRef.class,
            "playerref", PlayerRefContext::getPlayerRef);
        reg.addSingleContextValue(PlayerRefContext.class, PlayerRef.class,
            "player-ref", PlayerRefContext::getPlayerRef);
    }

}
