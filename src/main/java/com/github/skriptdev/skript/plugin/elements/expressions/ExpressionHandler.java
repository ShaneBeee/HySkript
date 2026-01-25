package com.github.skriptdev.skript.plugin.elements.expressions;

import io.github.syst3ms.skriptparser.registration.SkriptRegistration;

public class ExpressionHandler {

    public static void register(SkriptRegistration registration) {
        ExprAllPlayers.register(registration);
        ExprBlockType.register(registration);
        ExprClassInfoOf.register(registration);
        ExprInventory.register(registration);
        ExprItemStack.register(registration);
        ExprItemType.register(registration);
        ExprLocationOf.register(registration);
        ExprName.register(registration);
        ExprNPCType.register(registration);
        ExprUUID.register(registration);
        ExprUUIDRandom.register(registration);
        ExprVector3d.register(registration);
        ExprVector3f.register(registration);
        ExprVector3i.register(registration);
        ExprWorld.register(registration);
        ExprWorldOfEntity.register(registration);
        ExprWorldOfLocation.register(registration);
    }

}
