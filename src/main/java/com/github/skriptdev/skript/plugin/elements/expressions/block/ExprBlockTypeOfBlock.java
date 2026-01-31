package com.github.skriptdev.skript.plugin.elements.expressions.block;

import com.github.skriptdev.skript.api.hytale.Block;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExprBlockTypeOfBlock implements Expression<BlockType> {

    public static void register(SkriptRegistration reg) {
        reg.newExpression(ExprBlockTypeOfBlock.class, BlockType.class, false,
                "block[ ]type of %blocks%")
            .name("Block Type of Block")
            .description("Get/set the BlockType of a block.")
            .examples("on player block break:",
                "\tif context-blocktype = ore_copper_stone:",
                "\t\tcancel event",
                "\t\tset blocktype of context-block to rock_Stone",
                "",
                "set blocktype of {_block} to empty # This is how Hytale refers to air")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<Block> blocks;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.blocks = (Expression<Block>) expressions[0];
        return true;
    }

    @Override
    public BlockType[] getValues(@NotNull TriggerContext ctx) {
        List<BlockType> blockTypes = new ArrayList<>();
        for (Block block : this.blocks.getArray(ctx)) {
            blockTypes.add(block.getType());
        }
        return blockTypes.toArray(BlockType[]::new);
    }

    @Override
    public Optional<Class<?>[]> acceptsChange(@NotNull ChangeMode mode) {
        if (mode == ChangeMode.SET) return Optional.of(new Class<?>[]{BlockType.class});
        return Optional.empty();
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public void change(@NotNull TriggerContext ctx, @NotNull ChangeMode changeMode, Object @NotNull [] changeWith) {
        if (changeMode != ChangeMode.SET) return;
        if (changeWith == null) return;

        if (!(changeWith[0] instanceof BlockType type)) return;

        for (Block block : this.blocks.getArray(ctx)) {
            block.setType(type);
        }
    }

    @Override
    public boolean isSingle() {
        return this.blocks.isSingle();
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "blocktype of " + this.blocks.toString(ctx, debug);
    }

}
