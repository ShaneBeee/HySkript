package com.github.skriptdev.skript.plugin.elements.types;

import com.github.skriptdev.skript.api.hytale.Block;
import com.github.skriptdev.skript.api.skript.registration.EnumRegistry;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.protocol.InteractionType;

public class TypesBlock {

    static void register(SkriptRegistration registration) {
        registration.newType(Block.class, "block", "block@s")
            .name("Block")
            .description("Represents a block in a world.")
            .since("1.0.0")
            .toStringFunction(Block::toTypeString)
            .register();
        EnumRegistry.register(registration, InteractionType.class, "interactiontype", "interactiontype@s")
            .name("Interaction Type")
            .description("Represents the types of interactions that can be performed on a block.")
            .since("INSERT VERSION")
            .register();
    }

}
