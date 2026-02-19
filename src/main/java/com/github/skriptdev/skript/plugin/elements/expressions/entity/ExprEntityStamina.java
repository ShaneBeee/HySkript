package com.github.skriptdev.skript.plugin.elements.expressions.entity;

import com.github.skriptdev.skript.api.hytale.utils.EntityUtils;
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

public class ExprEntityStamina extends PropertyExpression<LivingEntity, Number> {

    private static final int STAMINA_STAT_INDEX = DefaultEntityStatTypes.getStamina();

    public static void register(SkriptRegistration registration) {
        registration.newPropertyExpression(ExprEntityStamina.class, Number.class,
                "[(min:min|max:max)] stamina", "livingentities")
            .name("Entity Stamina")
            .description("Get/set the stamina of an entity.",
                "Also supports getting the min/max stamina, these cannot be changed.")
            .examples("set {_stamina} to stamina of player",
                "set stamina of player to 20",
                "if stamina of player is greater than 0:",
                "\treset stamina of player")
            .since("INSERT VERSION")
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
        EntityStatMap entityStatMap = EntityUtils.getEntityStatMap(entity);
        if (entityStatMap == null) return null;
        EntityStatValue stamina = entityStatMap.get(STAMINA_STAT_INDEX);
        if (stamina == null) return null;

        if (this.pattern == 1) return stamina.getMin();
        if (this.pattern == 2) return stamina.getMax();
        return stamina.get();
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

        Runnable staminaRunnable = () -> {

            EntityStatMap statMap = EntityUtils.getEntityStatMap(entity);
            if (statMap == null) return;

            if (changeMode == ChangeMode.RESET) {
                statMap.resetStatValue(STAMINA_STAT_INDEX);
                return;
            }

            float newValue;
            if (changeWith.length > 0 && changeWith[0] instanceof Number number) {
                newValue = number.floatValue();
            } else {
                newValue = 0f;
            }

            if (changeMode != ChangeMode.SET) {
                EntityStatValue staminaStat = statMap.get(STAMINA_STAT_INDEX);
                if (staminaStat == null) return;
                float oldStaminaValue = staminaStat.get();

                if (changeMode == ChangeMode.ADD) {
                    newValue += oldStaminaValue;
                } else if (changeMode == ChangeMode.REMOVE) {
                    newValue = oldStaminaValue - newValue;
                } else if (changeMode == ChangeMode.DELETE) {
                    newValue = 0f;
                }
            }

            statMap.setStatValue(STAMINA_STAT_INDEX, newValue);
        };

        if (world.isInThread()) {
            staminaRunnable.run();
        } else {
            world.execute(staminaRunnable);
        }
    }

}
