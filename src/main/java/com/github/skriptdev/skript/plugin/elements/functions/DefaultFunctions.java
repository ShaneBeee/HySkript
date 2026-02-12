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
import io.github.syst3ms.skriptparser.util.SkriptDate;
import io.github.syst3ms.skriptparser.util.Time;
import org.bson.BsonDocument;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class DefaultFunctions {

    public static void register(SkriptRegistration reg) {
        dateTimeFunctions(reg);
        itemFunctions(reg);
        positionFunctions(reg);
    }

    private static void dateTimeFunctions(SkriptRegistration reg) {
        Functions.newJavaFunction(reg, new JavaFunction<>(
                "date",
                new FunctionParameter[]{
                    new FunctionParameter<>("year", Number.class, true),
                    new FunctionParameter<>("month", Number.class, true),
                    new FunctionParameter<>("day", Number.class, true)
                },
                SkriptDate.class,
                true
            ) {
                @Override
                public SkriptDate[] executeSimple(Object[][] params) {
                    Number year = (Number) params[0][0];
                    Number month = (Number) params[1][0];
                    Number day = (Number) params[2][0];
                    LocalDateTime localDateTime = LocalDateTime.of(year.intValue(), month.intValue(),
                        day.intValue(), 0, 0);
                    long epochSecond = localDateTime.toEpochSecond(ZoneOffset.systemDefault().getRules().getOffset(localDateTime));
                    return new SkriptDate[]{SkriptDate.of(epochSecond * 1000)};
                }
            })
            .name("Date")
            .description("Creates a new Date with the given parameters.")
            .examples("set {_date} to date(2026, 1, 1)")
            .since("INSERT VERSION")
            .register();

        Functions.newJavaFunction(reg, new JavaFunction<>(
                "dateTime",
                new FunctionParameter[]{
                    new FunctionParameter<>("year", Number.class, true),
                    new FunctionParameter<>("month", Number.class, true),
                    new FunctionParameter<>("day", Number.class, true),
                    new FunctionParameter<>("hour", Number.class, true),
                    new FunctionParameter<>("minute", Number.class, true),
                    new FunctionParameter<>("second", Number.class, true)
                },
                SkriptDate.class,
                true) {
                @Override
                public SkriptDate[] executeSimple(Object[][] params) {
                    Number year = (Number) params[0][0];
                    Number month = (Number) params[1][0];
                    Number day = (Number) params[2][0];
                    Number hour = (Number) params[3][0];
                    Number minute = (Number) params[4][0];
                    Number second = (Number) params[5][0];
                    LocalDateTime localDateTime = LocalDateTime.of(year.intValue(), month.intValue(),
                        day.intValue(), hour.intValue(), minute.intValue(), second.intValue());
                    long epochSecond = localDateTime.toEpochSecond(ZoneOffset.systemDefault().getRules().getOffset(localDateTime));
                    return new SkriptDate[]{SkriptDate.of(epochSecond * 1000)};
                }
            })
            .name("DateTime")
            .description("Creates a new Date with a time with the given parameters.",
                "Reminder this is on a 24 hour clock.")
            .examples("set {_date} to dateTime(2026, 1, 1, 12, 30, 0)")
            .since("INSERT VERSION")
            .register();

        Functions.newJavaFunction(reg, new JavaFunction<>(
                "time",
                new FunctionParameter[]{
                    new FunctionParameter<>("hour", Number.class, true),
                    new FunctionParameter<>("minute", Number.class, true),
                    new FunctionParameter<>("second", Number.class, true)
                },
                Time.class,
                true) {
                @Override
                public Time[] executeSimple(Object[][] params) {
                    Number hour = (Number) params[0][0];
                    Number minute = (Number) params[1][0];
                    Number second = (Number) params[2][0];
                    return new Time[]{Time.of(hour.intValue(), minute.intValue(), second.intValue(), 0)};
                }
            })
            .name("Time")
            .description("Creates a new Time with the given parameters.",
                "Reminder this is on a 24 hour clock.")
            .examples("set {_time} to time(12, 0, 0)")
            .since("INSERT VERSION")
            .register();
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

        Functions.newJavaFunction(reg, new JavaFunction<>(
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
