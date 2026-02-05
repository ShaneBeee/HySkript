package com.github.skriptdev.skript.api.skript.command;

import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;
import com.hypixel.hytale.server.npc.commands.NPCCommand;

import java.util.Map;
import java.util.TreeMap;

/**
 * Registration shortcuts for string to ArgumentType mappings.
 */
public class ArgUtils {

    private static final Map<String, ArgumentType<?>> TYPES_MAP = new TreeMap<>();

    public static void init() {
        // BASIC
        register(ArgTypes.BOOLEAN, "boolean", "bool");
        register(ArgTypes.STRING, "string", "text");
        register(ArgTypes.UUID, "uuid");

        // NUMBERS
        register(ArgTypes.DOUBLE, "double");
        register(ArgTypes.FLOAT, "float");
        register(ArgTypes.INTEGER, "integer", "int");

        // ENTITY
        register(NPCCommand.NPC_ROLE, "role", "npcrole", "npc_role");
        register(ArgTypes.PLAYER_REF, "player_ref", "playerref");

        // ASSETS
        register(ArgTypes.BLOCK_TYPE_ASSET, "block_type", "blocktype");
        register(ArgTypes.EFFECT_ASSET, "entity_effect", "entityeffect");
        register(ArgTypes.ITEM_ASSET, "item");
        register(ArgTypes.SOUND_CATEGORY, "sound_category");
        register(ArgTypes.SOUND_EVENT_ASSET, "sound_event", "soundevent", "sound");

        // WORLD
        register(ArgTypes.RELATIVE_BLOCK_POSITION, "relative_block_position");
        register(ArgTypes.RELATIVE_POSITION, "relative_position");
        register(ArgTypes.ROTATION, "rotation", "vector3f");
        register(ArgTypes.VECTOR3I, "vector3i");
        register(ArgTypes.WORLD, "world");
    }

    private static void register(ArgumentType<?> type, String... names) {
        for (String name : names) {
            TYPES_MAP.put(name, type);
        }
    }

    /**
     * Get an argument type by its name.
     *
     * @param name Name of the argument type.
     * @return The argument type if found, otherwise null.
     */
    public static ArgumentType<?> getType(String name) {
        return TYPES_MAP.get(name);
    }

    public static String getTypeUsage() {
        return String.join(", ", TYPES_MAP.keySet().stream().sorted().toList());
    }

}
