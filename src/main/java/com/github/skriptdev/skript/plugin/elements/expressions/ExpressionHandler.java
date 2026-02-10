package com.github.skriptdev.skript.plugin.elements.expressions;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.plugin.elements.expressions.block.ExprBlockAt;
import com.github.skriptdev.skript.plugin.elements.expressions.block.ExprBlockFluid;
import com.github.skriptdev.skript.plugin.elements.expressions.block.ExprBlockIterator;
import com.github.skriptdev.skript.plugin.elements.expressions.block.ExprBlockSphere;
import com.github.skriptdev.skript.plugin.elements.expressions.block.ExprBlockTypeAtLocation;
import com.github.skriptdev.skript.plugin.elements.expressions.block.ExprBlockTypeOfBlock;
import com.github.skriptdev.skript.plugin.elements.expressions.block.ExprBlockFluidLevel;
import com.github.skriptdev.skript.plugin.elements.expressions.block.ExprTargetBlockOfPlayer;
import com.github.skriptdev.skript.plugin.elements.expressions.entity.ExprActiveSlot;
import com.github.skriptdev.skript.plugin.elements.expressions.entity.ExprEntitiesInRadius;
import com.github.skriptdev.skript.plugin.elements.expressions.entityeffect.ExprActiveEntityEffectDuration;
import com.github.skriptdev.skript.plugin.elements.expressions.entityeffect.ExprActiveEntityEffects;
import com.github.skriptdev.skript.plugin.elements.expressions.entity.ExprEntityHealth;
import com.github.skriptdev.skript.plugin.elements.expressions.entity.ExprEntityStat;
import com.github.skriptdev.skript.plugin.elements.expressions.entity.ExprEntityVelocity;
import com.github.skriptdev.skript.plugin.elements.expressions.entity.ExprHeldItem;
import com.github.skriptdev.skript.plugin.elements.expressions.entity.ExprNPCType;
import com.github.skriptdev.skript.plugin.elements.expressions.entity.ExprName;
import com.github.skriptdev.skript.plugin.elements.expressions.entity.ExprTargetEntityOfEntity;
import com.github.skriptdev.skript.plugin.elements.expressions.entityeffect.ExprActiveEntityEffectEffect;
import com.github.skriptdev.skript.plugin.elements.expressions.item.ExprInventory;
import com.github.skriptdev.skript.plugin.elements.expressions.item.ExprInventorySlot;
import com.github.skriptdev.skript.plugin.elements.expressions.item.ExprInventorySlots;
import com.github.skriptdev.skript.plugin.elements.expressions.item.ExprItemContainer;
import com.github.skriptdev.skript.plugin.elements.expressions.item.ExprItemStack;
import com.github.skriptdev.skript.plugin.elements.expressions.item.ExprItemType;
import com.github.skriptdev.skript.plugin.elements.expressions.item.ExprItemsInInventory;
import com.github.skriptdev.skript.plugin.elements.expressions.other.ExprCast;
import com.github.skriptdev.skript.plugin.elements.expressions.other.ExprClassInfoOf;
import com.github.skriptdev.skript.plugin.elements.expressions.other.ExprCurrentContext;
import com.github.skriptdev.skript.plugin.elements.expressions.other.ExprDistance;
import com.github.skriptdev.skript.plugin.elements.expressions.other.ExprLocationDirection;
import com.github.skriptdev.skript.plugin.elements.expressions.other.ExprLocationOf;
import com.github.skriptdev.skript.plugin.elements.expressions.other.ExprMessage;
import com.github.skriptdev.skript.plugin.elements.expressions.other.ExprMessageColor;
import com.github.skriptdev.skript.plugin.elements.expressions.other.ExprMessageLink;
import com.github.skriptdev.skript.plugin.elements.expressions.other.ExprMessageParam;
import com.github.skriptdev.skript.plugin.elements.expressions.other.ExprMessageProperties;
import com.github.skriptdev.skript.plugin.elements.expressions.other.ExprUUID;
import com.github.skriptdev.skript.plugin.elements.expressions.other.ExprUUIDRandom;
import com.github.skriptdev.skript.plugin.elements.expressions.other.ExprVector3d;
import com.github.skriptdev.skript.plugin.elements.expressions.other.ExprVector3f;
import com.github.skriptdev.skript.plugin.elements.expressions.other.ExprVector3i;
import com.github.skriptdev.skript.plugin.elements.expressions.player.ExprAllPlayers;
import com.github.skriptdev.skript.plugin.elements.expressions.player.ExprChatMessage;
import com.github.skriptdev.skript.plugin.elements.expressions.player.ExprGameMode;
import com.github.skriptdev.skript.plugin.elements.expressions.player.ExprPlayerPermissionGroup;
import com.github.skriptdev.skript.plugin.elements.expressions.player.ExprPlayerPermissions;
import com.github.skriptdev.skript.plugin.elements.expressions.player.ExprPlayerRespawnLocations;
import com.github.skriptdev.skript.plugin.elements.expressions.server.ExprConsole;
import com.github.skriptdev.skript.plugin.elements.expressions.world.ExprChunkAtLocation;
import com.github.skriptdev.skript.plugin.elements.expressions.world.ExprRelativePositionResolve;
import com.github.skriptdev.skript.plugin.elements.expressions.world.ExprWorld;
import com.github.skriptdev.skript.plugin.elements.expressions.world.ExprWorldOf;
import com.github.skriptdev.skript.plugin.elements.expressions.world.ExprWorldSpawn;

public class ExpressionHandler {

    public static void register(SkriptRegistration registration) {
        // BLOCK
        ExprBlockAt.register(registration);
        ExprBlockFluid.register(registration);
        ExprBlockFluidLevel.register(registration);
        ExprBlockIterator.register(registration);
        ExprBlockSphere.register(registration);
        ExprBlockTypeAtLocation.register(registration);
        ExprBlockTypeOfBlock.register(registration);
        ExprTargetBlockOfPlayer.register(registration);

        // ENTITY
        ExprActiveSlot.register(registration);
        ExprEntitiesInRadius.register(registration);
        ExprEntityHealth.register(registration);
        ExprEntityStat.register(registration);
        ExprEntityVelocity.register(registration);
        ExprHeldItem.register(registration);
        ExprName.register(registration);
        ExprNPCType.register(registration);
        ExprTargetEntityOfEntity.register(registration);

        // ENTITY EFFECT
        ExprActiveEntityEffectDuration.register(registration);
        ExprActiveEntityEffectEffect.register(registration);
        ExprActiveEntityEffects.register(registration);

        // ITEM
        ExprInventory.register(registration);
        ExprInventorySlot.register(registration);
        ExprInventorySlots.register(registration);
        ExprItemContainer.register(registration);
        ExprItemsInInventory.register(registration);
        ExprItemStack.register(registration);
        ExprItemType.register(registration);

        // OTHER
        ExprCast.register(registration);
        ExprClassInfoOf.register(registration);
        ExprDistance.register(registration);
        ExprLocationDirection.register(registration);
        ExprLocationOf.register(registration);
        ExprMessage.register(registration);
        ExprMessageColor.register(registration);
        ExprMessageLink.register(registration);
        ExprMessageParam.register(registration);
        ExprMessageProperties.register(registration);
        ExprUUID.register(registration);
        ExprUUIDRandom.register(registration);
        ExprVector3d.register(registration);
        ExprVector3f.register(registration);
        ExprVector3i.register(registration);

        // PLAYER
        ExprAllPlayers.register(registration);
        ExprChatMessage.register(registration);
        ExprGameMode.register(registration);
        ExprPlayerPermissionGroup.register(registration);
        ExprPlayerPermissions.register(registration);
        ExprPlayerRespawnLocations.register(registration);

        // SERVER
        ExprConsole.register(registration);

        // WORLD
        ExprChunkAtLocation.register(registration);
        ExprRelativePositionResolve.register(registration);
        ExprWorld.register(registration);
        ExprWorldOf.register(registration);
        ExprWorldSpawn.register(registration);

        // TEST
        ExprCurrentContext.register(registration);

    }

}
