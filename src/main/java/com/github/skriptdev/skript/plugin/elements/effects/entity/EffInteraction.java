package com.github.skriptdev.skript.plugin.elements.effects.entity;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.api.utils.Utils;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class EffInteraction extends Effect {

    public static void register(SkriptRegistration reg) {
        reg.newEffect(EffInteraction.class, "perform [root] interaction %rootinteraction% on %entity%",
                "perform [root] interaction %rootinteraction% on %entity% at %location%",
                "perform %interactiontype% [root] interaction %rootinteraction% on %entity%",
                "perform %interactiontype% [root] interaction %rootinteraction% on %entity% at %location%")
            .name("Perform Interaction")
            .description("Performs a root interaction on an entity.",
                "If an InteractionType is not specified, the `primary` InteractionType will be performed.")
            .examples("perform interaction Shovel_Dig on player")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<InteractionType> interactionType;
    private Expression<RootInteraction> interaction;
    private Expression<Entity> entity;
    private Expression<Location> location;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        if (matchedPattern == 0) {
            this.interaction = (Expression<RootInteraction>) expressions[0];
            this.entity = (Expression<Entity>) expressions[1];
        } else if (matchedPattern == 1) {
            this.interaction = (Expression<RootInteraction>) expressions[0];
            this.entity = (Expression<Entity>) expressions[1];
            this.location = (Expression<Location>) expressions[2];
        } else if (matchedPattern == 2) {
            this.interactionType = (Expression<InteractionType>) expressions[0];
            this.interaction = (Expression<RootInteraction>) expressions[1];
            this.entity = (Expression<Entity>) expressions[2];
        } else {
            this.interactionType = (Expression<InteractionType>) expressions[0];
            this.interaction = (Expression<RootInteraction>) expressions[1];
            this.entity = (Expression<Entity>) expressions[2];
            this.location = (Expression<Location>) expressions[3];
        }
        return true;
    }

    @SuppressWarnings("removal")
    @Override
    protected void execute(@NotNull TriggerContext ctx) {
        RootInteraction interaction = this.interaction.getSingle(ctx).orElse(null);
        if (interaction == null) return;

        InteractionType type;
        if (this.interactionType != null) {
            InteractionType interactionType1 = this.interactionType.getSingle(ctx).orElse(null);
            type = Objects.requireNonNullElse(interactionType1, InteractionType.Primary);
        } else {
            type = InteractionType.Primary;
        }

        Entity entity = this.entity.getSingle(ctx).orElse(null);
        if (entity == null) return;

        World world = entity.getWorld();
        if (world == null) return;

        BlockPosition blockPosition;
        if (this.location != null) {
            Location location = this.location.getSingle(ctx).orElse(null);
            if (location != null) {
                Vector3i position = location.getPosition().toVector3i();
                blockPosition = new BlockPosition(position.getX(), position.getY(), position.getZ());
            } else {
                blockPosition = null;
            }
        } else {
            blockPosition = null;
        }

        Runnable worldRunnable = () -> {
            Ref<EntityStore> ref = entity.getReference();
            if (ref == null) return;

            Store<EntityStore> store = world.getEntityStore().getStore();
            InteractionManager manager = store.getComponent(ref, InteractionModule.get().getInteractionManagerComponent());
            if (manager == null) return;

            InteractionContext context = InteractionContext.forInteraction(manager, ref, type, store);
            InteractionChain chain;
            if (blockPosition == null) {
                Utils.warn("no position specified for interaction");
                chain = manager.initChain(type, context, interaction, true);
            } else {
                Utils.warn("interaction at position " + blockPosition);
                chain = manager.initChain(type, context, interaction, entity.getNetworkId(), blockPosition, true);
            }
            manager.queueExecuteChain(chain);
        };

        // Ensure running on the correct thread
        if (world.isInThread()) {
            worldRunnable.run();
        } else {
            world.execute(worldRunnable);
        }
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String type = this.interactionType != null ? this.interactionType.toString(ctx, debug) + " " : "";
        return "perform  " + type + "interaction " + this.interaction.toString(ctx, debug) +
            " on " + this.entity.toString(ctx, debug);
    }

}
