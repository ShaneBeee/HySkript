package com.github.skriptdev.skript.plugin.elements.expressions;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ExprUUIDRandom implements Expression<UUID> {

    public static void register(SkriptRegistration registration) {
        registration.newExpression(ExprUUIDRandom.class, UUID.class, false,
                "random uuid")
            .name("UUID - Random")
            .description("Generates a random UUID.")
            .examples("set {_uuid} to random uuid")
            .since("INSERT VERSION")
            .register();
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        return true;
    }

    @Override
    public UUID[] getValues(@NotNull TriggerContext ctx) {
        return new UUID[]{UUID.randomUUID()};
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "random uuid";
    }

}
