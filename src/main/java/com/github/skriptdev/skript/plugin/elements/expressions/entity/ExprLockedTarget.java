package com.github.skriptdev.skript.plugin.elements.expressions.entity;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.MarkedEntitySupport;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import io.github.syst3ms.skriptparser.util.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExprLockedTarget implements Expression<Object> {

    public static void register(SkriptRegistration reg) {
        reg.newExpression(ExprLockedTarget.class, Object.class, true,
                "locked target entity of %npcentities%",
                "locked target entity (refs|references) of %npcentities%")
            .name("NPCEntity Locked Target")
            .description("Get/set/clear the locked target entity of an NPCEntity.",
                "This represents the entity an NPC is locked on to, such as a skeleton attacking a player.")
            .since("INSERT VERSION")
            .register();
    }

    private int pattern;
    private Expression<NPCEntity> npcentities;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.pattern = matchedPattern;
        this.npcentities = (Expression<NPCEntity>) expressions[0];
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Object[] getValues(@NotNull TriggerContext ctx) {
        if (this.pattern == 0) {
            List<Entity> targets = new ArrayList<>();
            for (Ref<EntityStore> ref : getRefs(ctx)) {
                Entity entity1 = EntityUtils.getEntity(ref, ref.getStore());
                if (entity1 != null) {
                    targets.add(entity1);
                }
            }
            return targets.toArray();
        } else {
            return getRefs(ctx).toArray();
        }
    }

    @Override
    public Optional<Class<?>[]> acceptsChange(@NotNull ChangeMode mode) {
        if (mode == ChangeMode.SET || mode == ChangeMode.DELETE) {
            return CollectionUtils.optionalArrayOf(Entity.class, Ref.class);
        }
        return Optional.empty();
    }

    @SuppressWarnings({"ConstantValue", "rawtypes", "unchecked"})
    @Override
    public void change(@NotNull TriggerContext ctx, @NotNull ChangeMode changeMode, Object @NotNull [] changeWith) {
        Ref<EntityStore> ref = null;
        if (changeMode == ChangeMode.SET && changeWith != null && changeWith.length > 0) {
            if (changeWith[0] instanceof Entity entity) {
                ref = entity.getReference();
            } else if (changeWith[0] instanceof Ref ref1) {
                ref = (Ref<EntityStore>) ref1;
            }
        }

        for (NPCEntity npc : this.npcentities.getArray(ctx)) {
            Role role = npc.getRole();
            assert role != null;
            MarkedEntitySupport markedEntitySupport = role.getMarkedEntitySupport();
            markedEntitySupport.setMarkedEntity("LockedTarget", ref);
        }
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "";
    }

    private List<Ref<EntityStore>> getRefs(TriggerContext ctx) {
        List<Ref<EntityStore>> refs = new ArrayList<>();
        for (NPCEntity entity : this.npcentities.getArray(ctx)) {
            Role role = entity.getRole();
            assert role != null;

            MarkedEntitySupport markedEntitySupport = role.getMarkedEntitySupport();
            Ref<EntityStore> lockedTarget = markedEntitySupport.getMarkedEntityRef("LockedTarget");
            refs.add(lockedTarget);
        }
        return refs;
    }

}
