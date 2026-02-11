package com.github.skriptdev.skript.plugin.elements.expressions.entity;

import com.github.skriptdev.skript.api.hytale.utils.EntityUtils;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ExprName extends PropertyExpression<Object, String> {

    public static void register(SkriptRegistration registration) {
        registration.newPropertyExpression(ExprName.class, String.class,
                "[:display] name[s]", "entities/players/playerrefs/worlds")
            .name("Name of Object")
            .description("Gets the name of an object.",
                "Currently supports players, entities, and worlds.",
                "Display name refers to the nameplate over an entity/player's head.",
                "Display name of Entity/Player can be set. PlayerRef/World do not support setting.")
            .examples("set {_name} to name of player",
                "set {_w} to name of world of player",
                "set display name of target entity of player to \"Mr Sheep\"")
            .since("1.0.0")
            .register();
    }

    private boolean display;

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, ParseContext parseContext) {
        this.display = parseContext.hasMark("display");
        return super.init(expressions, matchedPattern, parseContext);
    }

    @Override
    public @Nullable String getProperty(Object object) {
        return switch (object) {
            case PlayerRef playerRef -> playerRef.getUsername();
            case Player player -> this.display ? EntityUtils.getName(player) : player.getDisplayName();
            case Entity entity -> EntityUtils.getName(entity);
            case World world -> world.getName();
            default -> null;
        };
    }

    @Override
    public Optional<Class<?>[]> acceptsChange(@NotNull ChangeMode mode) {
        if (this.display && (mode == ChangeMode.SET || mode == ChangeMode.DELETE))
            return Optional.of(new Class<?>[]{String.class});
        return Optional.empty();
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public void change(@NotNull TriggerContext ctx, @NotNull ChangeMode changeMode, Object @NotNull [] changeWith) {
        String name = null;

        if (changeWith != null && changeWith.length > 0 && changeWith[0] instanceof String s) {
            name = s;
        }

        for (Object o : getOwner().getArray(ctx)) {
            if (o instanceof Entity entity) {
                EntityUtils.setNameplateName(entity, name);
            }
        }
    }

}
