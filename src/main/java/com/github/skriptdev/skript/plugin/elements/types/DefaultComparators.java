package com.github.skriptdev.skript.plugin.elements.types;

import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import io.github.syst3ms.skriptparser.types.comparisons.Comparator;
import io.github.syst3ms.skriptparser.types.comparisons.Comparators;
import io.github.syst3ms.skriptparser.types.comparisons.Relation;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

public class DefaultComparators {

    public static void register() {
        inventory();
    }

    private static void inventory() {
        // Inventory contains ItemStack
        Comparators.registerComparator(Inventory.class, ItemStack.class, new Comparator<>(false) {
            @Override
            public Relation apply(@NotNull Inventory inventory, @NotNull ItemStack itemStack) {
                AtomicReference<Relation> relation = new AtomicReference<>(Relation.NOT_EQUAL);
                inventory.getCombinedEverything().forEach((s, is) -> {
                    if (is.isEquivalentType(itemStack)) {
                        relation.set(Relation.EQUAL);
                    }
                });
                return relation.get();
            }
        });
        // Inventory contains Item
        Comparators.registerComparator(Inventory.class, Item.class, new Comparator<>(false) {
            @Override
            public Relation apply(@NotNull Inventory inventory, @NotNull Item item) {
                AtomicReference<Relation> relation = new AtomicReference<>(Relation.NOT_EQUAL);
                inventory.getCombinedEverything().forEach((s, is) -> {
                    if (is.getItem().equals(item)) {
                        relation.set(Relation.EQUAL);
                    }
                });
                return relation.get();
            }
        });

        // ItemContainer contains ItemStack
        Comparators.registerComparator(ItemContainer.class, ItemStack.class, new Comparator<>(false) {
            @Override
            public Relation apply(@NotNull ItemContainer container, @NotNull ItemStack itemStack) {
                AtomicReference<Relation> relation = new AtomicReference<>(Relation.NOT_EQUAL);
                container.forEach((s, is) -> {
                    if (is.isEquivalentType(itemStack)) {
                        relation.set(Relation.EQUAL);
                    }
                });
                return relation.get();
            }
        });
        // ItemContainer contains Item
        Comparators.registerComparator(ItemContainer.class, Item.class, new Comparator<>(false) {
            @Override
            public Relation apply(@NotNull ItemContainer container, @NotNull Item item) {
                AtomicReference<Relation> relation = new AtomicReference<>(Relation.NOT_EQUAL);
                container.forEach((s, is) -> {
                    if (is.getItem().equals(item)) {
                        relation.set(Relation.EQUAL);
                    }
                });
                return relation.get();
            }
        });
    }

}
