package com.github.skriptdev.skript.plugin.elements.effects.entity;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.Frozen;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

public class EffFreeze extends Effect {

    public static void register(SkriptRegistration reg) {
        reg.newEffect(EffFreeze.class, "freeze %livingentities%",
                "unfreeze %livingentities%")
            .name("Freeze Entity")
            .description("Freeze or unfreeze the specified entities.",
                "This doesn't appear to work on players.")
            .examples("freeze entities in radius 10 around player",
                "unfreeze {_e}")
            .since("INSERT VERSION")
            .register();
    }

    private boolean freeze;
    private Expression<LivingEntity> livingEntities;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.freeze = matchedPattern == 0;
        this.livingEntities = (Expression<LivingEntity>) expressions[0];
        return true;
    }

    @Override
    protected void execute(@NotNull TriggerContext ctx) {
        for (LivingEntity livingEntity : this.livingEntities.getArray(ctx)) {
            Ref<EntityStore> reference = livingEntity.getReference();
            if (reference == null) continue;

            Store<EntityStore> store = reference.getStore();
            if (this.freeze) {
                store.ensureComponent(reference, Frozen.getComponentType());
            } else {
                store.removeComponentIfExists(reference, Frozen.getComponentType());
            }
        }
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String freeze = this.freeze ? "freeze" : "unfreeze";
        return freeze + " " + this.livingEntities.toString(ctx, debug);
    }

}
