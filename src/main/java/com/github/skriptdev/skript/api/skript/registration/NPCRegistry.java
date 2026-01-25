package com.github.skriptdev.skript.api.skript.registration;

import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class NPCRegistry {

    private static final Map<String, NPCRole> ROLE_NAME_MAP = new TreeMap<>();
    private static final Map<Integer, NPCRole> ROLE_INDEX_MAP = new TreeMap<>();

    static {
        NPCPlugin npcPlugin = NPCPlugin.get();
        for (String name : npcPlugin.getRoleTemplateNames(false)) {
            int index = npcPlugin.getIndex(name);
            ROLE_NAME_MAP.put(name.toLowerCase(Locale.ROOT), new NPCRole(name, index));
            ROLE_INDEX_MAP.put(index, new NPCRole(name, index));
        }
    }

    public static NPCRole getByIndex(int index) {
        return ROLE_INDEX_MAP.get(index);
    }

    public static String getTypeUsage() {
        return String.join(", ", ROLE_NAME_MAP.keySet());
    }

    public static NPCRole parse(String name) {
        return ROLE_NAME_MAP.get(name.toLowerCase(Locale.ROOT));
    }

    public static String stringify(NPCEntity entity) {
        String roleName = entity.getRoleName();
        if (roleName != null) return "NPCEntity{role=" + roleName + "}";

        int roleIndex = entity.getRoleIndex();
        NPCRole npcRole = ROLE_INDEX_MAP.get(roleIndex);
        if (npcRole != null) return "NPCEntity{role=" + npcRole.name() + "}";
        return "NPCEntity{role=null}";
    }

    public record NPCRole(String name, int index) {
        // Hytale doesn't appear to have an actual storage of roles
        // They just use strings everywhere
    }

}
