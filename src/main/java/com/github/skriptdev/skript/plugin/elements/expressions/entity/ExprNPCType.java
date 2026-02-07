package com.github.skriptdev.skript.plugin.elements.expressions.entity;

import com.github.skriptdev.skript.api.skript.registration.NPCRegistry;
import com.github.skriptdev.skript.api.skript.registration.NPCRegistry.NPCRole;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.systems.RoleChangeSystem;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ExprNPCType extends PropertyExpression<Entity, NPCRole> {

    public static void register(SkriptRegistration registration) {
        registration.newPropertyExpression(ExprNPCType.class, NPCRole.class,
                "npc (type|role)", "entities")
            .name("NPC Role of Entity")
            .description("Get/set the NPC role of an NPC entity.")
            .examples("set {_type} to npc type of target entity",
                "if npc type of {_entity} = sheep:",
                "set npc role of target entity of player to cow")
            .since("1.0.0")
            .register();
    }

    @Override
    public @Nullable NPCRole getProperty(@NotNull Entity owner) {
        if (owner instanceof NPCEntity npcEntity) {
            return NPCRegistry.getByIndex(npcEntity.getRoleIndex());
        }
        return null;
    }

    @Override
    public Optional<Class<?>[]> acceptsChange(@NotNull ChangeMode mode) {
        if (mode == ChangeMode.SET) return Optional.of(new Class<?>[]{NPCRole.class});
        return Optional.empty();
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public void change(@NotNull TriggerContext ctx, @NotNull ChangeMode changeMode, Object @NotNull [] changeWith) {
        Expression<Entity> owner = getOwner();
        if (owner == null || changeWith == null) return;

        if (changeMode == ChangeMode.SET) {
            if (changeWith.length == 1 && changeWith[0] instanceof NPCRole role) {
                for (Entity entity : owner.getArray(ctx)) {
                    if (entity instanceof NPCEntity npcEntity) {
                        changeRole(npcEntity, role);
                    }
                }
            }
        }
    }

    private void changeRole(NPCEntity npcEntity, NPCRole npcRole) {
        Ref<EntityStore> reference = npcEntity.getReference();
        if (reference == null) return;

        World world = npcEntity.getWorld();
        if (world == null) return;

        Store<EntityStore> store = world.getEntityStore().getStore();
        Role role = npcEntity.getRole();
        if (role == null || role.isRoleChangeRequested()) return;

        RoleChangeSystem.requestRoleChange(reference, role, npcRole.index(), true, store);
    }

}
