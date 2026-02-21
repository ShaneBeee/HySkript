package com.github.skriptdev.skript.plugin.elements.effects.entity;

import com.github.skriptdev.skript.api.hytale.utils.EntityUtils;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

public class EffRemoveStatModifier extends Effect {

    public static void register(SkriptRegistration reg) {
        reg.newEffect(EffRemoveStatModifier.class,
            "remove %entitystattype% entity stat modifier with key %string% from %entities%")
            .name("Entity Stat Modifier - Remove")
            .description("Removes an entity stat modifier from the specified entities.",
                "The key will be the key used to apply the modifier.")
            .examples("remove health entity stat modifier with key \"MyKey\" from all players")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<EntityStatType> entityStatType;
    private Expression<String> key;
    private Expression<Entity> entities;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.entityStatType = (Expression<EntityStatType>) expressions[0];
        this.key = (Expression<String>) expressions[1];
        this.entities = (Expression<Entity>) expressions[2];
        return true;
    }

    @Override
    protected void execute(@NotNull TriggerContext ctx) {
        EntityStatType statType = this.entityStatType.getSingle(ctx).orElse(null);
        String key = this.key.getSingle(ctx).orElse(null);
        if (statType == null || key == null) return;

        int index = EntityStatType.getAssetMap().getIndex(statType.getId());
        for (Entity entity : this.entities.getArray(ctx)) {
            EntityStatMap component = EntityUtils.getComponent(entity, EntityStatMap.getComponentType());
            if (component == null) continue;

            EntityStatValue entityStatValue = component.get(index);
            if (entityStatValue == null) continue;

            component.removeModifier(index, key);
        }
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "remove " + this.entityStatType.toString(ctx, debug) + " entity stat modifier with key " +
            this.key.toString(ctx, debug) + " from " + this.entities.toString(ctx, debug);
    }

}
