package com.github.skriptdev.skript.plugin.elements.types;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import io.github.syst3ms.skriptparser.types.conversions.Converters;

import java.util.Optional;

public class DefaultConverters {

    public static void register() {
        inventory();
    }

    private static void inventory() {
        // Item to BlockType
        Converters.registerConverter(Item.class, BlockType.class, (item) -> {
            if (item.hasBlockType()) {
                String blockId = item.getBlockId();
                BlockType asset = BlockType.getAssetMap().getAsset(blockId);
                if (asset != null) return Optional.of(asset);
                return Optional.empty();
            }
            return Optional.empty();
        });

        // BlockType to Item
        Converters.registerConverter(BlockType.class, Item.class, (blockType) -> {
            Item item = blockType.getItem();
            if (item != null) return Optional.of(item);
            return Optional.empty();
        });
    }

}
