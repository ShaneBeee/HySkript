package com.github.skriptdev.skript.plugin.elements.expressions.ref;

import com.github.skriptdev.skript.api.hytale.utils.EntityReferenceUtils;
import com.github.skriptdev.skript.api.hytale.utils.EntityReferenceUtils.ReferenceType;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Literal;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("rawtypes")
public class ExprComponentsInRadius implements Expression<Object> {

    public static void register(SkriptRegistration reg) {
        List<String> typeNames = new ArrayList<>();
        for (ReferenceType<?> type : EntityReferenceUtils.getTypes()) {
            Class<?> componentClass = type.getComponentClass();
            Optional<? extends Type<?>> byClassExact = TypeManager.getByClassExact(componentClass);
            byClassExact.ifPresent(value -> typeNames.add(value.getDocumentation().getName()));
        }
        String supportedTypes = String.join(", ", typeNames);

        reg.newExpression(ExprComponentsInRadius.class, Object.class, true,
                "[all] %*type% components in radius %number% of %location%",
                "[all] %*type% components within %location% and %location%")
            .name("Components in Radius")
            .description("Get all components of a specific type within a radius around a location.",
                "Currently supported types: " + supportedTypes + ".")
            .examples("loop ItemComponent components in radius 10 around player")
            .since("1.2.0")
            .register();
    }

    private ReferenceType refType;
    private Expression<Number> radius;
    private Expression<Location> location;
    private Expression<Location> loc1, loc2;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        Literal<Type<?>> literalType = (Literal<Type<?>>) expressions[0];
        Optional<? extends Type<?>> typeOptional = literalType.getSingle();
        if (typeOptional.isEmpty()) return false;

        Type<?> type = typeOptional.get();
        Class<?> typeClass = type.getTypeClass();
        if (Component.class.isAssignableFrom(typeClass)) {
            ReferenceType<?> type1 = EntityReferenceUtils.getType((Class<? extends Component<?>>) typeClass);
            if (type1 != null) {
                this.refType = type1;
            }
        }

        if (this.refType == null) {
            parseContext.getLogger().error("There is no component type linked to '" + type.getBaseName() + "'", ErrorType.SEMANTIC_ERROR);
            return false;
        }
        if (matchedPattern == 0) {
            this.radius = (Expression<Number>) expressions[0];
            this.location = (Expression<Location>) expressions[1];
        } else if (matchedPattern == 1) {
            this.loc1 = (Expression<Location>) expressions[0];
            this.loc2 = (Expression<Location>) expressions[1];
        } else {
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object[] getValues(@NotNull TriggerContext ctx) {

        List<Object> objects = new ArrayList<>();

        if (this.location != null) {
            Location loc = this.location.getSingle(ctx).orElse(null);
            if (loc == null) return null;

            Number number = this.radius.getSingle(ctx).orElse(null);
            if (number == null) return null;

            double radius = number.doubleValue();

            String worldName = loc.getWorld();
            if (worldName == null) return null;

            World world = Universe.get().getWorld(worldName);
            if (world == null) return null;
            Store<EntityStore> store = world.getEntityStore().getStore();

            List<Ref<EntityStore>> refsInSphere = EntityReferenceUtils.getRefsInSphere(loc.getPosition(), radius, store);
            for (Ref<EntityStore> entityStoreRef : refsInSphere) {
                Component component = this.refType.getComponent(entityStoreRef);
                if (component != null) {
                    objects.add(component);
                }
            }
        } else {
            Location loc1 = this.loc1.getSingle(ctx).orElse(null);
            Location loc2 = this.loc2.getSingle(ctx).orElse(null);
            if (loc1 == null || loc2 == null) return null;

            String worldName = loc1.getWorld();
            if (worldName == null) return null;

            World world = Universe.get().getWorld(worldName);
            if (world == null) return null;
            Store<EntityStore> store = world.getEntityStore().getStore();

            Vector3d min = Vector3d.min(loc1.getPosition(), loc2.getPosition());
            Vector3d max = Vector3d.max(loc1.getPosition(), loc2.getPosition());

            List<Ref<EntityStore>> refsInBox = EntityReferenceUtils.getRefsInBox(min, max, store);
            for (Ref<EntityStore> inBox : refsInBox) {
                Component component = this.refType.getComponent(inBox);
                if (component != null) {
                    objects.add(component);
                }
            }
        }

        return objects.toArray(new Object[0]);
    }

    @Override
    public Class<?> getReturnType() {
        return this.refType.getComponentClass();
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        if (this.location != null) {
            return "all " + this.refType.getName() + " components in radius " + this.radius.toString(ctx, debug) +
                " around " + this.location.toString(ctx, debug);
        } else {
            return "all " + this.refType.getName() + " components within " + this.loc1.toString(ctx, debug) + " and " +
                this.loc2.toString(ctx, debug);
        }
    }

}
