package com.github.skriptdev.skript.plugin.elements.events.entity;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.plugin.HySk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.TriggerMap;
import io.github.syst3ms.skriptparser.lang.event.SkriptEvent;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EvtEntityDeath extends SkriptEvent {

    public static void register(SkriptRegistration registration) {
        registration.newEvent(EvtEntityDeath.class, "death", "death of player", "death of npc")
            .setHandledContexts(EntityDeathContext.class)
            .name("Entity Death")
            .description("Called when an entity dies.",
                "**Patterns**:",
                "- `on death` = Death of all entities (players and NPCs).",
                "- `on death of player` = Death of players only.",
                "- `on death of npc` = Death of NPCs only.")
            .examples("on death of player:",
                "\tbroadcast \"Poor %context-victim%\" has died!",
                "",
                "on death of player:",
                "\tset {lost::%uuid of context-victim%} to context-lost-items")
            .since("INSERT VERSION")
            .register();

        registration.addContextValue(EntityDeathContext.class,
            Entity.class, true, "victim", EntityDeathContext::getVictim);
        registration.addContextValue(EntityDeathContext.class,
            Entity.class, true, "attacker", EntityDeathContext::getAttacker);
        registration.addContextValue(EntityDeathContext.class,
            Damage.Source.class, true, "damage-source", EntityDeathContext::getDamageSource);
        registration.addContextValue(EntityDeathContext.class,
            DamageCause.class, true, "death-cause", EntityDeathContext::getDamageCause);
        registration.addContextValue(EntityDeathContext.class,
            Damage.class, true, "death-info", EntityDeathContext::getDamage);
        registration.addContextValue(EntityDeathContext.class,
            ItemStack.class, false, "lost-items", EntityDeathContext::getItemsLostOnDeath);
    }

    private static EntityDeathListener LISTENER;

    private int pattern;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        if (LISTENER == null) {
            LISTENER = new EntityDeathListener();
            HySk.getInstance().getEntityStoreRegistry().registerSystem(LISTENER);
        }
        this.pattern = matchedPattern;
        return true;
    }

    @Override
    public boolean check(TriggerContext ctx) {
        if (ctx instanceof EntityDeathContext deathContext) {
            if (this.pattern == 0) return true;
            return deathContext.pattern == this.pattern;
        }

        return false;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "entity death";
    }

    private static class EntityDeathListener extends DeathSystems.OnDeathSystem {

        @SuppressWarnings("DataFlowIssue")
        @Override
        public void onComponentAdded(@NotNull Ref<EntityStore> ref, @NotNull DeathComponent deathComponent, @NotNull Store<EntityStore> store, @NotNull CommandBuffer<EntityStore> buffer) {
            NPCEntity npc = buffer.getComponent(ref, NPCEntity.getComponentType());
            Player player = buffer.getComponent(ref, Player.getComponentType());

            int pattern;
            Entity victim;
            if (player != null) {
                pattern = 1;
                victim = player;
            } else if (npc != null) {
                pattern = 2;
                victim = npc;
            } else return;

            TriggerMap.callTriggersByContext(new EntityDeathContext(pattern, victim, deathComponent));
        }

        @Override
        public @Nullable Query<EntityStore> getQuery() {
            return super.componentType();
        }
    }

    @SuppressWarnings("DataFlowIssue")
    private record EntityDeathContext(int pattern, Entity victim, DeathComponent component) implements TriggerContext {

        public Entity[] getVictim() {
            return new Entity[]{this.victim};
        }

        public Entity[] getAttacker() {
            Entity attacker = null;
            if (this.component.getDeathInfo().getSource() instanceof Damage.EntitySource entitySource) {
                Ref<EntityStore> attackerRef = entitySource.getRef();
                Store<EntityStore> store = attackerRef.getStore();
                Player player = store.getComponent(attackerRef, Player.getComponentType());
                if (player != null) {
                    attacker = player;
                } else {
                    NPCEntity npc = store.getComponent(attackerRef, NPCEntity.getComponentType());
                    if (npc != null) attacker = npc;
                }
            }
            return new Entity[]{attacker};
        }

        public Damage.Source[] getDamageSource() {
            return new Damage.Source[]{this.component.getDeathInfo().getSource()};
        }

        public DamageCause[] getDamageCause() {
            return new DamageCause[]{this.component.getDeathCause()};
        }

        public Damage[] getDamage() {
            return new Damage[]{this.component.getDeathInfo()};
        }

        public ItemStack[] getItemsLostOnDeath() {
            return this.component.getItemsLostOnDeath();
        }

        @Override
        public String getName() {
            return "entity-death-context";
        }
    }

}
