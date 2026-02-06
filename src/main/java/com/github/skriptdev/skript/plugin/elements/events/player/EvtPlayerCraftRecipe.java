package com.github.skriptdev.skript.plugin.elements.events.player;

import com.github.skriptdev.skript.api.skript.event.CancellableContext;
import com.github.skriptdev.skript.api.skript.event.PlayerContext;
import com.github.skriptdev.skript.api.skript.event.SystemEvent;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.plugin.HySk;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.CraftRecipeEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.TriggerMap;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EvtPlayerCraftRecipe extends SystemEvent<EntityEventSystem<EntityStore, CraftRecipeEvent>> {

    public static void register(SkriptRegistration reg) {
        reg.newEvent(EvtPlayerCraftRecipe.class,
                "player craft recipe",
                "pre player craft recipe",
                "post player craft recipe")
            .setHandledContexts(CraftRecipeContext.class)
            .name("Player Craft Recipe")
            .experimental("These events don't seem to be called right now, server issues?!?!")
            .description("Called when a player crafts a recipe.")
            .examples("on player craft recipe:",
                "\tif context-recipe-id = \"Salvage_Rock_Lime_Cobble\":",
                "\t\tcancel event")
            .since("INSERT VERSION")
            .register();

        reg.addSingleContextValue(CraftRecipeContext.class, Integer.class,
            "quantity", CraftRecipeContext::getQuantity);
        reg.addSingleContextValue(CraftRecipeContext.class, CraftingRecipe.class,
            "recipe", CraftRecipeContext::getRecipe);
        reg.addSingleContextValue(CraftRecipeContext.class, String.class,
            "recipe-id", CraftRecipeContext::getRecipeId);
    }

    static final ComponentRegistryProxy<EntityStore> REGISTRY = HySk.getInstance().getEntityStoreRegistry();
    private static PreCraftRecipeSystem PRE_SYSTEM;
    private static PostCraftRecipeSystem POST_SYSTEM;
    private int pattern;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        this.pattern = matchedPattern;
        if (PRE_SYSTEM == null) {
            PRE_SYSTEM = new PreCraftRecipeSystem();
            REGISTRY.registerSystem(PRE_SYSTEM);
        }
        if (POST_SYSTEM == null) {
            POST_SYSTEM = new PostCraftRecipeSystem();
            REGISTRY.registerSystem(POST_SYSTEM);
        }
        return true;
    }

    @Override
    public boolean check(TriggerContext ctx) {
        if (ctx instanceof CraftRecipeContext craftRecipeContext) {
            if (this.pattern == 0) return true;
            else if (this.pattern == 1) return craftRecipeContext.getPattern() == 0;
            else if (this.pattern == 2) return craftRecipeContext.getPattern() == 1;
        }
        return false;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "player craft recipe";
    }

    public record CraftRecipeContext(CraftRecipeEvent event,
                                     Player player) implements PlayerContext, CancellableContext {
        private int getPattern() {
            if (this.event instanceof CraftRecipeEvent.Post) {
                return 1;
            }
            return 0;
        }

        @Override
        public Player getPlayer() {
            return this.player;
        }

        private int getQuantity() {
            return this.event.getQuantity();
        }

        private CraftingRecipe getRecipe() {
            return this.event.getCraftedRecipe();
        }

        private String getRecipeId() {
            return this.event.getCraftedRecipe().getId();
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
            return "craft-recipe-context";
        }
    }

    public static class PreCraftRecipeSystem extends EntityEventSystem<EntityStore, CraftRecipeEvent.Pre> {

        protected PreCraftRecipeSystem() {
            super(CraftRecipeEvent.Pre.class);
        }

        @Override
        public void handle(int i, @NotNull ArchetypeChunk<EntityStore> archetypeChunk,
                           @NotNull Store<EntityStore> store, @NotNull CommandBuffer<EntityStore> commandBuffer,
                           @NotNull CraftRecipeEvent.Pre event) {

            Ref<EntityStore> ref = archetypeChunk.getReferenceTo(i);
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) return;

            CraftRecipeContext context = new CraftRecipeContext(event, player);
            TriggerMap.callTriggersByContext(context);
        }

        @Override
        public @Nullable Query<EntityStore> getQuery() {
            return Player.getComponentType();
        }
    }

    public static class PostCraftRecipeSystem extends EntityEventSystem<EntityStore, CraftRecipeEvent.Post> {

        protected PostCraftRecipeSystem() {
            super(CraftRecipeEvent.Post.class);
        }

        @Override
        public void handle(int i, @NotNull ArchetypeChunk<EntityStore> archetypeChunk,
                           @NotNull Store<EntityStore> store, @NotNull CommandBuffer<EntityStore> commandBuffer,
                           @NotNull CraftRecipeEvent.Post event) {

            Ref<EntityStore> ref = archetypeChunk.getReferenceTo(i);
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) return;

            CraftRecipeContext context = new CraftRecipeContext(event, player);
            TriggerMap.callTriggersByContext(context);
        }

        @Override
        public @Nullable Query<EntityStore> getQuery() {
            return Player.getComponentType();
        }
    }

}
