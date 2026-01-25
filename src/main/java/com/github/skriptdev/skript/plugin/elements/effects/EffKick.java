package com.github.skriptdev.skript.plugin.elements.effects;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class EffKick extends Effect {

    public static void register(SkriptRegistration registration) {
        registration.newEffect(EffKick.class, "kick %players/playerrefs%",
                "kick %players/playerrefs% (for reason|due to|because) %string%")
            .name("Kick Player")
            .description("Kicks the specified players with an optional reason.")
            .examples("kick all players due to \"Cheating!\"",
                "kick player due to \"Bad chat message!\"",
                "kick all players")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<?> players;
    private Expression<String> reason;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.players = expressions[0];
        if (matchedPattern == 1) {
            this.reason = (Expression<String>) expressions[1];
        }
        return true;
    }

    @Override
    protected void execute(@NotNull TriggerContext ctx) {
        String reason = "You were kicked.";
        if (this.reason != null) {
            Optional<? extends String> single = this.reason.getSingle(ctx);
            if (single.isPresent()) reason = single.get();
        }

        for (Object o : this.players.getArray(ctx)) {
            kick(o, reason);
        }
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String reason = this.reason == null ? "" : " for reason " + this.reason.toString(ctx, debug);
        return "kick " + this.players.toString(ctx, debug) + reason;
    }

    @SuppressWarnings("removal")
    public static void kick(Object player, String reason) {
        if (player instanceof PlayerRef ref) {
            ref.getPacketHandler().disconnect(reason);
        } else if (player instanceof Player p) {
            p.getPlayerRef().getPacketHandler().disconnect(reason);
        }
    }

}
