package com.github.skriptdev.skript.plugin.elements.events.player;

import com.github.skriptdev.skript.api.skript.event.PlayerContext;
import com.github.skriptdev.skript.api.skript.event.SystemEvent;
import com.github.skriptdev.skript.api.skript.event.WorldContext;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.DiscoverZoneEvent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldMapTracker;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.TriggerMap;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EvtPlayerDiscoverZone extends SystemEvent<EntityEventSystem<EntityStore, DiscoverZoneEvent>> {

    public static void register(SkriptRegistration reg) {
        reg.newEvent(EvtPlayerDiscoverZone.class, "player discover zone")
            .name("Player Discover Zone")
            .experimental("This event doesn't seem to be called right now, server issues?!?!")
            .setHandledContexts(DiscoverZoneContext.class)
            .description("Called when a player discovers a zone.")
            .since("1.0.0")
            .register();

        reg.addSingleContextValue(DiscoverZoneContext.class, String.class,
            "zone-name", DiscoverZoneContext::getZoneName);
        reg.addSingleContextValue(DiscoverZoneContext.class, boolean.class,
            "displaying", DiscoverZoneContext::isDisplaying);
        reg.addSingleContextValue(DiscoverZoneContext.class, String.class,
            "region-name", DiscoverZoneContext::getRegionName);
    }

    private static DiscoverZoneSystem SYSTEM;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        if (SYSTEM == null) {
            SYSTEM = new DiscoverZoneSystem();
            applySystem(SYSTEM);
        }
        return true;
    }

    @Override
    public boolean check(TriggerContext ctx) {
        return ctx instanceof DiscoverZoneContext;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "player discover zone";
    }

    public record DiscoverZoneContext(DiscoverZoneEvent event, Player player)
        implements PlayerContext, WorldContext {

        @Override
        public Player getPlayer() {
            return this.player;
        }

        @Override
        public World getWorld() {
            return this.player.getWorld();
        }

        public String getZoneName() {
            WorldMapTracker.ZoneDiscoveryInfo discoveryInfo = this.event.getDiscoveryInfo();
            return discoveryInfo.zoneName();
        }

        public boolean isDisplaying() {
            return this.event.getDiscoveryInfo().display();
        }

        public String getRegionName() {
            return this.event.getDiscoveryInfo().regionName();
        }

        @Override
        public String getName() {
            return "discover zone context";
        }
    }

    public static class DiscoverZoneSystem extends EntityEventSystem<EntityStore, DiscoverZoneEvent> {

        protected DiscoverZoneSystem() {
            super(DiscoverZoneEvent.class);
        }

        @Override
        public void handle(int i, @NotNull ArchetypeChunk<EntityStore> archetypeChunk,
                           @NotNull Store<EntityStore> store, @NotNull CommandBuffer<EntityStore> commandBuffer,
                           @NotNull DiscoverZoneEvent discoverZoneEvent) {

            Ref<EntityStore> ref = archetypeChunk.getReferenceTo(i);
            Player player = store.getComponent(ref, Player.getComponentType());
            DiscoverZoneContext context = new DiscoverZoneContext(discoverZoneEvent, player);
            TriggerMap.callTriggersByContext(context);
        }

        @Override
        public @Nullable Query<EntityStore> getQuery() {
            return Player.getComponentType();
        }
    }

}
