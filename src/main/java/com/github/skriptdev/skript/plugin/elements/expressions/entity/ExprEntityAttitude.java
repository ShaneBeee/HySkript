package com.github.skriptdev.skript.plugin.elements.expressions.entity;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.WorldSupport;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

public class ExprEntityAttitude implements Expression<Attitude> {

    public static void register(SkriptRegistration reg) {
        reg.newExpression(ExprEntityAttitude.class, Attitude.class, true,
                "attitude of %npcentity% towards %entity%")
            .name("Entity Attitude")
            .description("Get the attitude of an NPC towards an entity.",
                "Cannot be set, instead use the Entity Attitude effect to apply a timed attitude change.")
            .examples("set {_a} to attitude of target entity of player towards player",
                "if attitude of target entity of player towards player = neutral:")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<NPCEntity> npcEntity;
    private Expression<Entity> target;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.npcEntity = (Expression<NPCEntity>) expressions[0];
        this.target = (Expression<Entity>) expressions[1];
        return true;
    }

    @Override
    public Attitude[] getValues(@NotNull TriggerContext ctx) {
        NPCEntity npc = this.npcEntity.getSingle(ctx).orElse(null);
        Entity target = this.target.getSingle(ctx).orElse(null);
        if (npc == null || target == null) return null;

        Role role = npc.getRole();
        if (role == null) return null;

        Ref<EntityStore> npcRef = npc.getReference();
        Ref<EntityStore> targetRef = target.getReference();
        if (npcRef == null || targetRef == null) return null;

        WorldSupport worldSupport = role.getWorldSupport();
        Attitude attitude = worldSupport.getAttitude(npcRef, targetRef, npcRef.getStore());
        return new Attitude[]{attitude};
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "attitude of " + this.npcEntity.toString(ctx, debug) + " towards " + this.target.toString(ctx, debug);
    }

}
