package com.github.skriptdev.skript.plugin.elements.expressions.entity;

import com.github.skriptdev.skript.api.hytale.utils.EntityUtils;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExprEntityStat implements Expression<Number> {

    public static void register(SkriptRegistration registration) {
        registration.newExpression(ExprEntityStat.class, Number.class, true,
                "%entitystattype% stat of %livingentities%",
                "%livingentities%'[s] %entitystattype% stat")
            .name("Entity Stat")
            .description("Get/set the stats of an entity.")
            .examples("set {_o} to oxygen stat of player",
                "set health stat of all entities to 10",
                "set ammo stat of player to 5",
                "remove 3 from SignatureEnergy stat of player",
                "reset immunity stat of all players")
            .since("1.0.0")
            .register();
    }

    private Expression<EntityStatType> entityStatType;
    private Expression<LivingEntity> entities;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        if (matchedPattern == 0) {
            this.entityStatType = (Expression<EntityStatType>) expressions[0];
            this.entities = (Expression<LivingEntity>) expressions[1];
        } else {
            this.entityStatType = (Expression<EntityStatType>) expressions[1];
            this.entities = (Expression<LivingEntity>) expressions[0];
        }
        return true;
    }

    @Override
    public Number[] getValues(@NotNull TriggerContext ctx) {
        Optional<? extends EntityStatType> single = this.entityStatType.getSingle(ctx);
        if (single.isEmpty()) return null;
        int statIndex = EntityStatType.getAssetMap().getIndex(single.get().getId());

        List<Number> values = new ArrayList<>();

        for (LivingEntity livingEntity : this.entities.getArray(ctx)) {
            EntityStatMap entityStatMap = EntityUtils.getEntityStatMap(livingEntity);
            if (entityStatMap == null) continue;

            EntityStatValue entityStatValue = entityStatMap.get(statIndex);
            if (entityStatValue == null) continue;

            values.add(entityStatValue.get());
        }

        return values.toArray(new Number[0]);
    }

    @Override
    public Optional<Class<?>[]> acceptsChange(@NotNull ChangeMode mode) {
        return Optional.of(new Class<?>[]{Number.class});
    }

    @Override
    public void change(@NotNull TriggerContext ctx, @NotNull ChangeMode changeMode, Object @NotNull [] changeWith) {
        Optional<? extends EntityStatType> single = this.entityStatType.getSingle(ctx);
        if (single.isEmpty()) return;
        int statIndex = EntityStatType.getAssetMap().getIndex(single.get().getId());

        float newValue;
        if (changeWith.length > 0 && changeWith[0] instanceof Number number) {
            newValue = number.floatValue();
        } else {
            newValue = 0f;
        }
        for (LivingEntity entity : this.entities.getArray(ctx)) {
            EntityStatMap statMap = EntityUtils.getEntityStatMap(entity);
            if (statMap == null) continue;

            if (changeMode == ChangeMode.RESET) {
                statMap.resetStatValue(statIndex);
                continue;
            }

            if (changeMode != ChangeMode.SET) {
                EntityStatValue stat = statMap.get(statIndex);
                if (stat == null) continue;
                float oldHealthValue = stat.get();

                if (changeMode == ChangeMode.ADD) {
                    newValue += oldHealthValue;
                } else if (changeMode == ChangeMode.REMOVE) {
                    newValue = oldHealthValue - newValue;
                } else if (changeMode == ChangeMode.DELETE) {
                    newValue = 0f;
                }
            }

            statMap.setStatValue(statIndex, newValue);
        }
    }

    @Override
    public boolean isSingle() {
        return this.entities.isSingle();
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return this.entityStatType.toString(ctx, debug) + " stat of " + this.entities.toString(ctx, debug);
    }

}
