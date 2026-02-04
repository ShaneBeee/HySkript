package com.github.skriptdev.skript.plugin.elements.types;

import com.github.skriptdev.skript.api.skript.registration.EnumRegistry;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeDoublePosition;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeIntPosition;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.worldgen.zone.Zone;

public class TypesWorld {

    static void register(SkriptRegistration reg) {
        reg.newType(WorldChunk.class, "chunk", "chunk@s")
            .name("Chunk")
            .description("Represents a chunk in a world. A chunk is a 32x32x(world height) set of blocks.")
            .since("1.0.0")
            .toStringFunction(worldChunk -> "chunk (x=" + worldChunk.getX() + ",z=" + worldChunk.getZ() + ") in world '" + worldChunk.getWorld().getName() + "'")
            .register();
        reg.newType(RelativeDoublePosition.class, "relativeposition", "relativePosition@s")
            .name("Relative Position")
            .description("Represents a position relative to another position.")
            .since("INSERT VERSION")
            .register();
        reg.newType(RelativeIntPosition.class, "relativeblockposition", "relativeBlockPosition@s")
            .name("Relative Block Position")
            .description("Represents a block position relative to another block position.")
            .since("INSERT VERSION")
            .register();
        EnumRegistry.register(reg, SoundCategory.class, "soundcategory", "soundcategor@y@ies")
            .name("Sound Category")
            .description("Represents a sound category.")
            .since("1.0.0")
            .register();
        reg.newType(World.class, "world", "world@s")
            .name("World")
            .description("Represents a world in the game.")
            .since("1.0.0")
            .toStringFunction(World::getName)
            .register();
        reg.newType(Zone.class, "zone", "zone@s")
            .name("Zone")
            .description("Represents a zone in the game.")
            .since("1.0.0")
            .toStringFunction(zone -> String.format("Zone{id=%s, name=%s}", zone.id(), zone.name()))
            .register();
    }

}
