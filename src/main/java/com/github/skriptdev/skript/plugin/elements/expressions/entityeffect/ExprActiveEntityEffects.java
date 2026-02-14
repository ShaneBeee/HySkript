package com.github.skriptdev.skript.plugin.elements.expressions.entityeffect;

import com.github.skriptdev.skript.api.hytale.utils.EntityUtils;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.effect.ActiveEntityEffect;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExprActiveEntityEffects implements Expression<ActiveEntityEffect> {

    public static void register(SkriptRegistration reg) {
        reg.newExpression(ExprActiveEntityEffects.class, ActiveEntityEffect.class, false,
                "active entity effects of %livingentities%")
            .name("Active Entity Effects")
            .description("Returns all active effects of an entity.",
                "You can also remove an EntityEffect from this list or clear all effects at once.")
            .examples("set {_a::*} to active entity effects of player",
                "remove freeze from active entity effects of player",
                "clear active entity effects of all players")
            .since("1.0.0")
            .register();
    }

    private Expression<LivingEntity> entities;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.entities = (Expression<LivingEntity>) expressions[0];
        return true;
    }

    @Override
    public ActiveEntityEffect[] getValues(@NotNull TriggerContext ctx) {
        List<ActiveEntityEffect> effects = new ArrayList<>();

        for (LivingEntity entity : this.entities.getArray(ctx)) {
            EffectControllerComponent component = EntityUtils.getComponent(entity, EffectControllerComponent.getComponentType());
            if (component == null) continue;

            component.getActiveEffects().forEach(effects::add);
        }
        return effects.toArray(ActiveEntityEffect[]::new);
    }

    @Override
    public Optional<Class<?>[]> acceptsChange(@NotNull ChangeMode mode) {
        if (mode == ChangeMode.DELETE || mode == ChangeMode.REMOVE) {
            return Optional.of(new Class<?>[]{EntityEffect[].class});
        }
        return Optional.empty();
    }

    @SuppressWarnings({"ConstantValue", "RedundantLengthCheck"})
    @Override
    public void change(@NotNull TriggerContext ctx, @NotNull ChangeMode changeMode, Object @NotNull [] changeWith) {

        List<Integer> effectIndexes = new ArrayList<>();
        if (changeMode != null && changeWith.length > 0) {
            for (Object o : changeWith) { // TODO gotta figure out why we keep getting casting errors from Object to type
                if (o instanceof EntityEffect entityEffect) {
                    int index = EntityEffect.getAssetMap().getIndex(entityEffect.getId());
                    effectIndexes.add(index);
                }
            }
        }

        for (LivingEntity livingEntity : this.entities.getArray(ctx)) {
            Ref<EntityStore> reference = livingEntity.getReference();
            if (reference == null) continue;

            Store<EntityStore> store = reference.getStore();
            EffectControllerComponent component = EntityUtils.getComponent(livingEntity,
                EffectControllerComponent.getComponentType());
            if (component == null) continue;

            if (changeMode == ChangeMode.DELETE) {
                component.clearEffects(reference, store);
            } else if (changeMode == ChangeMode.REMOVE && !effectIndexes.isEmpty()) {
                for (int index : effectIndexes) {
                    component.removeEffect(reference, index, store);
                }
            }
        }
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "active entity effects of " + this.entities.toString(ctx, debug);
    }

}
