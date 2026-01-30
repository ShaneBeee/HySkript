package com.github.skriptdev.skript.plugin.elements.expressions.item;

import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.Nullable;

public class ExprItemType extends PropertyExpression<ItemStack, Item> {

    public static void register(SkriptRegistration registration) {
        registration.newPropertyExpression(ExprItemType.class, Item.class,
                "item[[ ]type]", "itemstack")
            .name("Item Type")
            .description("Returns the item type of an itemstack.")
            .examples("set {_item} to itemtype of {_item}")
            .since("INSERT VERSION")
            .register();
    }

    @Override
    public @Nullable Item getProperty(ItemStack owner) {
        return owner.getItem();
    }

}
