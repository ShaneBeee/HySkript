package com.github.skriptdev.skript.api.hytale.utils;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Quick utility methods for working with AssetStore
 */
public class AssetStoreUtils {

    /**
     * Get a BlockType from ID
     *
     * @param blockId ID of the BlockType
     * @return BlockType from ID if found, otherwise null
     */
    public static @Nullable BlockType getBlockType(@NotNull String blockId) {
        return BlockType.getAssetMap().getAsset(blockId);
    }

    /**
     * Get a BlockType from ItemStack
     *
     * @param itemStack ItemStack to get BlockType from
     * @return BlockType from ItemStack if found, otherwise null
     */
    public static @Nullable BlockType getBlockType(@NotNull ItemStack itemStack) {
        return getBlockType(itemStack.getItem());
    }

    /**
     * Get a BlockType from Item
     *
     * @param item Item to get BlockType from
     * @return BlockType from Item if found, otherwise null
     */
    public static @Nullable BlockType getBlockType(@NotNull Item item) {
        if (item.hasBlockType()) return getBlockType(item.getBlockId());
        return null;
    }

}
