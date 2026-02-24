package com.github.skriptdev.skript.plugin.elements.events.player;

import com.github.skriptdev.skript.api.hytale.Block;
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
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.TriggerMap;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public class EvtPlayerUseBlock extends SystemEvent<EntityEventSystem<EntityStore, ? extends UseBlockEvent>> {

    public static void register(SkriptRegistration reg) {
        reg.newEvent(EvtPlayerUseBlock.class,
                "player use block",
                "pre player use block",
                "before player uses block",
                "post player use block",
                "after player uses block")
            .setHandledContexts(PreUseBlockContext.class, PostUseBlockContext.class)
            .name("Player Use Block")
            .description("Called when a player uses a block.",
                "Pre is cancellable, post is not.")
            .examples("on pre player use block:",
                "\tif \"%event-blocktype%\" contains \"Bed\":",
                "\t\tcancel event",
                "\t\tsend \"No sleepy time for you!\" to player")
            .since("1.0.0")
            .register();

        reg.addSingleContextValue(UseBlockContext.class, BlockType.class, "blocktype", UseBlockContext::getBlockType);
        reg.addSingleContextValue(UseBlockContext.class, Block.class, "block", UseBlockContext::getBlock);
        reg.addSingleContextValue(UseBlockContext.class, InteractionType.class, "interaction-type", UseBlockContext::getInteractionType);
    }

    private static PreUseBlockSystem PRE_SYSTEM;
    private static PostUseBlockSystem POST_SYSTEM;
    private boolean pre;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        if (PRE_SYSTEM == null) {
            PRE_SYSTEM = new PreUseBlockSystem();
            applySystem(PRE_SYSTEM);
        }
        if (POST_SYSTEM == null) {
            POST_SYSTEM = new PostUseBlockSystem();
            applySystem(POST_SYSTEM);
        }
        this.pre = matchedPattern < 3;
        parseContext.getParserState().setCurrentContexts(this.pre ? Collections.singleton(PreUseBlockContext.class) :
            Collections.singleton(PostUseBlockContext.class));
        return true;
    }

    @Override
    public boolean check(TriggerContext ctx) {
        if (this.pre) return ctx instanceof PreUseBlockContext;
        return ctx instanceof PostUseBlockContext;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "player use block";
    }

    public static abstract class UseBlockContext<T extends UseBlockEvent> implements PlayerContext, WorldContext {

        final T event;
        final Player player;

        public UseBlockContext(T event, Player player) {
            this.event = event;
            this.player = player;
        }

        @Override
        public Player getPlayer() {
            return this.player;
        }

        @Override
        public World getWorld() {
            return this.player.getWorld();
        }

        public BlockType getBlockType() {
            return this.event.getBlockType();
        }

        public InteractionType getInteractionType() {
            return this.event.getInteractionType();
        }

        public Block getBlock() {
            World world = this.player.getWorld();
            if (world == null) return null;
            return new Block(world, this.event.getTargetBlock());
        }
    }

    public static class PreUseBlockContext extends UseBlockContext<UseBlockEvent.Pre> implements CancellableContext {

        public PreUseBlockContext(UseBlockEvent.Pre event, Player player) {
            super(event, player);
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
            return "pre use block context";
        }
    }

    public static class PostUseBlockContext extends UseBlockContext<UseBlockEvent.Post> {

        public PostUseBlockContext(UseBlockEvent.Post event, Player player) {
            super(event, player);
        }

        @Override
        public String getName() {
            return "post use block context";
        }
    }

    public static class PreUseBlockSystem extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {

        protected PreUseBlockSystem() {
            super(UseBlockEvent.Pre.class);
        }

        @Override
        public @Nullable Query<EntityStore> getQuery() {
            return Player.getComponentType();
        }

        @Override
        public void handle(int i, @NotNull ArchetypeChunk<EntityStore> archetypeChunk, @NotNull Store<EntityStore> store,
                           @NotNull CommandBuffer<EntityStore> commandBuffer, @NotNull UseBlockEvent.Pre pre) {

            Ref<EntityStore> ref = archetypeChunk.getReferenceTo(i);
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) return;

            TriggerMap.callTriggersByContext(new PreUseBlockContext(pre, player));
        }
    }

    public static class PostUseBlockSystem extends EntityEventSystem<EntityStore, UseBlockEvent.Post> {

        protected PostUseBlockSystem() {
            super(UseBlockEvent.Post.class);
        }

        @Override
        public @Nullable Query<EntityStore> getQuery() {
            return Player.getComponentType();
        }

        @Override
        public void handle(int i, @NotNull ArchetypeChunk<EntityStore> archetypeChunk, @NotNull Store<EntityStore> store,
                           @NotNull CommandBuffer<EntityStore> commandBuffer, @NotNull UseBlockEvent.Post post) {

            Ref<EntityStore> ref = archetypeChunk.getReferenceTo(i);
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) return;

            TriggerMap.callTriggersByContext(new PostUseBlockContext(post, player));
        }
    }

}
