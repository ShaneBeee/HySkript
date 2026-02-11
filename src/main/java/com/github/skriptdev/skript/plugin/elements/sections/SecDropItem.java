package com.github.skriptdev.skript.plugin.elements.sections;

import com.github.skriptdev.skript.api.hytale.utils.EntityUtils;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import io.github.syst3ms.skriptparser.util.Pair;
import io.github.syst3ms.skriptparser.variables.Variables;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class SecDropItem extends CodeSection {

    public static void register(SkriptRegistration reg) {
        reg.newSection(SecDropItem.class, "drop %item/itemstack% at %location%",
                "drop %item/itemstack% at %location% with pickup delay %duration%",
                "drop %item/itemstack% at %location% with velocity %vector3f%",
                "drop %item/itemstack% at %location% with velocity %vector3f% [and] [with] pickup delay %duration%")
            .name("Drop Item")
            .description("Drops the specified items.")
            .examples("drop ingredient_poop at location of player",
                "drop {_itemstack} at location of player with pickup delay 10 seconds",
                "drop {_i} at location of player with velocity vector3f(0,1,0) and with pickup delay 5 seconds")
            .since("1.0.0")
            .register();

        reg.addSingleContextValue(ItemComponentContext.class, ItemComponent.class,
            "item-component", ItemComponentContext::getItemComponent);
        reg.addSingleContextValue(ItemComponentContext.class, Entity.class,
            "item-entity", ItemComponentContext::getEntity);
    }

    private Expression<?> items;
    private Expression<Location> location;
    private Expression<Duration> pickupDelay;
    private Expression<Vector3f> velocity;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.items = expressions[0];
        this.location = (Expression<Location>) expressions[1];
        if (matchedPattern == 1) {
            this.pickupDelay = (Expression<Duration>) expressions[2];
        } else if (matchedPattern == 2) {
            this.velocity = (Expression<Vector3f>) expressions[2];
        } else if (matchedPattern == 3) {
            this.velocity = (Expression<Vector3f>) expressions[2];
            this.pickupDelay = (Expression<Duration>) expressions[3];
        }
        ParserState parserState = parseContext.getParserState();
        List<Class<? extends TriggerContext>> triggerContexts = new ArrayList<>(parserState.getCurrentContexts().stream().toList());
        triggerContexts.add(ItemComponentContext.class);
        parserState.setCurrentContexts(new HashSet<>(triggerContexts));
        return true;
    }

    @Override
    public Optional<? extends Statement> walk(@NotNull TriggerContext ctx) {
        Optional<? extends Statement> nextStatement = getNext();
        Location location = this.location.getSingle(ctx).orElse(null);
        if (location == null) return nextStatement;

        String worldName = location.getWorld();
        World world = Universe.get().getWorld(worldName);
        if (world == null) return nextStatement;

        Store<EntityStore> store = world.getEntityStore().getStore();

        Optional<?> single1 = this.items.getSingle(ctx);
        if (single1.isEmpty()) return nextStatement;
        Object o = single1.get();

        ItemStack itemStack;
        if (o instanceof ItemStack itemStack1) {
            itemStack = itemStack1;
        } else if (o instanceof Item item) {
            itemStack = new ItemStack(item.getId());
        } else {
            return nextStatement;
        }

        Vector3f velocity;
        if (this.velocity != null) {
            Optional<? extends Vector3f> single = this.velocity.getSingle(ctx);
            if (single.isPresent()) {
                velocity = single.get();
            } else {
                velocity = Vector3f.ZERO;
            }
        } else {
            velocity = Vector3f.ZERO;
        }

        float pickupDelay;
        if (this.pickupDelay != null) {
            Optional<? extends Duration> single = this.pickupDelay.getSingle(ctx);
            if (single.isPresent()) {
                Duration duration = single.get();
                pickupDelay = duration.toMillis() / 1000.0f;
            } else {
                pickupDelay = 0;
            }
        } else {
            pickupDelay = 0f;
        }

        Optional<? extends Statement> first = getFirst();
        // If there's nothing in the section, let's bounce
        if (first.isEmpty()) return nextStatement;

        Statement firstStatement = first.get();
        // Prevent the statement after our section from running.
        // This prevents the wrong TriggerContext being used
        setNext(null);

        Runnable dropRunnable = () -> {
            Pair<Entity, ItemComponent> pair = EntityUtils.dropItem(store, itemStack, location, velocity, pickupDelay);
            ItemComponentContext itemContext = new ItemComponentContext(pair.getFirst(), pair.getSecond());

            // Copy variables from the previous context into our section context
            Variables.copyLocalVariables(ctx, itemContext);
            Statement.runAll(firstStatement, itemContext);

            // After the section runs, copy variables back into the previous context and clear variables from our section
            Variables.copyLocalVariables(itemContext, ctx);
            Variables.clearLocalVariables(itemContext);

            // If the next statement exists, run it
            nextStatement.ifPresent(statement -> Statement.runAll(statement, ctx));
        };

        // Make sure we're running on the correct thread.
        if (world.isInThread()) {
            world.execute(dropRunnable);
        } else {
            dropRunnable.run();
        }
        return Optional.empty();
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String velocity = this.velocity != null ? " with velocity " + this.velocity.toString(ctx, debug) : "";
        String pickupDelay = this.pickupDelay != null ? " with pickup delay " + this.pickupDelay.toString(ctx, debug) : "";
        return "drop " + this.items.toString(ctx, debug) + " at " + this.location.toString(ctx, debug) + velocity + pickupDelay;
    }

    public static class ItemComponentContext implements TriggerContext {

        private final @Nullable Entity entity;
        private final ItemComponent component;

        public ItemComponentContext(@Nullable Entity entity, ItemComponent component) {
            this.entity = entity;
            this.component = component;
        }

        public ItemComponent getItemComponent() {
            return this.component;
        }

        public @Nullable Entity getEntity() {
            return this.entity;
        }

        @Override
        public String getName() {
            return "item component context";
        }
    }

}
