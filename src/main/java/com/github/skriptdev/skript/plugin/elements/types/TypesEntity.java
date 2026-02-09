package com.github.skriptdev.skript.plugin.elements.types;

import com.github.skriptdev.skript.api.hytale.EntityComponentUtils;
import com.github.skriptdev.skript.api.skript.registration.NPCRegistry;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.effect.ActiveEntityEffect;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import io.github.syst3ms.skriptparser.types.changers.Changer;
import org.jetbrains.annotations.NotNull;

public class TypesEntity {

    static void register(SkriptRegistration reg) {
        reg.newType(ActiveEntityEffect.class, "activeentityeffect", "activeEntityEffect@s")
            .name("Active Entity Effect")
            .description("Represents an active EntityEffect applied to an entity.")
            .since("1.0.0")
            .toStringFunction(ActiveEntityEffect::toString)
            .register();
        reg.newType(Entity.class, "entity", "entit@y@ies")
            .toStringFunction(EntityComponentUtils::getName)
            .name("Entity")
            .description("Represents any Entity in the game, including Players and NPCs.")
            .since("1.0.0")
            .defaultChanger(new Changer<>() {
                @Override
                public Class<?>[] acceptsChange(@NotNull ChangeMode mode) {
                    if (mode == ChangeMode.DELETE) return new Class<?>[] {Entity.class};
                    return null;
                }

                @Override
                public void change(Entity @NotNull [] toChange, Object @NotNull [] changeWith, @NotNull ChangeMode mode) {
                    if (mode != ChangeMode.DELETE) return;
                    for (Entity entity : toChange) {
                        if (entity instanceof Player) continue;
                        Ref<EntityStore> reference = entity.getReference();
                        if (reference == null) continue;
                        World world = entity.getWorld();
                        if (world == null) continue;
                        world.getEntityStore().getStore().removeEntity(reference, RemoveReason.REMOVE);
                    }
                }
            })
            .register();
        reg.newType(LivingEntity.class, "livingentity", "livingEntit@y@ies")
            .name("Living Entity")
            .description("Represents any living entity in the game, including players and mobs.")
            .since("1.0.0")
            .toStringFunction(EntityComponentUtils::getName)
            .register();
        reg.newType(NPCEntity.class, "npcentity", "npcEntit@y@ies")
            .name("NPC Entity")
            .description("Represents an NPC entity in the game.")
            .since("1.0.0")
            .toStringFunction(NPCRegistry::stringify)
            .register();
        reg.newType(NPCRegistry.NPCRole.class, "npcrole", "npcrole@s")
            .name("NPC Role")
            .description("Represents the type of NPCs in the game.")
            .examples("coming soon") // TODO
            .usage(NPCRegistry.getTypeUsage())
            .since("1.0.0")
            .toStringFunction(NPCRegistry.NPCRole::name)
            .supplier(NPCRegistry::iterator)
            .literalParser(NPCRegistry::parse)
            .register();
    }

}
