package com.github.skriptdev.skript.plugin.elements.types;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;

public class TypesWorld {

    static void register(SkriptRegistration registration) {
        registration.newType(World.class, "world", "world@s")
            .name("World")
            .description("Represents a world in the game.")
            .since("1.0.0")
            .toStringFunction(World::getName)
            .register();
        registration.newType(WorldChunk.class, "chunk", "chunk@s")
            .name("Chunk")
            .description("Represents a chunk in a world. A chunk is a 32x32x(world height) set of blocks.")
            .since("1.0.0")
            .toStringFunction(worldChunk -> "chunk (x=" + worldChunk.getX() + ",z=" + worldChunk.getZ() + ") in world '" + worldChunk.getWorld().getName() + "'")
            .register();
    }

}
