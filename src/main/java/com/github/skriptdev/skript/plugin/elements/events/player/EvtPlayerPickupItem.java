package com.github.skriptdev.skript.plugin.elements.events.player;

import com.github.skriptdev.skript.api.skript.event.PlayerContext;
import com.github.skriptdev.skript.api.skript.event.SystemEvent;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.entity.item.PickupItemComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.TriggerMap;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EvtPlayerPickupItem extends SystemEvent<EvtPlayerPickupItem.PlayerPickupItemSystem> {

    public static void register(SkriptRegistration reg) {
        reg.newEvent(EvtPlayerPickupItem.class,
                "player pickup item", "player picked up item")
            .setHandledContexts(PlayerPickupItemContext.class)
            .name("Player Pickup Item")
            .description("Called when a player picks up an item.",
                "Sadly this cannot be cancelled... maybe in the future.")
            .examples("on player pickup item:",
                "\tsend \"You picked up %event-item%!\" to event-player\"")
            .since("INSERT VERSION")
            .register();

        reg.newSingleContextValue(PlayerPickupItemContext.class, ItemStack.class,
            "itemstack", PlayerPickupItemContext::getItemStack)
            .register();
        reg.newSingleContextValue(PlayerPickupItemContext.class, Item.class,
            "item", PlayerPickupItemContext::getItem)
            .register();
    }

    private static PlayerPickupItemSystem SYSTEM;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        if (SYSTEM == null) {
            SYSTEM = new PlayerPickupItemSystem();
            applySystem(SYSTEM);
        }
        return true;
    }

    @Override
    public boolean check(TriggerContext ctx) {
        return ctx instanceof PlayerPickupItemContext;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "player pickup item";
    }

    private record PlayerPickupItemContext(ItemComponent itemComponent, Player player) implements PlayerContext {

        @Override
        public Player getPlayer() {
            return this.player;
        }

        public ItemStack getItemStack() {
            return this.itemComponent.getItemStack();
        }

        public Item getItem() {
            return getItemStack().getItem();
        }

        @Override
        public String getName() {
            return "player pickup item context";
        }
    }

    public static class PlayerPickupItemSystem extends HolderSystem<EntityStore> {

        @Override
        public void onEntityAdd(@NotNull Holder<EntityStore> holder, @NotNull AddReason addReason, @NotNull Store<EntityStore> store) {
        }

        @Override
        public void onEntityRemoved(@NotNull Holder<EntityStore> holder, @NotNull RemoveReason removeReason, @NotNull Store<EntityStore> store) {
            ItemComponent component = holder.getComponent(ItemComponent.getComponentType());
            if (component == null || !component.isRemovedByPlayerPickup()) return;

            PickupItemComponent pickupComp = holder.getComponent(PickupItemComponent.getComponentType());
            if (pickupComp == null) return;

            Ref<EntityStore> targetRef = pickupComp.getTargetRef();
            if (targetRef == null) return;

            Player player = store.getComponent(targetRef, Player.getComponentType());
            if (player == null) return;

            TriggerMap.callTriggersByContext(new PlayerPickupItemContext(component, player));
        }

        @Override
        public @Nullable Query<EntityStore> getQuery() {
            return ItemComponent.getComponentType();
        }
    }

}
