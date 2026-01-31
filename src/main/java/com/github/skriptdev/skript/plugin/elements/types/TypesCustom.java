package com.github.skriptdev.skript.plugin.elements.types;

import com.github.skriptdev.skript.api.hytale.Direction;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;

public class TypesCustom {

    static void register(SkriptRegistration reg) {
        reg.newType(Direction.class, "direction", "direction@s")
            .name("Direction")
            .description("Represents a direction in the world.")
            .usage(Direction.getUsageString())
            .literalParser(Direction::parse)
            .toStringFunction(Direction::getName)
            .since("1.0.0")
            .register();
    }

}
