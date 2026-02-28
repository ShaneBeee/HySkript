package com.github.skriptdev.skript.plugin.elements.expressions.block;

import com.github.skriptdev.skript.api.hytale.Block;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.math.vector.Vector3i;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import io.github.syst3ms.skriptparser.util.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ExprBlockRotation implements Expression<Vector3i> {

    public static void register(SkriptRegistration reg) {
        reg.newExpression(ExprBlockRotation.class, Vector3i.class, true,
                "block rotation of %block%")
            .name("Block Rotation")
            .description("Get/set the rotation of a block.",
                "Represented as a vector3i(pitch, yaw, roll).",
                "Do note that only increments of 90 degrees are supported (0, 90, 180, 270).",
                "Caution: This allows you to rotate blocks in ways they're not meant to... have fun, be careful!")
            .examples("set block rotation of block at player to vector3i(0, 180, 0)")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<Block> block;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.block = (Expression<Block>) expressions[0];
        return true;
    }

    @Override
    public Vector3i[] getValues(@NotNull TriggerContext ctx) {
        Block block = this.block.getSingle(ctx).orElse(null);
        if (block == null) return null;

        return new Vector3i[]{block.getRotation()};
    }

    @Override
    public Optional<Class<?>[]> acceptsChange(@NotNull ChangeMode mode) {
        if (mode == ChangeMode.SET) return CollectionUtils.optionalArrayOf(Vector3i.class);
        return Optional.empty();
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public void change(@NotNull TriggerContext ctx, @NotNull ChangeMode changeMode, Object @NotNull [] changeWith) {
        if (changeWith == null || changeWith.length == 0 || !(changeWith[0] instanceof Vector3i rotation)) return;

        this.block.getSingle(ctx).ifPresent(block -> block.setRotation(rotation));
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "block rotation of " + this.block.toString(ctx, debug);
    }

}
