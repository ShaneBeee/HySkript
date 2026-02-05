package com.github.skriptdev.skript.plugin.elements.events.player;

import com.github.skriptdev.skript.api.skript.event.CancellableContext;
import com.github.skriptdev.skript.api.skript.event.PlayerContext;
import com.github.skriptdev.skript.api.skript.event.SystemEvent;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.ChangeGameModeEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.TriggerMap;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.context.ContextValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EvtPlayerChangeGameMode extends SystemEvent<EntityEventSystem<EntityStore, ChangeGameModeEvent>> {

    public static void register(SkriptRegistration reg) {
        reg.newEvent(EvtPlayerChangeGameMode.class, "[player(s| )] game[(-| )]mode change[s]")
                .setHandledContexts(PlayerChangeGameModeContext.class)
                .name("Player Change GameMode")
                .description("Called when a player's game-mode is changed.")
                .since("INSERT VERSION")
                .register();

        reg.newSingleContextValue(PlayerChangeGameModeContext.class, GameMode.class, "game[(-| )]mode", PlayerChangeGameModeContext::getOldGameMode)
                .setState(ContextValue.State.PAST)
                .setUsage(ContextValue.Usage.EXPRESSION_OR_ALONE)
                .register();
        reg.newSingleContextValue(PlayerChangeGameModeContext.class, GameMode.class, "game[(-| )]mode", PlayerChangeGameModeContext::getNewGameMode)
                .setState(ContextValue.State.PRESENT)
                .setUsage(ContextValue.Usage.EXPRESSION_OR_ALONE)
                .addSetter(PlayerChangeGameModeContext::setNewGameMode)
                .register();
    }

    private static ChangeGameModeEventSystem SYSTEM;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        if (SYSTEM == null) {
            SYSTEM = new ChangeGameModeEventSystem(ChangeGameModeEvent.class);
            applySystem(SYSTEM);
        }
        return true;
    }

    @Override
    public boolean check(@NotNull TriggerContext triggerContext) {
        return triggerContext instanceof PlayerChangeGameModeContext;
    }

    @Override
    public String toString(@Nullable TriggerContext triggerContext, boolean debug) {
        return "player change gamemode";
    }

    private record PlayerChangeGameModeContext(ChangeGameModeEvent event,  Player player) 
    implements PlayerContext, CancellableContext {

        public Player getPlayer() {
            return this.player;
        }

        public GameMode getNewGameMode() {
            return event.getGameMode();
        }

        public GameMode getOldGameMode() {
            return getPlayer().getGameMode();
        }

        public void setNewGameMode(GameMode gameMode) {
            event.setGameMode(gameMode);
        }

        @Override
        public String getName() {
            return "player change gamemode context";
        }

        @Override
        public boolean isCancelled() {
            return event.isCancelled();
        }

        @Override
        public void setCancelled(boolean cancelled) {
            event.setCancelled(cancelled);
        }
    }

    private static final class ChangeGameModeEventSystem extends EntityEventSystem<EntityStore, ChangeGameModeEvent> {

        private ChangeGameModeEventSystem(@NotNull Class<ChangeGameModeEvent> eventType) {
            super(eventType);
        }

        @Override
        public void handle(int i, @NotNull ArchetypeChunk<EntityStore> archetypeChunk, @NotNull Store<EntityStore> store, @NotNull CommandBuffer<EntityStore> commandBuffer, @NotNull ChangeGameModeEvent event) {

            Ref<EntityStore> ref = archetypeChunk.getReferenceTo(i);
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) return;

            TriggerMap.callTriggersByContext(new PlayerChangeGameModeContext(event, player));
        }

        @Override
        public @NotNull Query<EntityStore> getQuery() {
            return PlayerRef.getComponentType();
        }
    }
}
