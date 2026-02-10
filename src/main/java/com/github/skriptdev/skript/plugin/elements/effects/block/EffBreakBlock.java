package com.github.skriptdev.skript.plugin.elements.effects.block;

import com.github.skriptdev.skript.api.hytale.Block;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

public class EffBreakBlock extends Effect {

    public static void register(SkriptRegistration reg) {
        reg.newEffect(EffBreakBlock.class, "break %blocks% [with settings %number%]")
            .name("Break Block")
            .description("Breaks the specified blocks.",
                "**Settings**:",
                "I don't really know what this does yet, but from testing:",
                "- `-1` = Breaks the block without particles and performs update of block above (ie: break if it can't be supported).",
                "- `0-3` = Breaks the block with particles.",
                "- `4+` = Breaks the block without particles.",
                "- `256` = Breaks the block with particles and performs update of block above (default).",
                "- Any other number doesn't do anything different than the few stated above.")
            .examples("break target block of player with settings 0")
            .since("1.0.0")
            .register();
    }

    private Expression<Block> blocks;
    private Expression<Number> settings;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.blocks = (Expression<Block>) expressions[0];
        if (expressions.length > 1) {
            this.settings = (Expression<Number>) expressions[1];
        }
        return true;
    }

    @Override
    protected void execute(@NotNull TriggerContext ctx) {
        int settings = 256; // Break block with particles and neighboring updates.
        if (this.settings != null) {
            Number number = this.settings.getSingle(ctx).orElse(null);
            if (number != null) settings = number.intValue();
        }
        for (Block block : this.blocks.getArray(ctx)) {
            block.breakBlock(settings);
        }
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String settings = this.settings == null ? "" : " with settings " + this.settings.toString(ctx, debug);
        return "break " + this.blocks.toString(ctx, debug) + settings;
    }

}
