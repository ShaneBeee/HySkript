package com.github.skriptdev.skript.plugin.elements.expressions;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;

public class ExpressionHandler {

    public static void register(SkriptRegistration registration) {
        ExprAllPlayers.register(registration);
        ExprBlockAt.register(registration);
        ExprBlockType.register(registration);
        ExprChatMessage.register(registration);
        ExprClassInfoOf.register(registration);
        ExprConsole.register(registration);
        ExprEntityHealth.register(registration);
        ExprEntityStat.register(registration);
        ExprInventory.register(registration);
        ExprItemContainer.register(registration);
        ExprItemStack.register(registration);
        ExprItemType.register(registration);
        ExprLocationDirection.register(registration);
        ExprLocationOf.register(registration);
        ExprMessage.register(registration);
        ExprMessageColor.register(registration);
        ExprMessageLink.register(registration);
        ExprMessageParam.register(registration);
        ExprMessageProperties.register(registration);
        ExprName.register(registration);
        ExprNPCType.register(registration);
        ExprPlayerSpawns.register(registration);
        ExprUUID.register(registration);
        ExprUUIDRandom.register(registration);
        ExprVector3d.register(registration);
        ExprVector3f.register(registration);
        ExprVector3i.register(registration);
        ExprWorld.register(registration);
        ExprWorldOf.register(registration);
        ExprWorldSpawn.register(registration);
    }

}
