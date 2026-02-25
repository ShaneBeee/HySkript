package com.github.skriptdev.skript.plugin.elements.expressions.entity;

import com.github.skriptdev.skript.api.hytale.utils.EntityUtils;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import io.github.syst3ms.skriptparser.util.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ExprEntityModel extends PropertyExpression<Entity, ModelAsset> {

    public static void register(SkriptRegistration reg) {
        reg.newPropertyExpression(ExprEntityModel.class, ModelAsset.class,
                "model [component]", "entities")
            .name("Entity Model")
            .description("Get/resete the model of an entity.")
            .examples("set {_model} to model component of player",
                "reset model component of player")
            .since("INSERT VERSION")
            .register();
    }

    @Override
    public @Nullable ModelAsset getProperty(@NotNull Entity entity) {
        ModelComponent component = EntityUtils.getComponent(entity, ModelComponent.getComponentType());
        if (component == null) return null;

        String model = component.getModel().getModel();
        return ModelAsset.getAssetMap().getAsset(model);
    }

    @Override
    public Optional<Class<?>[]> acceptsChange(@NotNull ChangeMode mode) {
        if (mode == ChangeMode.RESET || mode == ChangeMode.DELETE) {
            return CollectionUtils.optionalArrayOf(ModelAsset.class);
        }
        return Optional.empty();
    }

    @Override
    public void change(@NotNull TriggerContext ctx, @NotNull ChangeMode changeMode, Object @NotNull [] changeWith) {
        for (Entity entity : getOwner().getArray(ctx)) {
            if (entity instanceof Player player) {
                resetPlayerModel(player);
            } else {
                resetEntityModel(entity);
            }
        }
    }

    private void resetPlayerModel(Player player) {
        PlayerSkinComponent playerSkinComponent = EntityUtils.getComponent(player, PlayerSkinComponent.getComponentType());
        assert playerSkinComponent != null;

        Model newModel = CosmeticsModule.get().createModel(playerSkinComponent.getPlayerSkin());
        ModelComponent component = new ModelComponent(newModel);
        EntityUtils.putComponent(player, ModelComponent.getComponentType(), component);
        playerSkinComponent.setNetworkOutdated();
    }

    private void resetEntityModel(Entity entity) {
        EntityUtils.tryRemoveComponent(entity, ModelComponent.getComponentType());
        if (!(entity instanceof NPCEntity npcEntity)) return;

        Role role = npcEntity.getRole();
        assert role != null;

        Ref<EntityStore> reference = npcEntity.getReference();
        assert reference != null;
        Store<EntityStore> store = reference.getStore();
        role.updateMotionControllers(reference, null, null, store);
    }

}
