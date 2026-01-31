package com.github.skriptdev.skript.plugin.elements.expressions.entity;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ExprTargetEntityOfEntity implements Expression<Entity> {

    public static void register(SkriptRegistration reg) {
        reg.newExpression(ExprTargetEntityOfEntity.class, Entity.class, true,
                "target entity of %entity%")
            .name("Target Entity of Entity")
            .description("Returns the target entity of the given entity.")
            .examples("set {_target} to target entity of player")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<Entity> entity;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.entity = (Expression<Entity>) expressions[0];
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Entity[] getValues(@NotNull TriggerContext ctx) {
        Optional<? extends Entity> single = this.entity.getSingle(ctx);
        if (single.isEmpty()) return null;

        Entity entity = single.get();
        Ref<EntityStore> ref = entity.getReference();
        World world = entity.getWorld();
        if (world == null || ref == null) return null;

        Store<EntityStore> store = world.getEntityStore().getStore();
        Ref<EntityStore> targetEntity = TargetUtil.getTargetEntity(ref, store);
        if (targetEntity == null || !targetEntity.isValid()) return null;

        // TODO better handling of this deprecation
        Entity target = EntityUtils.getEntity(targetEntity, store);

        return new Entity[]{target};
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "target entity of " + this.entity.toString(ctx, debug);
    }

}
