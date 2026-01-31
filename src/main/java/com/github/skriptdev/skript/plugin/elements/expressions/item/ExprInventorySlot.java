package com.github.skriptdev.skript.plugin.elements.expressions.item;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ExprInventorySlot implements Expression<ItemStack> {

    public static void register(SkriptRegistration reg) {
        reg.newExpression(ExprInventorySlot.class, ItemStack.class, true,
                "itemstack in slot %number% of %inventory/itemcontainer%")
            .name("Itemstack in Inventory Slot")
            .description("Get/set/delete the itemstack in a slot of an Inventory or ItemContainer.",
                "When using Inventory, this will combine all ItemContainers in the inventory and grab from there.")
            .examples("set {_item} to itemstack in slot 3 of inventory of player",
                "delete itemstack in slot 3 of inventory of player",
                "delete itemstack in slot 2 of hotbar item container of inventory of player",
                "set itemstack in slot 1 of hotbar item container of inventory of player to itemstack of ingredient_poop")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<Number> slot;
    private Expression<?> container;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.slot = (Expression<Number>) expressions[0];
        this.container = expressions[1];
        return true;
    }

    @Override
    public ItemStack[] getValues(@NotNull TriggerContext ctx) {
        Number number = this.slot.getSingle(ctx).orElse(null);
        Object o = this.container.getSingle(ctx).orElse(null);
        if (o == null || number == null) return null;
        int i = number.intValue();

        ItemStack itemStack = null;
        if (o instanceof Inventory inventory) {
            itemStack = inventory.getCombinedEverything().getItemStack(number.shortValue());
        } else if (o instanceof ItemContainer itemContainer) {
            itemStack = itemContainer.getItemStack(number.shortValue());
        }
        if (itemStack == null) return null;

        return new ItemStack[]{itemStack};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Optional<Class<?>[]> acceptsChange(@NotNull ChangeMode mode) {
        if (mode == ChangeMode.SET || mode == ChangeMode.DELETE) return Optional.of(new Class<?>[]{ItemStack.class});
        return Optional.empty();
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public void change(@NotNull TriggerContext ctx, @NotNull ChangeMode changeMode, Object @NotNull [] changeWith) {
        if (changeMode == ChangeMode.SET && changeWith == null) return;
        Number numSlot = this.slot.getSingle(ctx).orElse(null);
        Object o = this.container.getSingle(ctx).orElse(null);
        if (numSlot == null || o == null) return;

        if (changeMode == ChangeMode.SET) {
            ItemStack itemStack = (ItemStack) changeWith[0];
            if (o instanceof Inventory inventory) {
                inventory.getCombinedEverything().setItemStackForSlot(numSlot.shortValue(), itemStack);
            } else if (o instanceof ItemContainer itemContainer) {
                itemContainer.setItemStackForSlot(numSlot.shortValue(), itemStack);
            }
        } else if (changeMode == ChangeMode.DELETE) {
            if (o instanceof Inventory inventory) {
                inventory.getCombinedEverything().setItemStackForSlot(numSlot.shortValue(), null);
            } else if (o instanceof ItemContainer itemContainer) {
                itemContainer.setItemStackForSlot(numSlot.shortValue(), null);
            }
        }

    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "itemstack in slot" + this.slot.toString(ctx, debug) + " of " + this.container.toString(ctx, debug);
    }

}
