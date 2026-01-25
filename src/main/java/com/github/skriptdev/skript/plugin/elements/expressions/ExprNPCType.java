package com.github.skriptdev.skript.plugin.elements.expressions;

import com.github.skriptdev.skript.api.skript.registration.NPCRegistry;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExprNPCType extends PropertyExpression<NPCRegistry.NPCRole, Entity> {

    public static void register(SkriptRegistration registration) {
        registration.newPropertyExpression(ExprNPCType.class, NPCRegistry.NPCRole.class,
                "entities", "npc type")
            .name("NPC Type of Entity")
            .description("Returns the NPC type of an NPC entity.")
            .examples("set {_type} to npc type of target entity",
                "if npc type of {_entity} = sheep:")
            .since("INSERT VERSION")
            .register();
    }

    @Override
    public @Nullable NPCRegistry.NPCRole getProperty(@NotNull Entity owner) {
        if (owner instanceof NPCEntity npcEntity) {
            return NPCRegistry.getByIndex(npcEntity.getRoleIndex());
        }
        return null;
    }

}
