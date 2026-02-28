package com.github.skriptdev.skript.plugin.elements.expressions.ref;

import com.github.skriptdev.skript.api.hytale.utils.EntityReferenceUtils;
import com.github.skriptdev.skript.api.hytale.utils.EntityReferenceUtils.ReferenceType;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ExprRefComponent implements Expression<Object> {

    private static final List<String> TO_STRINGS = new ArrayList<>();

    public static void register(SkriptRegistration reg) {
        List<ReferenceType<?>> types = EntityReferenceUtils.getTypes();

        String[] patterns = new String[types.size()];
        for (int i = 0; i < types.size(); i++) {
            ReferenceType<?> referenceType = types.get(i);
            patterns[i] = referenceType.getName() + " component of %ref%";
            TO_STRINGS.add(referenceType.getName() + " component of ");
        }
        reg.newExpression(ExprRefComponent.class, Object.class, true, patterns)
            .name("Reference Component")
            .description("Get the specified component of a reference.")
            .examples("loop refs in radius 10 around player:",
                "\tset {_item} to item component of loop-ref")
            .since("1.2.0")
            .register();
    }

    private int pattern;
    private Expression<Ref<EntityStore>> refs;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.pattern = matchedPattern;
        this.refs = (Expression<Ref<EntityStore>>) expressions[0];
        return true;
    }

    @Override
    public Object[] getValues(@NotNull TriggerContext ctx) {
        Ref<EntityStore> ref = this.refs.getSingle(ctx).orElse(null);
        if (ref == null) return null;

        ReferenceType<?> referenceType = EntityReferenceUtils.getTypes().get(this.pattern);
        Component<EntityStore> component = referenceType.getComponent(ref);
        return new Object[]{component};
    }

    @Override
    public Class<?> getReturnType() {
        return EntityReferenceUtils.getTypes().get(this.pattern).getComponentClass();
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return TO_STRINGS.get(this.pattern) + this.refs.toString(ctx, debug);
    }

}
