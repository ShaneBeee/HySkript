package com.github.skriptdev.skript.plugin.elements.expressions.item;

import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ExprItemStack implements Expression<ItemStack> {

    public static void register(SkriptRegistration registration) {
        registration.newExpression(ExprItemStack.class, ItemStack.class, false,
                "[new] item[ ]stack of [%-number% of] %item%")
            .name("ItemStack")
            .description("Create a new itemstack from an item.")
            .examples("set {_stack} to itemstack of Food_Fish_Grilled",
                "add itemstack of 3 of Food_Fish_Grilled to inventory of player")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<Item> item;
    private Expression<Number> amount;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        if (expressions.length == 1) {
            this.item = (Expression<Item>) expressions[0];
        } else {
            this.amount = (Expression<Number>) expressions[0];
            this.item = (Expression<Item>) expressions[1];
        }
        return true;
    }

    @Override
    public ItemStack[] getValues(@NotNull TriggerContext ctx) {
        int amount = 1;
        if (this.amount != null) {
            Optional<? extends Number> single = this.amount.getSingle(ctx);
            if (single.isPresent()) amount = single.get().intValue();
        }
        Optional<? extends Item> item = this.item.getSingle(ctx);
        if (item.isEmpty()) return null;
        ItemStack itemStack = new ItemStack(item.get().getId(), amount);
        return new ItemStack[]{itemStack};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends ItemStack> getReturnType() {
        return ItemStack.class;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "itemstack of " + this.item.toString(ctx, debug);
    }

}
