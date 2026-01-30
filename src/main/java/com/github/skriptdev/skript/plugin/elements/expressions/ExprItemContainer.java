package com.github.skriptdev.skript.plugin.elements.expressions;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ExprItemContainer implements Expression<ItemContainer> {

    public static void register(SkriptRegistration reg) {
        reg.newExpression(ExprItemContainer.class, ItemContainer.class, true,
                "storage item container of %inventory%",
                "armor item container of %inventory%",
                "hot[ ]bar item container of %inventory%",
                "utility item container of %inventory%",
                "tools item container of %inventory%",
                "backpack item container of %inventory%",
                "combined everything item container of %inventory%")
            .name("Item Container of Inventory")
            .description("Returns different item containers of an inventory.")
            .since("INSERT VERSION")
            .register();
    }

    private int pattern;
    private Expression<Inventory> inventory;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.pattern = matchedPattern;
        this.inventory = (Expression<Inventory>) expressions[0];
        return true;
    }

    @Override
    public ItemContainer[] getValues(@NotNull TriggerContext ctx) {
        Optional<? extends Inventory> single = this.inventory.getSingle(ctx);
        if (single.isEmpty()) return null;

        Inventory inventory = single.get();
        ItemContainer container = switch (this.pattern) {
            case 0 -> inventory.getStorage();
            case 1 -> inventory.getArmor();
            case 2 -> inventory.getHotbar();
            case 3 -> inventory.getUtility();
            case 4 -> inventory.getTools();
            case 5 -> inventory.getBackpack();
            case 6 -> inventory.getCombinedEverything();
            default -> null;
        };
        return new ItemContainer[]{container};
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String type = switch (this.pattern) {
            case 0 -> "storage";
            case 1 -> "armor";
            case 2 -> "hotbar";
            case 3 -> "utility";
            case 4 -> "tools";
            case 5 -> "backpack";
            case 6 -> "combined everything";
            default -> "unknown";
        };
        return type + " item container of " + this.inventory.toString(ctx, debug);
    }

}
