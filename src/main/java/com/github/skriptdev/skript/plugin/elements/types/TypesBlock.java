package com.github.skriptdev.skript.plugin.elements.types;

import com.github.skriptdev.skript.api.hytale.Block;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;

public class TypesBlock {

    static void register(SkriptRegistration registration) {
        registration.newType(Block.class, "block", "block@s")
            .name("Block")
            .description("Represents a block in a world.")
            .since("1.0.0")
            .toStringFunction(Block::toTypeString)
            .register();
    }

}
