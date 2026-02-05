package com.github.skriptdev.skript.plugin.elements.events.player;

import com.github.skriptdev.skript.api.skript.event.PlayerContext;
import com.github.skriptdev.skript.api.skript.event.PlayerRefContext;
import com.github.skriptdev.skript.plugin.HySk;
import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.TriggerMap;
import io.github.syst3ms.skriptparser.lang.event.SkriptEvent;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EvtPlayerJoin extends SkriptEvent {

    public static void register(SkriptRegistration reg) {
        reg.newEvent(EvtPlayerJoin.class, "player connect", "player ready", "player quit")
            .name("Player Join/Quit")
            .description("Events triggered when a player joins or quits the server.")
            .since("1.0.0")
            .setHandledContexts(PlayerConnectContext.class)
            .register();
    }

    private static EventRegistration<Void, PlayerConnectEvent> CONNECT_LISTENER;
    private static EventRegistration<String, PlayerReadyEvent> READY_LISTENER;
    private static EventRegistration<Void, PlayerDisconnectEvent> DISCONNECT_LISTENER;
    private int pattern;

    @SuppressWarnings("removal")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.pattern = matchedPattern;

        EventRegistry registry = HySk.getInstance().getEventRegistry();
        if (CONNECT_LISTENER == null) {
            CONNECT_LISTENER = registry.register(PlayerConnectEvent.class, playerConnectEvent -> {
                Player player = playerConnectEvent.getHolder().getComponent(Player.getComponentType());
                TriggerMap.callTriggersByContext(new PlayerConnectContext(playerConnectEvent.getPlayerRef(), player, 0));
            });
        }
        if (READY_LISTENER == null) {
            READY_LISTENER = registry.registerGlobal(PlayerReadyEvent.class, playerReadyEvent -> {
                Player player = playerReadyEvent.getPlayer();
                TriggerMap.callTriggersByContext(new PlayerConnectContext(player.getPlayerRef(), player, 1));
            });
        }
        if (DISCONNECT_LISTENER == null) {
            DISCONNECT_LISTENER = registry.register(PlayerDisconnectEvent.class, playerDisconnectEvent -> {
                PlayerRef playerRef = playerDisconnectEvent.getPlayerRef();
                PlayerConnectContext playerConnectContext = new PlayerConnectContext(playerRef, null, 2);
                TriggerMap.callTriggersByContext(playerConnectContext);
            });
        }
        return true;
    }

    @Override
    public boolean check(@NotNull TriggerContext ctx) {
        if (!(ctx instanceof PlayerConnectContext playerConnectContext)) return false;
        return this.pattern == playerConnectContext.getPattern();
    }

    public int getPattern() {
        return this.pattern;
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        String t = switch (this.pattern) {
            case 0 -> "connect";
            case 1 -> "ready";
            case 2 -> "quit";
            default -> "unknown";
        };
        return "player " + t;
    }

    public static class PlayerConnectContext implements PlayerContext, PlayerRefContext {

        private final PlayerRef playerRef;
        private final Player player;
        private final int pattern;

        public PlayerConnectContext(PlayerRef playerRef, Player player, int pattern) {
            this.playerRef = playerRef;
            this.player = player;
            this.pattern = pattern;
        }

        @Override
        public PlayerRef getPlayerRef() {
            return this.playerRef;
        }

        @Override
        public Player getPlayer() {
            return this.player;
        }

        public int getPattern() {
            return this.pattern;
        }

        @Override
        public String getName() {
            return "player-connect-context";
        }

    }

}
