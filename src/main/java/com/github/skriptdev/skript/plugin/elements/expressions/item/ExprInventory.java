package com.github.skriptdev.skript.plugin.elements.expressions.item;

import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ExprInventory implements Expression<Inventory> {

    public static void register(SkriptRegistration registration) {
        registration.newExpression(ExprInventory.class, Inventory.class, false,
                "inventory of %livingentity%")
            .name("Inventory")
            .description("Returns the inventory of a living entity.",
                "An inventory can also be cleared.")
            .examples("set {_inv} to inventory of player",
                "clear inventory of player")
            .since("1.0.0")
            .register();
    }

    private Expression<LivingEntity> entity;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.entity = (Expression<LivingEntity>) expressions[0];
        return true;
    }

    @Override
    public Inventory[] getValues(@NotNull TriggerContext ctx) {
        Optional<? extends LivingEntity> single = this.entity.getSingle(ctx);
        if (single.isEmpty()) return null;
        LivingEntity livingEntity = single.get();
        return new Inventory[]{livingEntity.getInventory()};
    }

    @Override
    public Optional<Class<?>[]> acceptsChange(@NotNull ChangeMode mode) {
        if (mode == ChangeMode.ADD || mode == ChangeMode.DELETE) {
            return Optional.of(new Class<?>[]{ItemStack[].class});
        }
        return Optional.empty();
    }

    @Override
    public void change(@NotNull TriggerContext ctx, @NotNull ChangeMode changeMode, Object @NotNull [] changeWith) {
        Inventory[] toChange = getValues(ctx);
        if (changeMode == ChangeMode.ADD) {
            for (Inventory inventory : toChange) {
                for (Object o : changeWith) {
                    if (o instanceof ItemStack itemStack) {
                        inventory.getCombinedEverything().addItemStack(itemStack);
                    }
                }
            }
        } else if (changeMode == ChangeMode.DELETE) {
            for (Inventory inventory : toChange) {
                inventory.clear();
            }
        }
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "inventory of " + this.entity.toString(ctx, debug);
    }

}
