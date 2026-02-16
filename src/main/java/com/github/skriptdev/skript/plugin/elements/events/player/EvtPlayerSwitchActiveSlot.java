package com.github.skriptdev.skript.plugin.elements.events.player;

import com.github.skriptdev.skript.api.skript.event.CancellableContext;
import com.github.skriptdev.skript.api.skript.event.PlayerContext;
import com.github.skriptdev.skript.api.skript.event.SystemEvent;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.SwitchActiveSlotEvent;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.TriggerMap;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.context.ContextValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EvtPlayerSwitchActiveSlot extends SystemEvent<EntityEventSystem<EntityStore, SwitchActiveSlotEvent>> {

    public static void register(SkriptRegistration reg) {
        reg.newEvent(EvtPlayerSwitchActiveSlot.class,
                "player switch active slot",
                "player switch active hot[ ]bar slot",
                "player switch active utility slot")
            .setHandledContexts(SwitchSlotContext.class)
            .name("Player Switch Active Slot")
            .description("Called when a player switches their active slot.")
            .experimental("This event doesn't appear to actually get called by the server for hotbar.")
            .examples("")
            .since("INSERT VERSION")
            .register();

        reg.newSingleContextValue(SwitchSlotContext.class, Number.class,
                "slot-index", SwitchSlotContext::getPreviousSlot)
            .description("The slot number before the change happened.")
            .setState(ContextValue.State.PAST)
            .register();

        reg.newSingleContextValue(SwitchSlotContext.class, Number.class,
                "slot-index", SwitchSlotContext::getNewSlot)
            .description("The slot number after the change happened. Can be set.")
            .setState(ContextValue.State.PRESENT)
            .addSetter(SwitchSlotContext::setNewSlot)
            .register();

        reg.newSingleContextValue(SwitchSlotContext.class, ItemContainer.class,
                "item-container", SwitchSlotContext::getContainer)
            .description("The ItemContainer used in this event.")
            .register();
    }

    private static SwitchSlotSystem SYSTEM;
    private int pattern;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        if (SYSTEM == null) {
            SYSTEM = new SwitchSlotSystem();
            applySystem(SYSTEM);
        }
        this.pattern = matchedPattern;
        return true;
    }

    @Override
    public boolean check(TriggerContext ctx) {
        if (ctx instanceof SwitchSlotContext ssc) {
            if (this.pattern == 0) return true;
            else if (this.pattern == 1) return ssc.isHotbar();
            else if (this.pattern == 2) return ssc.isUtility();
        }
        return false;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "player switch active slot";
    }

    public record SwitchSlotContext(SwitchActiveSlotEvent event, Player player)
        implements PlayerContext, CancellableContext {

        @Override
        public Player getPlayer() {
            return this.player;
        }

        public int getPreviousSlot() {
            return this.event.getPreviousSlot();
        }

        public int getNewSlot() {
            return this.event.getNewSlot();
        }

        public void setNewSlot(Number number) {
            this.event.setNewSlot(number.byteValue());
        }

        public ItemContainer getContainer() {
            return this.player.getInventory().getSectionById(this.event.getInventorySectionId());
        }

        public boolean isHotbar() {
            return this.event.getInventorySectionId() == -1;
        }

        public boolean isUtility() {
            return this.event.getInventorySectionId() == -5;
        }

        @Override
        public boolean isCancelled() {
            return this.event.isCancelled();
        }

        @Override
        public void setCancelled(boolean cancelled) {
            this.event.setCancelled(cancelled);
        }

        @Override
        public String getName() {
            return "switch active slot context";
        }
    }

    public static class SwitchSlotSystem extends EntityEventSystem<EntityStore, SwitchActiveSlotEvent> {

        protected SwitchSlotSystem() {
            super(SwitchActiveSlotEvent.class);
        }

        @Override
        public void handle(int i, @NotNull ArchetypeChunk<EntityStore> archetypeChunk,
                           @NotNull Store<EntityStore> store, @NotNull CommandBuffer<EntityStore> commandBuffer,
                           @NotNull SwitchActiveSlotEvent event) {

            Ref<EntityStore> ref = archetypeChunk.getReferenceTo(i);
            Player player = commandBuffer.getComponent(ref, Player.getComponentType());
            SwitchSlotContext context = new SwitchSlotContext(event, player);
            TriggerMap.callTriggersByContext(context);
        }

        @Override
        public @Nullable Query<EntityStore> getQuery() {
            return Player.getComponentType();
        }
    }

}
