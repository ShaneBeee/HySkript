package com.github.skriptdev.skript.plugin.elements.conditions.block;

import com.github.skriptdev.skript.api.hytale.Block;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.properties.ConditionalType;
import io.github.syst3ms.skriptparser.lang.properties.PropertyConditional;

public class CondBlockIsSolid extends PropertyConditional<Object> {

    public static void register(SkriptRegistration reg) {
        reg.newPropertyConditional(CondBlockIsSolid.class, "blocks/blocktypes",
                ConditionalType.BE, "solid")
            .name("Block is Solid")
            .description("Check if a Block/BlockType is solid.")
            .examples("if target block of player is solid:")
            .since("INSERT VERSION")
            .register();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean check(TriggerContext ctx) {
        return getPerformer().check(ctx, o -> {
            BlockType blockType;
            if (o instanceof BlockType bt) {
                blockType = bt;
            } else if (o instanceof Block block) {
                blockType = block.getType();
            } else {
                return false;
            }
            return blockType.getMaterial() == BlockMaterial.Solid;
        }, isNegated());
    }

}
