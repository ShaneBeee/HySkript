package com.github.skriptdev.skript.plugin.elements.expressions.player;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.entities.Player;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ExprGameMode extends PropertyExpression<Object, GameMode> {

    public static void register(SkriptRegistration registration) {
        registration.newPropertyExpression(ExprGameMode.class, GameMode.class, "game[(-| )]mode", "players")
            .name("GameMode of a player")
            .description("Returns the game mode of a player.")
            .examples("set {_gm} to game-mode of context-player")
            .since("INSERT VERSION")
            .register();
    }

    @Override
    public @Nullable GameMode getProperty(@NotNull Object owner) {
        if (owner instanceof Player player) {
            return player.getGameMode();
        }
        return null;
    }

    @Override
    public Optional<Class<?>[]> acceptsChange(@NotNull ChangeMode mode) {
        if (mode == ChangeMode.SET) return Optional.of(new Class<?>[]{GameMode.class});
        return Optional.empty();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void change(@NotNull TriggerContext ctx, @NotNull ChangeMode changeMode, @NotNull Object[] changeWith) {
        if (changeMode != ChangeMode.SET || changeWith == null) return;
        GameMode newGameMode = changeWith[0] instanceof GameMode ? (GameMode) changeWith[0] : null;
        if (newGameMode == null) return;

        Optional<?> owner = getOwner().getSingle(ctx);
        if (!(owner.isPresent() && owner.get() instanceof Player player)) return;

        Player.setGameMode(player.getReference(), newGameMode, player.getReference().getStore());
    }

    @Override
    public Class<? extends GameMode> getReturnType() {
        return GameMode.class;
    }
}
