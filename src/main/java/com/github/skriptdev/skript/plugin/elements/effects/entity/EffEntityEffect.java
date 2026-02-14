package com.github.skriptdev.skript.plugin.elements.effects.entity;

import com.github.skriptdev.skript.api.hytale.utils.EntityUtils;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class EffEntityEffect extends Effect {

    public static void register(SkriptRegistration reg) {
        reg.newEffect(EffEntityEffect.class,
                "apply [entity[ ]effect] %entityeffect% to %livingentities% for %duration% [to (:extend|:overwrite)]",
                "apply infinite [entity[ ]effect] %entityeffect% to %livingentities%")
            .name("Apply Entity Effect")
            .description("Apply an entity effect to an entity for a given duration.",
                "You have the option to extend or overwrite any current effect (Default will ignore).",
                "You can also apply an infinite effect, which will last forever.")
            .examples("apply freeze to player for 1 minute",
                "apply infinite entity effect freeze to all players")
            .since("1.0.0")
            .register();
    }

    private Expression<EntityEffect> effects;
    private Expression<LivingEntity> entities;
    private Expression<Duration> duration;
    private OverlapBehavior behavior;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.effects = (Expression<EntityEffect>) expressions[0];
        this.entities = (Expression<LivingEntity>) expressions[1];
        if (matchedPattern == 0) {
            this.duration = (Expression<Duration>) expressions[2];
        }
        this.behavior = parseContext.hasMark("extend") ? OverlapBehavior.EXTEND :
            parseContext.hasMark("overwrite") ? OverlapBehavior.OVERWRITE : OverlapBehavior.IGNORE;
        return true;
    }

    @Override
    protected void execute(@NotNull TriggerContext ctx) {
        Duration duration = this.duration.getSingle(ctx).orElse(null);

        EntityEffect entityEffect = this.effects.getSingle(ctx).orElse(null);
        if (entityEffect == null) return;
        int index = EntityEffect.getAssetMap().getIndex(entityEffect.getId());

        for (LivingEntity entity : this.entities.getArray(ctx)) {
            EffectControllerComponent component = EntityUtils.getComponent(entity, EffectControllerComponent.getComponentType());
            if (component == null) continue;

            Ref<EntityStore> reference = entity.getReference();
            if (reference == null) continue;

            Store<EntityStore> store = reference.getStore();
            if (duration == null) {
                component.addInfiniteEffect(reference, index, entityEffect, store);
            } else {
                float seconds = Math.max(duration.toSeconds(), 0);
                component.addEffect(reference, entityEffect, seconds, this.behavior, store);
            }
        }
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        if (this.duration == null) {
            return "apply infinite entity effect " + this.effects.toString(ctx, debug) + " to " + this.entities.toString(ctx, debug);
        }
        String behavior = " to " + this.behavior.name();
        return "apply entity effect " + this.effects.toString(ctx, debug) + " to " + this.entities.toString(ctx, debug) +
            " for " + this.duration.toString(ctx, debug) + behavior;
    }

}
