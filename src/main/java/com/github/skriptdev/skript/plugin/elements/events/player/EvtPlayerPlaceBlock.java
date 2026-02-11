package com.github.skriptdev.skript.plugin.elements.events.player;

import com.github.skriptdev.skript.api.hytale.utils.AssetStoreUtils;
import com.github.skriptdev.skript.api.hytale.Block;
import com.github.skriptdev.skript.api.skript.event.BlockContext;
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
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.TriggerMap;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.context.ContextValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EvtPlayerPlaceBlock extends SystemEvent<EntityEventSystem<EntityStore, PlaceBlockEvent>> {

    public static void register(SkriptRegistration reg) {
        reg.newEvent(EvtPlayerPlaceBlock.class,
                "player place block", "player places block", "player placed block")
            .name("Player Place Block")
            .description("Called when a player places a block.")
            .examples("on player place block:",
                "\tset {_past} to past context-blocktype",
                "\tset {_future} to context-blocktype",
                "\tbroadcast \"%context-player% placed %{_future}% over %{_past}%\"")
            .since("1.0.0")
            .setHandledContexts(PlaceBlockContext.class)
            .register();

        reg.newSingleContextValue(PlaceBlockContext.class, Item.class,
                "item-in-hand", PlaceBlockContext::getItemInHand)
            .description("The Item type of the ItemStack in the player's hand when they placed the block.")
            .register();
        reg.newSingleContextValue(PlaceBlockContext.class, ItemStack.class,
                "itemstack-in-hand", PlaceBlockContext::getItemStackInHand)
            .description("The ItemStack in the player's hand when they placed the block.")
            .register();
        reg.newSingleContextValue(PlaceBlockContext.class, BlockType.class,
                "blocktype", PlaceBlockContext::getPlacedBlockType)
            .description("The BlockType of the block that was placed by the player.")
            .register();
        reg.newSingleContextValue(PlaceBlockContext.class, BlockType.class,
                "blocktype", PlaceBlockContext::getPreviousBlockType)
            .setState(ContextValue.State.PAST)
            .description("The BlockType of the block before the player placed the block.")
            .register();
    }

    private static PlaceBlockSystem SYSTEM;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        if (SYSTEM == null) {
            SYSTEM = new PlaceBlockSystem();
            applySystem(SYSTEM);
        }
        return true;
    }

    @Override
    public boolean check(TriggerContext ctx) {
        return ctx instanceof PlaceBlockContext;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "player place block";
    }

    public record PlaceBlockContext(PlaceBlockEvent event, Player player)
        implements PlayerContext, BlockContext, WorldContext, CancellableContext {

        public Player getPlayer() {
            return this.player;
        }

        @Override
        public World getWorld() {
            return this.player.getWorld();
        }

        public Item getItemInHand() {
            ItemStack itemInHand = this.event.getItemInHand();
            if (itemInHand == null) return null;
            return itemInHand.getItem();
        }

        public ItemStack getItemStackInHand() {
            return this.event.getItemInHand();
        }

        public BlockType getPreviousBlockType() {
            Block block = getBlock();
            if (block == null) return null;
            return block.getType();
        }

        public BlockType getPlacedBlockType() {
            ItemStack itemInHand = this.event.getItemInHand();
            if (itemInHand == null) return null;
            return AssetStoreUtils.getBlockType(itemInHand.getItem());
        }

        @Override
        public Block getBlock() {
            Vector3i targetBlock = this.event.getTargetBlock();
            World world = this.player.getWorld();
            if (world == null) return null;
            return new Block(world, targetBlock);
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
            return "place block context";
        }
    }

    public static class PlaceBlockSystem extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
        protected PlaceBlockSystem() {
            super(PlaceBlockEvent.class);
        }

        @Override
        public void handle(int i, @NotNull ArchetypeChunk<EntityStore> archetypeChunk, @NotNull Store<EntityStore> store,
                           @NotNull CommandBuffer<EntityStore> commandBuffer, @NotNull PlaceBlockEvent event) {

            Ref<EntityStore> ref = archetypeChunk.getReferenceTo(i);
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) return;

            PlaceBlockContext context = new PlaceBlockContext(event, player);
            TriggerMap.callTriggersByContext(context);
        }

        @Override
        public @Nullable Query<EntityStore> getQuery() {
            return Player.getComponentType();
        }
    }

}
