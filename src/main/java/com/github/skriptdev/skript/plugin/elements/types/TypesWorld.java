package com.github.skriptdev.skript.plugin.elements.types;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeDoublePosition;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeIntPosition;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.worldgen.zone.Zone;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import io.github.syst3ms.skriptparser.types.changers.Changer;
import io.github.syst3ms.skriptparser.util.CollectionUtils;
import org.jetbrains.annotations.NotNull;

public class TypesWorld {

    static void register(SkriptRegistration reg) {
        reg.newType(WorldChunk.class, "chunk", "chunk@s")
            .name("Chunk")
            .description("Represents a chunk in a world. A chunk is a 32x32x(world height) set of blocks.")
            .since("1.0.0")
            .toStringFunction(worldChunk -> "chunk (x=" + worldChunk.getX() + ",z=" + worldChunk.getZ() +
                ") in world '" + worldChunk.getWorld().getName() + "'")
            .toVariableNameFunction(worldChunk -> "chunk:" + worldChunk.getWorld().getName() +
                ":" + worldChunk.getX() + ":" + worldChunk.getZ())
            .register();
        reg.newType(Ref.class, "ref", "ref@s")
            .name("Reference")
            .description("Represents a reference to different objects in a world, such as entities and chunk components.",
                "References are the base of how Hytale holds objects in a world/chunk (store).")
            .since("1.2.0")
            .toStringFunction(ref ->
                String.format("Ref{store=%s, index=%s}",
                    ref.getStore().getClass().getSimpleName(),
                    ref.getIndex()))
            .defaultChanger(new Changer<>() {
                @Override
                public Class<?>[] acceptsChange(@NotNull ChangeMode mode) {
                    if (mode == ChangeMode.DELETE) {
                        return CollectionUtils.arrayOf(Ref.class);
                    }
                    return null;
                }

                @SuppressWarnings({"rawtypes", "unchecked"})
                @Override
                public void change(Ref @NotNull [] toChange, Object @NotNull [] changeWith, @NotNull ChangeMode mode) {
                    for (Ref ref : toChange) {
                        Store store = ref.getStore();
                        store.removeEntity(ref, RemoveReason.REMOVE);
                    }
                }
            })
            .register();
        reg.newType(RelativeDoublePosition.class, "relativeposition", "relativePosition@s")
            .name("Relative Position")
            .description("Represents a position relative to another position.")
            .since("1.0.0")
            .register();
        reg.newType(RelativeIntPosition.class, "relativeblockposition", "relativeBlockPosition@s")
            .name("Relative Block Position")
            .description("Represents a block position relative to another block position.")
            .since("1.0.0")
            .register();
        reg.newEnumType(SoundCategory.class, "soundcategory", "soundcategor@y@ies")
            .name("Sound Category")
            .description("Represents a sound category.")
            .since("1.0.0")
            .register();
        reg.newType(World.class, "world", "world@s")
            .name("World")
            .description("Represents a world in the game.")
            .since("1.0.0")
            .toStringFunction(World::getName)
            .toVariableNameFunction(w -> "world:" + w.getName())
            .register();
        reg.newType(Zone.class, "zone", "zone@s")
            .name("Zone")
            .description("Represents a zone in the game.")
            .since("1.0.0")
            .toStringFunction(zone -> String.format("Zone{id=%s, name=%s}", zone.id(), zone.name()))
            .register();
    }

}
