package com.github.skriptdev.skript.plugin.elements.effects.player;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

public class EffConnect extends Effect {

    public static void register(SkriptRegistration reg) {
        reg.newEffect(EffConnect.class, "(connect|transfer|refer) %players/playerrefs% to %string%",
                "(connect|transfer|refer) %players/playerrefs% to %string% on port %number%")
            .name("Connect")
            .description("Connects the specified players to another server.",
                "If port is excluded, it defaults to `5520` (Hytale's default port).",
                "See [PLAYER REFERRAL](https://support.hytale.com/hc/en-us/articles/45326769420827-Hytale-Server-" +
                    "Manual#multiserver-architecture) for more info.")
            .examples("connect all players to \"https://someserver.net\"")
            .register();
    }

    private Expression<?> players;
    private Expression<String> address;
    private Expression<Number> port;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.players = expressions[0];
        this.address = (Expression<String>) expressions[1];
        if (matchedPattern == 1) {
            this.port = (Expression<Number>) expressions[2];
        }
        return true;
    }

    @SuppressWarnings("removal") // Player#getPlayerRef (replacement?)
    @Override
    protected void execute(@NotNull TriggerContext ctx) {
        String address = this.address.getSingle(ctx).orElse(null);
        if (address == null) return;

        int port = 5520;
        if (this.port != null) {
            Number number = this.port.getSingle(ctx).orElse(null);
            if (number != null) port = number.intValue();
        }

        for (Object o : this.players.getArray(ctx)) {
            if (o instanceof PlayerRef playerRef) {
                playerRef.referToServer(address, port);
            } else if (o instanceof Player player) {
                player.getPlayerRef().referToServer(address, port);
            }
        }
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String port = this.port != null ? " on port " + this.port.toString(ctx, debug) : "";
        return "connect " + this.players.toString(ctx, debug) + " to " + this.address.toString(ctx, debug) + port;
    }

}
