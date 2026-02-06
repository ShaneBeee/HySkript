package com.github.skriptdev.skript.plugin.elements.functions;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.structures.functions.FunctionParameter;
import io.github.syst3ms.skriptparser.structures.functions.Functions;
import io.github.syst3ms.skriptparser.structures.functions.JavaFunction;
import org.bson.BsonDocument;

public class DefaultFunctions {

    public static void register(SkriptRegistration reg) {
        itemFunctions(reg);
        positionFunctions(reg);
    }

    private static void itemFunctions(SkriptRegistration reg) {
        Functions.newJavaFunction(reg, new JavaFunction<>(
                "itemstack",
                new FunctionParameter[]{
                    new FunctionParameter<>("type", Item.class, true),
                    new FunctionParameter<>("quantity", Number.class, true),
                    new FunctionParameter<>("durability", Number.class, true),
                    new FunctionParameter<>("maxDurability", Number.class, true)
                },
                ItemStack.class,
                true) {
                @Override
                public ItemStack[] executeSimple(Object[][] params) {
                    Item type = (Item) params[0][0];
                    Number quantity = (Number) params[1][0];
                    Number durability = (Number) params[2][0];
                    Number maxDurability = (Number) params[3][0];
                    int max = maxDurability.intValue();
                    ItemStack itemStack = new ItemStack(type.getId(), quantity.intValue(),
                        Math.clamp(durability.intValue(), 0, max), max, new BsonDocument());
                    return new ItemStack[]{itemStack};
                }
            })
            .name("ItemStack")
            .description("Creates a new ItemStack with the given parameters.")
            .examples("set {_stack} to itemstack(Food_Fish_Grilled, 1, 50, 100)")
            .since("1.0.0")
            .register();
    }

    private static void positionFunctions(SkriptRegistration reg) {
        Functions.newJavaFunction(reg, new JavaFunction<>(
                "location",
                new FunctionParameter[]{
                    new FunctionParameter<>("x", Number.class, true),
                    new FunctionParameter<>("y", Number.class, true),
                    new FunctionParameter<>("z", Number.class, true),
                    new FunctionParameter<>("world", World.class, true)
                },
                Location.class,
                true) {
                @Override
                public Location[] executeSimple(Object[][] params) {
                    Number x = (Number) params[0][0];
                    Number y = (Number) params[1][0];
                    Number z = (Number) params[2][0];
                    World world = (World) params[3][0];
                    return new Location[]{new Location(world.getName(), x.doubleValue(), y.doubleValue(), z.doubleValue())};
                }
            })
            .name("Location")
            .description("Creates a location in a world.")
            .examples("set {_loc} to location(1, 100, 1, world of player)")
            .since("1.0.0")
            .register();

        Functions.newJavaFunction(reg,new JavaFunction<>(
                "vector3i",
                new FunctionParameter[]{
                    new FunctionParameter<>("x", Number.class, true),
                    new FunctionParameter<>("y", Number.class, true),
                    new FunctionParameter<>("z", Number.class, true)
                },
                Vector3i.class,
                true) {
                @Override
                public Vector3i[] executeSimple(Object[][] params) {
                    Number x = (Number) params[0][0];
                    Number y = (Number) params[1][0];
                    Number z = (Number) params[2][0];
                    return new Vector3i[]{new Vector3i(x.intValue(), y.intValue(), z.intValue())};
                }
            })
            .name("Vector3i")
            .description("Creates a vector3i with integer coordinates.")
            .examples("set {_v} to vector3i(1, 100, 1)")
            .since("1.0.0")
            .register();

        Functions.newJavaFunction(reg, new JavaFunction<>(
                "vector3f",
                new FunctionParameter[]{
                    new FunctionParameter<>("x", Number.class, true),
                    new FunctionParameter<>("y", Number.class, true),
                    new FunctionParameter<>("z", Number.class, true)
                },
                Vector3f.class,
                true) {
                @Override
                public Vector3f[] executeSimple(Object[][] params) {
                    Number x = (Number) params[0][0];
                    Number y = (Number) params[1][0];
                    Number z = (Number) params[2][0];
                    return new Vector3f[]{new Vector3f(x.floatValue(), y.floatValue(), z.floatValue())};
                }
            })
            .name("Vector3f")
            .description("Creates a vector3f with float coordinates.")
            .examples("set {_v} to vector3f(1.234, 5.3, 1.999)")
            .since("1.0.0")
            .register();

        Functions.newJavaFunction(reg, new JavaFunction<>(
                "vector3d",
                new FunctionParameter[]{
                    new FunctionParameter<>("x", Number.class, true),
                    new FunctionParameter<>("y", Number.class, true),
                    new FunctionParameter<>("z", Number.class, true)
                },
                Vector3d.class,
                true) {
                @Override
                public Vector3d[] executeSimple(Object[][] params) {
                    Number x = (Number) params[0][0];
                    Number y = (Number) params[1][0];
                    Number z = (Number) params[2][0];
                    return new Vector3d[]{new Vector3d(x.doubleValue(), y.doubleValue(), z.doubleValue())};
                }
            })
            .name("Vector3d")
            .description("Creates a vector3d with double coordinates.")
            .examples("set {_v} to vector3d(1.234, 5.3, 1.999)")
            .since("1.0.0")
            .register();

        Functions.newJavaFunction(reg, new JavaFunction<>(
                "world",
                new FunctionParameter[]{
                    new FunctionParameter<>("name", String.class, true)
                },
                World.class,
                true) {
                @Override
                public World[] executeSimple(Object[][] params) {
                    String name = (String) params[0][0];
                    return new World[]{Universe.get().getWorld(name)};
                }
            })
            .name("World")
            .description("Returns the world with the given name.")
            .examples("set {_world} to world(\"default\")")
            .since("1.0.0")
            .register();
    }

}
