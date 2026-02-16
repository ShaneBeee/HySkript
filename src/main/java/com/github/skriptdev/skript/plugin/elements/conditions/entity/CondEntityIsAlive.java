package com.github.skriptdev.skript.plugin.elements.conditions.entity;

import com.github.skriptdev.skript.api.hytale.utils.EntityUtils;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.properties.ConditionalType;
import io.github.syst3ms.skriptparser.lang.properties.PropertyConditional;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

public class CondEntityIsAlive extends PropertyConditional<LivingEntity> {

    public static void register(SkriptRegistration reg) {
        reg.newPropertyConditional(CondEntityIsAlive.class,
                "livingentities",
                ConditionalType.BE,
                "(alive|:dead)")
            .name("Entity is Alive/Dead")
            .description("Check if a LivingEntity is alive/dead.")
            .examples("if player is alive:",
                "\tkill player")
            .since("INSERT VERSION")
            .register();
    }

    private boolean isDead;

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, ParseContext parseContext) {
        this.isDead = parseContext.hasMark("dead");
        return super.init(expressions, matchedPattern, parseContext);
    }

    @Override
    public boolean check(@NotNull TriggerContext ctx) {
        return getPerformer().check(ctx, entity -> {
            DeathComponent component = EntityUtils.getComponent(entity, DeathComponent.getComponentType());
            if (this.isDead) {
                return component != null;
            } else {
                return component == null;
            }
        });
    }

}
