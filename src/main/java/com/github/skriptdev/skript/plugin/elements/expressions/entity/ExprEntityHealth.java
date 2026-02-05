package com.github.skriptdev.skript.plugin.elements.expressions.entity;

import com.github.skriptdev.skript.api.hytale.EntityComponentUtils;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ExprEntityHealth extends PropertyExpression<LivingEntity, Number> {

    private static final int HEALTH_STAT_INDEX = DefaultEntityStatTypes.getHealth();

    public static void register(SkriptRegistration registration) {
        registration.newPropertyExpression(ExprEntityHealth.class, Number.class,
                "[(min:min|max:max)] health", "livingentities")
            .name("Entity Health")
            .description("Get/set the health of an entity.",
                "Also supports getting the min/max health, these cannot be changed.")
            .examples("set {_health} to health of player",
                "set health of player to 20",
                "if health of player is greater than 0:")
            .since("1.0.0")
            .register();
    }

    int pattern = 0;

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, ParseContext parseContext) {
        if (parseContext.hasMark("min")) pattern = 1;
        else if (parseContext.hasMark("max")) pattern = 2;
        return super.init(expressions, matchedPattern, parseContext);
    }

    @Override
    public @Nullable Number getProperty(@NotNull LivingEntity entity) {
        EntityStatMap entityStatMap = EntityComponentUtils.getEntityStatMap(entity);
        if (entityStatMap == null) return null;
        EntityStatValue health = entityStatMap.get(HEALTH_STAT_INDEX);
        if (health == null) return null;

        if (this.pattern == 1) return health.getMin();
        if (this.pattern == 2) return health.getMax();
        return health.get();
    }

    @Override
    public Optional<Class<?>[]> acceptsChange(@NotNull ChangeMode mode) {
        if (this.pattern > 0) {
            return Optional.empty();
        }
        return Optional.of(new Class<?>[]{Number.class});
    }

    @Override
    public void change(@NotNull TriggerContext ctx, @NotNull ChangeMode changeMode, Object @NotNull [] changeWith) {
        Optional<? extends LivingEntity> single = getOwner().getSingle(ctx);
        if (single.isEmpty()) return;

        LivingEntity entity = single.get();
        World world = entity.getWorld();
        if (world == null) return;

        Runnable healthRunnable = () -> {

            EntityStatMap statMap = EntityComponentUtils.getEntityStatMap(entity);
            if (statMap == null) return;

            if (changeMode == ChangeMode.RESET) {
                statMap.resetStatValue(HEALTH_STAT_INDEX);
                return;
            }

            float newValue;
            if (changeWith.length > 0 && changeWith[0] instanceof Number number) {
                newValue = number.floatValue();
            } else {
                newValue = 0f;
            }

            if (changeMode != ChangeMode.SET) {
                EntityStatValue healthStat = statMap.get(HEALTH_STAT_INDEX);
                if (healthStat == null) return;
                float oldHealthValue = healthStat.get();

                if (changeMode == ChangeMode.ADD) {
                    newValue += oldHealthValue;
                } else if (changeMode == ChangeMode.REMOVE) {
                    newValue = oldHealthValue - newValue;
                } else if (changeMode == ChangeMode.DELETE) {
                    newValue = 0f;
                }
            }

            statMap.setStatValue(HEALTH_STAT_INDEX, newValue);
        };

        if (world.isInThread()) {
            healthRunnable.run();
        } else {
            world.execute(healthRunnable);
        }
    }

}
