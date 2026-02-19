package com.github.skriptdev.skript.plugin.elements.events.entity;

import com.github.skriptdev.skript.api.skript.event.CancellableContext;
import com.github.skriptdev.skript.api.skript.event.PlayerContext;
import com.github.skriptdev.skript.api.skript.event.SystemEvent;
import com.github.skriptdev.skript.api.skript.event.WorldContext;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.InteractivelyPickupItemEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.TriggerMap;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EvtEntityPickupItem extends SystemEvent<EntityEventSystem<EntityStore, InteractivelyPickupItemEvent>> {

    public static void register(SkriptRegistration reg) {
        reg.newEvent(EvtEntityPickupItem.class,
                "entity interactively pickup item", "entity interactively picked up item",
                "player interactively pickup item", "player interactively picked up item")
            .name("Entity Interactively Pickup Item")
            .description("Called when picking up an item after interacting, such as clicking `F` on a block.")
            .since("1.0.0")
            .setHandledContexts(PickupItemContext.class)
            .register();

        reg.newSingleContextValue(PickupItemContext.class, Entity.class,
                "entity", PickupItemContext::getEntity)
            .description("The entity that picked up the item.")
            .register();
        reg.newSingleContextValue(PickupItemContext.class, ItemStack.class,
                "item-stack", PickupItemContext::getItemStack)
            .description("The ItemStack picked up by the entity.")
            .addSetter(PickupItemContext::setItemStack)
            .register();
    }

    private static PickupItemSystem SYSTEM;
    private int pattern;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        this.pattern = matchedPattern > 1 ? 1 : 0;
        if (SYSTEM == null) {
            SYSTEM = new PickupItemSystem();
            applySystem(SYSTEM);
        }
        return true;
    }

    @Override
    public boolean check(TriggerContext ctx) {
        if (ctx instanceof PickupItemContext pickupItemContext) {
            return this.pattern == pickupItemContext.pattern;
        }
        return false;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "entity pickup item";
    }

    public record PickupItemContext(InteractivelyPickupItemEvent event, Entity entity, int pattern)
        implements PlayerContext, CancellableContext, WorldContext {

        public Entity getEntity() {
            return this.entity;
        }

        public ItemStack getItemStack() {
            return this.event.getItemStack();
        }

        public void setItemStack(ItemStack itemStack) {
            this.event.setItemStack(itemStack);
        }

        public Player getPlayer() {
            if (this.entity instanceof Player p) return p;
            return null;
        }

        @Override
        public World getWorld() {
            return this.entity.getWorld();
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
            return "pickup item context";
        }
    }

    public static class PickupItemSystem extends EntityEventSystem<EntityStore, InteractivelyPickupItemEvent> {
        protected PickupItemSystem() {
            super(InteractivelyPickupItemEvent.class);
        }


        @SuppressWarnings("DataFlowIssue")
        @Override
        public void handle(int i, @NotNull ArchetypeChunk<EntityStore> archetypeChunk, @NotNull Store<EntityStore> store,
                           @NotNull CommandBuffer<EntityStore> commandBuffer, @NotNull InteractivelyPickupItemEvent event) {

            Ref<EntityStore> ref = archetypeChunk.getReferenceTo(i);

            Entity entity = null;
            int pattern = 0;
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player != null) {
                pattern = 1;
                entity = player;
            } else {
                NPCEntity component = store.getComponent(ref, NPCEntity.getComponentType());
                if (component != null) {
                    entity = component;
                }
            }

            PickupItemContext pickupItemContext = new PickupItemContext(event, entity, pattern);
            TriggerMap.callTriggersByContext(pickupItemContext);
        }

        @Override
        public @Nullable Query<EntityStore> getQuery() {
            return Player.getComponentType();
        }
    }

}
