package com.github.skriptdev.skript.plugin.elements.effects.entity;

import com.github.skriptdev.skript.api.hytale.utils.EntityUtils;
import com.github.skriptdev.skript.api.hytale.utils.StoreUtils;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.modules.projectile.config.ProjectileConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

public class EffShoot extends Effect {

    // TODO it doesn't actually work, hopefully it's just a Hytale Bug
    public static void register(SkriptRegistration reg) {
        reg.newEffect(EffShoot.class,
                "make %livingentity% shoot %projectileconfig%",
                "make %livingentity% shoot %projectileconfig% in direction %vector3d%")
            .name("Shoot")
            .description("Make a LivingEntity shoot a projectile.")
            .since("INSERT VERSION");
        //.register();
    }

    private Expression<LivingEntity> livingEntity;
    private Expression<ProjectileConfig> projectile;
    private Expression<Vector3d> direction;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.livingEntity = (Expression<LivingEntity>) expressions[0];
        this.projectile = (Expression<ProjectileConfig>) expressions[1];
        if (matchedPattern == 1) {
            this.direction = (Expression<Vector3d>) expressions[2];
        }
        return true;
    }

    @Override
    protected void execute(@NotNull TriggerContext ctx) {
        LivingEntity entity = this.livingEntity.getSingle(ctx).orElse(null);
        ProjectileConfig config = this.projectile.getSingle(ctx).orElse(null);
        if (entity == null || config == null) return;
        Ref<EntityStore> ref = entity.getReference();
        if (ref == null) return;
        Store<EntityStore> store = ref.getStore();
        CommandBuffer<EntityStore> buffer = StoreUtils.getCommandBuffer(store);

        TransformComponent component = EntityUtils.getComponent(entity, TransformComponent.getComponentType());
        if (component == null) return;

        Vector3d pos = component.getTransform().getPosition().clone();
        Vector3d dir = component.getTransform().getDirection().clone();

        if (this.direction != null) {
            Vector3d vector3d = this.direction.getSingle(ctx).orElse(null);
            if (vector3d != null) {
                dir = vector3d;
            }
        }

        ProjectileModule.get().spawnProjectile(null, buffer, config, pos, dir);
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "make " + this.livingEntity.toString(ctx, debug) + " shoot " + this.projectile.toString(ctx, debug);
    }

}
