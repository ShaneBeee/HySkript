package com.github.skriptdev.skript.plugin.elements.conditions;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.Frozen;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.properties.ConditionalType;
import io.github.syst3ms.skriptparser.lang.properties.PropertyConditional;
import org.jetbrains.annotations.NotNull;

public class CondEntityIsFrozen extends PropertyConditional<LivingEntity> {

    public static void register(SkriptRegistration reg) {
        reg.newPropertyConditional(CondEntityIsFrozen.class,
            "livingentities",
            ConditionalType.BE,
            "frozen")
            .name("Entity is Frozen")
            .description("Checks if the living entities are frozen.")
            .since("INSERT VERSION")
            .register();
    }

    @Override
    public boolean check(@NotNull TriggerContext ctx) {
        return this.getPerformer().check(ctx, livingEntity -> {
            Ref<EntityStore> reference = livingEntity.getReference();
            if (reference == null) return false;

            Store<EntityStore> store = reference.getStore();
            Frozen component = store.getComponent(reference, Frozen.getComponentType());
            return component != null;
        }, isNegated());
    }

}
