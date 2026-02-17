package com.github.skriptdev.skript.plugin.elements.functions;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.util.SkriptDate;
import io.github.syst3ms.skriptparser.util.Time;
import org.bson.BsonDocument;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class DefaultFunctions {

    public static void register(SkriptRegistration reg) {
        dateTimeFunctions(reg);
        itemFunctions(reg);
        mathFunctions(reg);
        positionFunctions(reg);
    }

    private static void dateTimeFunctions(SkriptRegistration reg) {
        reg.newJavaFunction("date", SkriptDate.class, true)
            .parameter("year", Number.class, true)
            .parameter("month", Number.class, true)
            .parameter("day", Number.class, true)
            .executeSingle(params -> {
                Number year = (Number) params[0][0];
                Number month = (Number) params[1][0];
                Number day = (Number) params[2][0];
                LocalDateTime localDateTime = LocalDateTime.of(year.intValue(), month.intValue(),
                    day.intValue(), 0, 0);
                long epochSecond = localDateTime.toEpochSecond(ZoneOffset.systemDefault().getRules().getOffset(localDateTime));
                return SkriptDate.of(epochSecond * 1000);
            })
            .name("Date")
            .description("Creates a new Date with the given parameters.")
            .examples("set {_date} to date(2026, 1, 1)")
            .since("INSERT VERSION")
            .register();

        reg.newJavaFunction("dateTime", SkriptDate.class, true)
            .parameter("year", Number.class, true)
            .parameter("month", Number.class, true)
            .parameter("day", Number.class, true)
            .parameter("hour", Number.class, true)
            .parameter("minute", Number.class, true)
            .parameter("second", Number.class, true)
            .executeSingle(params -> {
                Number year = (Number) params[0][0];
                Number month = (Number) params[1][0];
                Number day = (Number) params[2][0];
                Number hour = (Number) params[3][0];
                Number minute = (Number) params[4][0];
                Number second = (Number) params[5][0];
                LocalDateTime localDateTime = LocalDateTime.of(year.intValue(), month.intValue(),
                    day.intValue(), hour.intValue(), minute.intValue(), second.intValue());
                long epochSecond = localDateTime.toEpochSecond(ZoneOffset.systemDefault().getRules().getOffset(localDateTime));
                return SkriptDate.of(epochSecond * 1000);
            })
            .name("DateTime")
            .description("Creates a new Date with a time with the given parameters.",
                "Reminder this is on a 24 hour clock.")
            .examples("set {_date} to dateTime(2026, 1, 1, 12, 30, 0)")
            .since("INSERT VERSION")
            .register();

        reg.newJavaFunction("time", Time.class, true)
            .parameter("hour", Number.class, true)
            .parameter("minute", Number.class, true)
            .parameter("second", Number.class, true)
            .executeSingle(params -> {
                Number hour = (Number) params[0][0];
                Number minute = (Number) params[1][0];
                Number second = (Number) params[2][0];
                return Time.of(hour.intValue(), minute.intValue(), second.intValue(), 0);

            })
            .name("Time")
            .description("Creates a new Time with the given parameters.",
                "Reminder this is on a 24 hour clock.")
            .examples("set {_time} to time(12, 0, 0)")
            .since("INSERT VERSION")
            .register();
    }

    private static void itemFunctions(SkriptRegistration reg) {
        reg.newJavaFunction("itemstack", ItemStack.class, true)
            .parameter("type", Item.class, true)
            .parameter("quantity", Number.class, true)
            .parameter("durability", Number.class, true)
            .parameter("maxDurability", Number.class, true)
            .executeSingle(params -> {
                Item type = (Item) params[0][0];
                Number quantity = (Number) params[1][0];
                Number durability = (Number) params[2][0];
                Number maxDurability = (Number) params[3][0];
                int max = maxDurability.intValue();
                return new ItemStack(type.getId(), quantity.intValue(),
                    Math.clamp(durability.intValue(), 0, max), max, new BsonDocument());
            })
            .name("ItemStack")
            .description("Creates a new ItemStack with the given parameters.")
            .examples("set {_stack} to itemstack(Food_Fish_Grilled, 1, 50, 100)")
            .since("1.0.0")
            .register();

        reg.newJavaFunction("inventory", Inventory.class, true)
            .parameter("storageCapacity", Number.class)
            .parameter("armorCapacity", Number.class)
            .parameter("hotbarCapacity", Number.class)
            .parameter("utilityCapacity", Number.class)
            .executeSingle(params -> {
                short storage = (short) Math.max(((Number) params[0][0]).shortValue(), 0);
                short armor = (short) Math.max(((Number) params[1][0]).shortValue(), 0);
                short hotbar = (short) Math.max(((Number) params[2][0]).shortValue(), 0);
                short utility = (short) Math.max(((Number) params[3][0]).shortValue(), 0);
                Inventory inv = new Inventory(storage, armor, hotbar, utility, (short) 0);
                // Unregister click listeners since there's no owner
                inv.unregister();
                return inv;
            })
            .name("Inventory")
            .description("Create a new inventory with the given ItemContainer sizes.",
                "This inventory will not have click events due to no owner.",
                "See the `inventoryWithOwner` function for that.")
            .since("INSERT VERSION")
            .register();

        reg.newJavaFunction("inventoryWithOwner", Inventory.class, true)
            .parameter("owner", LivingEntity.class)
            .parameter("storageCapacity", Number.class)
            .parameter("armorCapacity", Number.class)
            .parameter("hotbarCapacity", Number.class)
            .parameter("utilityCapacity", Number.class)
            .executeSingle(params -> {
                LivingEntity livingEntity = (LivingEntity) params[0][0];
                short storage = (short) Math.max(((Number) params[1][0]).shortValue(), 0);
                short armor = (short) Math.max(((Number) params[2][0]).shortValue(), 0);
                short hotbar = (short) Math.max(((Number) params[3][0]).shortValue(), 0);
                short utility = (short) Math.max(((Number) params[4][0]).shortValue(), 0);
                Inventory inv = new Inventory(storage, armor, hotbar, utility, (short) 0);
                inv.setEntity(livingEntity);
                return inv;
            })
            .name("Inventory with Owner")
            .description("Create a new inventory with the given ItemContainer capacities and an owner.",
                "The owner is used for registered click events.")
            .since("INSERT VERSION")
            .register();

        reg.newJavaFunction("itemContainer", ItemContainer.class, true)
            .parameter("capacity", Number.class)
            .executeSingle(params -> {
                short capacity = (short) Math.max(((Number) params[0][0]).shortValue(), 0);
                return new SimpleItemContainer(capacity);
            })
            .name("ItemContainer")
            .description("Create a new ItemContainer with the given capacity.")
            .since("INSERT VERSRION")
            .register();
    }

    private static void mathFunctions(SkriptRegistration reg) {
        reg.newJavaFunction("abs", Number.class, true)
            .parameter("number", Number.class)
            .executeSingle(params -> Math.abs(((Number) params[0][0]).doubleValue()))
            .name("Math - Absolute")
            .description("Returns the absolute (always positive) value of a number.")
            .since("INSERT VERSION")
            .register();
        reg.newJavaFunction("ceil", Number.class, true)
            .parameter("number", Number.class)
            .executeSingle(params -> Math.ceil(((Number) params[0][0]).doubleValue()))
            .name("Math - Ceiling")
            .description("Returns the ceiling (rounded up) value of a decimal point number.")
            .since("INSERT VERSION")
            .register();
        reg.newJavaFunction("cos", Number.class, true)
            .parameter("number", Number.class)
            .executeSingle(params -> Math.cos(((Number) params[0][0]).doubleValue()))
            .name("Math - Cosine")
            .description("Retuns the cosine of an angle.")
            .since("INSERT VERSION")
            .register();
        reg.newJavaFunction("floor", Number.class, true)
            .parameter("number", Number.class)
            .executeSingle(params -> Math.floor(((Number) params[0][0]).doubleValue()))
            .name("Math - Floor")
            .description("Returns the floor (rounded down) value of a decimal point number.")
            .since("INSERT VERSION")
            .register();
        reg.newJavaFunction("sin", Number.class, true)
            .parameter("number", Number.class)
            .executeSingle(params -> Math.sin(((Number) params[0][0]).doubleValue()))
            .name("Math - Sine")
            .description("Retuns the sine of an angle.")
            .since("INSERT VERSION")
            .register();
    }

    private static void positionFunctions(SkriptRegistration reg) {
        reg.newJavaFunction("location", Location.class, true)
            .parameter("x", Number.class)
            .parameter("y", Number.class)
            .parameter("z", Number.class)
            .parameter("world", World.class)
            .executeSingle(params -> {
                Number x = (Number) params[0][0];
                Number y = (Number) params[1][0];
                Number z = (Number) params[2][0];
                World world = (World) params[3][0];
                return new Location(world.getName(), x.doubleValue(), y.doubleValue(), z.doubleValue());

            })
            .name("Location")
            .description("Creates a location in a world.")
            .examples("set {_loc} to location(1, 100, 1, world of player)")
            .since("1.0.0")
            .register();

        reg.newJavaFunction("vector3i", Vector3i.class, true)
            .parameter("x", Number.class)
            .parameter("y", Number.class)
            .parameter("z", Number.class)
            .executeSingle(params -> {
                Number x = (Number) params[0][0];
                Number y = (Number) params[1][0];
                Number z = (Number) params[2][0];
                return new Vector3i(x.intValue(), y.intValue(), z.intValue());
            })
            .name("Vector3i")
            .description("Creates a vector3i with integer coordinates.")
            .examples("set {_v} to vector3i(1, 100, 1)")
            .since("1.0.0")
            .register();

        reg.newJavaFunction("vector3f", Vector3f.class, true)
            .parameter("x", Number.class)
            .parameter("y", Number.class)
            .parameter("z", Number.class)
            .executeSingle(params -> {
                Number x = (Number) params[0][0];
                Number y = (Number) params[1][0];
                Number z = (Number) params[2][0];
                return new Vector3f(x.floatValue(), y.floatValue(), z.floatValue());
            })
            .name("Vector3f")
            .description("Creates a vector3f with float coordinates.")
            .examples("set {_v} to vector3f(1.234, 5.3, 1.999)")
            .since("1.0.0")
            .register();

        reg.newJavaFunction("vector3d", Vector3d.class, true)
            .parameter("x", Number.class)
            .parameter("y", Number.class)
            .parameter("z", Number.class)
            .executeSingle(params -> {
                Number x = (Number) params[0][0];
                Number y = (Number) params[1][0];
                Number z = (Number) params[2][0];
                return new Vector3d(x.doubleValue(), y.doubleValue(), z.doubleValue());
            })
            .name("Vector3d")
            .description("Creates a vector3d with double coordinates.")
            .examples("set {_v} to vector3d(1.234, 5.3, 1.999)")
            .since("1.0.0")
            .register();

        reg.newJavaFunction("world", World.class, true)
            .parameter("name", String.class)
            .executeSingle(params -> Universe.get().getWorld((String) params[0][0]))
            .name("World")
            .description("Returns the world with the given name.")
            .examples("set {_world} to world(\"default\")")
            .since("1.0.0")
            .register();
    }

}
