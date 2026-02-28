package com.github.skriptdev.skript.plugin.elements.effects.entity;

import com.github.skriptdev.skript.api.hytale.utils.EntityUtils;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

public class EffEntityModel extends Effect {

    public static void register(SkriptRegistration reg) {
        reg.newEffect(EffEntityModel.class, "apply model [component] %modelasset% to %entities%",
                "apply model [component] %modelasset% with scale %number% to %entities%")
            .name("Entity Model")
            .description("Apply the model of an Entity/Player.",
                "Scale is a float value, think of 1 as 100%.",
                "To remove/reset use the entity model expression.")
            .examples("apply model sheep to player",
                "apply model Skeleton_Sand_Archer with scale 2 to player")
            .since("1.2.0")
            .register();
    }

    private Expression<ModelAsset> modelAsset;
    private Expression<Number> scale;
    private Expression<Entity> entities;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        if (matchedPattern == 0) {
            this.modelAsset = (Expression<ModelAsset>) expressions[0];
            this.entities = (Expression<Entity>) expressions[1];
        } else if (matchedPattern == 1) {
            this.modelAsset = (Expression<ModelAsset>) expressions[0];
            this.scale = (Expression<Number>) expressions[1];
            this.entities = (Expression<Entity>) expressions[2];
        }
        return true;
    }

    @Override
    protected void execute(@NotNull TriggerContext ctx) {
        ModelAsset modelAsset = this.modelAsset.getSingle(ctx).orElse(null);
        if (modelAsset == null) return;

        float scale = 1.0f;
        if (this.scale != null) {
            Number number = this.scale.getSingle(ctx).orElse(null);
            if (number != null) {
                // Must be greater than 0.0f
                scale = Math.max(0.000001f, number.floatValue());
            }
        }

        for (Entity entity : this.entities.getArray(ctx)) {
            Model model = Model.createScaledModel(modelAsset, scale);
            ModelComponent component = new ModelComponent(model);
            EntityUtils.putComponent(entity, ModelComponent.getComponentType(), component);
            if (entity instanceof NPCEntity npcEntity) {
                Ref<EntityStore> ref = npcEntity.getReference();
                assert ref != null;
                Store<EntityStore> store = ref.getStore();
                Role role = npcEntity.getRole();
                assert role != null;
                role.updateMotionControllers(ref, model, model.getBoundingBox(), store);
            }
        }
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String entity = this.entities.toString(ctx, debug);
        if (this.scale != null) {
            String scale = this.scale.toString(ctx, debug);
            return "apply model " + this.modelAsset.toString(ctx, debug) + " with scale " + scale + " to " + entity;
        } else {
            return "apply model " + this.modelAsset.toString(ctx, debug) + " to " + entity;
        }
    }

}
