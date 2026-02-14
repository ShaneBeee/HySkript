package com.github.skriptdev.skript.plugin.elements.expressions.entity;

import com.github.skriptdev.skript.api.hytale.utils.EntityUtils;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ExprEntityVelocity implements Expression<Vector3d> {

    public static void register(SkriptRegistration reg) {
        reg.newExpression(ExprEntityVelocity.class, Vector3d.class, false,
                "velocit(y|ies) of %entities%")
            .name("Entity Velocity")
            .description("Get/set/add to the velocity of an entity.")
            .examples("set velocity of player to vector3d(1, 2, 3)",
                "add vector3d(1, 2, 3) to velocity of target entity of player")
            .since("1.0.0")
            .register();
    }

    private Expression<Entity> entities;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.entities = (Expression<Entity>) expressions[0];
        return true;
    }

    @Override
    public Vector3d[] getValues(@NotNull TriggerContext ctx) {
        Entity[] entityArray = this.entities.getArray(ctx);
        Vector3d[] velocities = new Vector3d[entityArray.length];

        for (int i = 0; i < entityArray.length; i++) {
            Velocity component = EntityUtils.getComponent(entityArray[i], Velocity.getComponentType());
            if (component == null) continue;
            velocities[i] = component.getVelocity();
        }

        return velocities;
    }

    @Override
    public Optional<Class<?>[]> acceptsChange(@NotNull ChangeMode mode) {
        if (mode == ChangeMode.SET || mode == ChangeMode.RESET || mode == ChangeMode.ADD)
            return Optional.of(new Class<?>[]{Vector3d.class});
        return Optional.empty();
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public void change(@NotNull TriggerContext ctx, @NotNull ChangeMode changeMode, Object @NotNull [] changeWith) {
        Vector3d velocity = Vector3d.ZERO;
        if (changeWith != null && changeWith[0] instanceof Vector3d vec) {
            velocity = vec;
        }

        for (Entity entity : this.entities.getArray(ctx)) {
            Velocity component = EntityUtils.getComponent(entity, Velocity.getComponentType());
            if (component == null) continue;

            switch (changeMode) {
                case SET, RESET -> component.addInstruction(velocity, new VelocityConfig(), ChangeVelocityType.Set);
                case ADD -> component.addInstruction(velocity, new VelocityConfig(), ChangeVelocityType.Add);
            }
        }
    }

    @Override
    public boolean isSingle() {
        return this.entities.isSingle();
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String plural = this.entities.isSingle() ? "velocity" : "velocities";
        return plural + " of " + this.entities.toString(ctx, debug);
    }

}
