package com.github.skriptdev.skript.plugin.elements.expressions.player;

import com.github.skriptdev.skript.api.hytale.utils.PlayerUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class ExprAllPlayers implements Expression<Player> {

    public static void register(SkriptRegistration registration) {
        registration.newExpression(ExprAllPlayers.class, Player.class, false,
                "all players [in %world%]")
            .name("All Players")
            .description("Returns all players in the server or in a specific world.")
            .examples("broadcast \"There are %size of all players% players online!\"",
                "loop all players in world of player:",
                "loop all players:",
                "kill all players")
            .since("1.0.0")
            .register();
    }

    private Expression<World> world;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        if (expressions.length > 0) this.world = (Expression<World>) expressions[0];
        return true;
    }

    @Override
    public Player[] getValues(@NotNull TriggerContext ctx) {
        World world = null;
        if (this.world != null) {
            Optional<? extends World> single = this.world.getSingle(ctx);
            if (single.isPresent()) world = single.get();
        }
        List<Player> players = PlayerUtils.getPlayers(world);
        return players.toArray(new Player[0]);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String w = this.world != null ? " in " + this.world.toString(ctx, debug) : "";
        return "all players" + w;
    }

}
