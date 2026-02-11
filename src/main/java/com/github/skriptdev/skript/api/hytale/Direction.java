package com.github.skriptdev.skript.api.hytale;

import com.github.skriptdev.skript.api.hytale.utils.LocationUtils;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;

/**
 * Represents a direction.
 */
public enum Direction {

    NORTH("North", (location, offset) -> create(location, offset, 0, 0, -1)),
    SOUTH("South", (location, offset) -> create(location, offset, 0, 0, 1)),
    EAST("East", (location, offset) -> create(location, offset, 1, 0, 0)),
    WEST("West", (location, offset) -> create(location, offset, -1, 0, 0)),
    UP("Up", (location, offset) -> create(location, offset, 0, 1, 0), "above", "up", "up from"),
    DOWN("Down", (location, offset) -> create(location, offset, 0, -1, 0), "below", "down", "down from");

    private final String name;
    private final BiFunction<Location, Number, Location> function;
    private final String[] patterns;

    Direction(String name, BiFunction<Location, Number, Location> function, String... patterns) {
        this.name = name;
        this.function = function;
        if (patterns.length > 0) this.patterns = patterns;
        else this.patterns = new String[]{name.toLowerCase(Locale.ROOT)};
    }

    public Location apply(Location location, Number offset) {
        Location clone = LocationUtils.clone(location);
        return function.apply(clone, offset);
    }

    public Location apply(Location location) {
        Location clone = LocationUtils.clone(location);
        return function.apply(clone, 1);
    }

    public String getName() {
        return this.name;
    }

    public String getVariableName() {
        return "direction:" + this.name.toLowerCase(Locale.ROOT);
    }

    private static Location create(Location location, Number offset, int x, int y, int z) {
        double value = offset.doubleValue();
        Vector3d add = location.getPosition().add(x * value, y * value, z * value);
        return new Location(location.getWorld(), add);
    }

    public static Direction parse(String string) {
        for (Direction value : Direction.values()) {
            for (String pattern : value.patterns) {
                if (pattern.equalsIgnoreCase(string)) return value;
            }
        }
        return null;
    }

    public static String getUsageString() {
        List<String> names = new ArrayList<>();
        for (Direction value : Direction.values()) {
            names.addAll(Arrays.asList(value.patterns));
        }
        return String.join(", ", names.stream().sorted().toList());
    }

}
