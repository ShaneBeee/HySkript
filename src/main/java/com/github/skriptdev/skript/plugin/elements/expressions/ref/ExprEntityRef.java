package com.github.skriptdev.skript.plugin.elements.expressions.ref;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.Entity;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@SuppressWarnings("rawtypes")
public class ExprEntityRef implements Expression<Ref> {

    public static void register(SkriptRegistration reg) {
        reg.newExpression(ExprEntityRef.class, Ref.class, true,
                "(ref|reference) (of|from) %entity%")
            .name("Entity Reference")
            .description("Get the reference of an entity.")
            .examples("set {_ref} to reference of player",
                "set {_ref} to ref of event-entity")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<Entity> entity;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.entity = (Expression<Entity>) expressions[0];
        return true;
    }

    @Override
    public Ref[] getValues(@NotNull TriggerContext ctx) {
        Optional<? extends Entity> single = this.entity.getSingle(ctx);
        return single.map(value -> new Ref[]{value.getReference()}).orElse(null);
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "reference of " + this.entity.toString(ctx, debug);
    }

}
