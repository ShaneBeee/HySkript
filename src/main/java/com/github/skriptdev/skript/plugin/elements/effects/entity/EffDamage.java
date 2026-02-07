package com.github.skriptdev.skript.plugin.elements.effects.entity;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.api.utils.Utils;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class EffDamage extends Effect {

    @SuppressWarnings("NotNullFieldNotInitialized")
    private static @NotNull DamageCause DEFAULT_CAUSE;

    public static void register(SkriptRegistration reg) {
        DamageCause cause = DamageCause.getAssetMap().getAsset("Environment");
        if (cause == null) {
            Utils.error("Default damage cause 'Environmental' not found. Skipping EffDamage registration.");
            return;
        }
        DEFAULT_CAUSE = cause;
        reg.newEffect(EffDamage.class, "damage %entities% by %number%",
                "make %entity% damage %entities% by %number%",
                "damage %entities% by %number% with cause %damagecause%",
                "make %entity% damage %entities% by %number% with cause %damagecause%")
            .name("Damage")
            .description("Damages entities with a specified amount of damage.")
            .examples("damage player by 5",
                "make player damage {_e} by 5",
                "damage entities in radius 5 around player by 5 with cause Environmental",
                "make player damage {_e::*} by 5 with cause Environmental")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<Entity> entities;
    private Expression<Entity> sourceEntity;
    private Expression<Number> damage;
    private Expression<DamageCause> cause;
    private int pattern;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.pattern = matchedPattern;
        switch (matchedPattern) {
            case 0 -> {
                this.entities = (Expression<Entity>) expressions[0];
                this.damage = (Expression<Number>) expressions[1];
            }
            case 1 -> {
                this.sourceEntity = (Expression<Entity>) expressions[0];
                this.entities = (Expression<Entity>) expressions[1];
                this.damage = (Expression<Number>) expressions[2];
            }
            case 2 -> {
                this.entities = (Expression<Entity>) expressions[0];
                this.damage = (Expression<Number>) expressions[1];
                this.cause = (Expression<DamageCause>) expressions[2];
            }
            case 3 -> {
                this.sourceEntity = (Expression<Entity>) expressions[0];
                this.entities = (Expression<Entity>) expressions[1];
                this.damage = (Expression<Number>) expressions[2];
                this.cause = (Expression<DamageCause>) expressions[3];
            }
            default -> {
                Utils.error("Invalid pattern for EffDamage effect: " + matchedPattern);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void execute(@NotNull TriggerContext ctx) {
        Number number = this.damage.getSingle(ctx).orElse(null);
        if (number == null) return;

        float damage = number.floatValue();
        DamageCause cause;
        if (this.cause != null) {
            cause = this.cause.getSingle(ctx).orElse(null);
            if (cause == null) cause = DEFAULT_CAUSE;
        } else {
            cause = DEFAULT_CAUSE;
        }

        Damage.Source source = Damage.NULL_SOURCE;
        if (this.sourceEntity != null) {
            Optional<? extends Entity> single = this.sourceEntity.getSingle(ctx);
            if (single.isPresent()) {
                Entity sourceEntity = single.get();
                Ref<EntityStore> reference = sourceEntity.getReference();
                if (reference != null) {
                    source = new Damage.EntitySource(sourceEntity.getReference());
                }
            }
        }

        for (Entity entity : this.entities.getArray(ctx)) {
            if (entity instanceof LivingEntity livingEntity) {
                causeDamage(livingEntity, source, cause, damage);
            }
        }
    }

    private void causeDamage(@NotNull LivingEntity entity, @NotNull Damage.Source source, @NotNull DamageCause cause, float amount) {
        Ref<EntityStore> reference = entity.getReference();
        if (reference == null) return;

        Store<EntityStore> store = reference.getStore();
        Damage damage = new Damage(source, cause, amount);

        DamageSystems.executeDamage(reference, store, damage);
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return switch (this.pattern) {
            case 0 -> "damage " + this.entities.toString(ctx, debug) + " by " + this.damage.toString(ctx, debug);
            case 1 ->
                "make " + this.sourceEntity.toString(ctx, debug) + " damage " + this.entities.toString(ctx, debug) +
                    " by " + this.damage.toString(ctx, debug);
            case 2 -> "damage " + this.entities.toString(ctx, debug) + " by " + this.damage.toString(ctx, debug) +
                " with cause " + this.cause.toString(ctx, debug);
            case 3 ->
                "make " + this.sourceEntity.toString(ctx, debug) + " damage " + this.entities.toString(ctx, debug) +
                    " by " + this.damage.toString(ctx, debug) + " with cause " + this.cause.toString(ctx, debug);
            default -> throw new IllegalStateException("Unexpected value: " + this.pattern);
        };
    }

}
