package com.github.skriptdev.skript.plugin.elements.expressions.item;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExprItemsInInventory implements Expression<ItemStack> {

    public static void register(SkriptRegistration reg) {
        reg.newExpression(ExprItemsInInventory.class, ItemStack.class, false,
                "itemstacks in %inventory/itemcontainer%")
            .name("Itemstacks in Inventory/ItemContainer")
            .description("Returns all itemstacks in an Inventory or ItemContainer.")
            .examples("set {_items::*} to itemstacks in inventory of player",
                "loop itemstacks in inventory of player:")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<?> container;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.container = expressions[0];
        return true;
    }


    @Override
    public ItemStack[] getValues(@NotNull TriggerContext ctx) {
        List<ItemStack> itemStacks = new ArrayList<>();

        Optional<?> single = this.container.getSingle(ctx);
        if (single.isEmpty()) return null;

        Object o = single.get();
        if (o instanceof Inventory inventory) {
            CombinedItemContainer combinedEverything = inventory.getCombinedEverything();
            combinedEverything.forEach((_, itemStack) -> itemStacks.add(itemStack));
        } else if (o instanceof ItemContainer itemContainer) {
            itemContainer.forEach((_, itemStack) -> itemStacks.add(itemStack));
        }

        return itemStacks.toArray(new ItemStack[0]);
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "itemstacks in " + this.container.toString(ctx, debug);
    }

}
