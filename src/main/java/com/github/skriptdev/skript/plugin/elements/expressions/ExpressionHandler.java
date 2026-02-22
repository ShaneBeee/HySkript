package com.github.skriptdev.skript.plugin.elements.expressions;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.plugin.elements.expressions.block.ExprBlockAt;
import com.github.skriptdev.skript.plugin.elements.expressions.block.ExprBlockFluid;
import com.github.skriptdev.skript.plugin.elements.expressions.block.ExprBlockHealth;
import com.github.skriptdev.skript.plugin.elements.expressions.block.ExprBlockIterator;
import com.github.skriptdev.skript.plugin.elements.expressions.block.ExprBlockRotation;
import com.github.skriptdev.skript.plugin.elements.expressions.block.ExprBlockSphere;
import com.github.skriptdev.skript.plugin.elements.expressions.block.ExprBlockTypeAtLocation;
import com.github.skriptdev.skript.plugin.elements.expressions.block.ExprBlockTypeOfBlock;
import com.github.skriptdev.skript.plugin.elements.expressions.block.ExprBlockFluidLevel;
import com.github.skriptdev.skript.plugin.elements.expressions.block.ExprHighestBlock;
import com.github.skriptdev.skript.plugin.elements.expressions.block.ExprTargetBlockOfPlayer;
import com.github.skriptdev.skript.plugin.elements.expressions.entity.ExprActiveSlot;
import com.github.skriptdev.skript.plugin.elements.expressions.entity.ExprEntitiesInRadius;
import com.github.skriptdev.skript.plugin.elements.expressions.entity.ExprEntityComponents;
import com.github.skriptdev.skript.plugin.elements.expressions.entity.ExprEntityStamina;
import com.github.skriptdev.skript.plugin.elements.expressions.item.ExprItemQuality;
import com.github.skriptdev.skript.plugin.elements.expressions.other.ExprValueWithin;
import com.github.skriptdev.skript.plugin.elements.expressions.player.ExprPlayerDefenseLevel;
import com.github.skriptdev.skript.plugin.elements.expressions.player.ExprPlayerMovementBaseSpeed;
import com.github.skriptdev.skript.plugin.elements.expressions.entity.ExprEntityHeadRotation;
import com.github.skriptdev.skript.plugin.elements.expressions.entity.ExprEntityScale;
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
import com.github.skriptdev.skript.plugin.elements.expressions.item.ExprInventoryAmountOfItems;
import com.github.skriptdev.skript.plugin.elements.expressions.item.ExprInventorySlot;
import com.github.skriptdev.skript.plugin.elements.expressions.item.ExprInventorySlots;
import com.github.skriptdev.skript.plugin.elements.expressions.item.ExprItemContainer;
import com.github.skriptdev.skript.plugin.elements.expressions.item.ExprItemStack;
import com.github.skriptdev.skript.plugin.elements.expressions.item.ExprItemStackName;
import com.github.skriptdev.skript.plugin.elements.expressions.item.ExprItemStackQuantity;
import com.github.skriptdev.skript.plugin.elements.expressions.item.ExprItemStackWithQuantity;
import com.github.skriptdev.skript.plugin.elements.expressions.item.ExprItemType;
import com.github.skriptdev.skript.plugin.elements.expressions.item.ExprItemsInInventory;
import com.github.skriptdev.skript.plugin.elements.expressions.other.ExprCast;
import com.github.skriptdev.skript.plugin.elements.expressions.other.ExprClassInfoOf;
import com.github.skriptdev.skript.plugin.elements.expressions.other.ExprCoordinates;
import com.github.skriptdev.skript.plugin.elements.expressions.other.ExprCurrentContext;
import com.github.skriptdev.skript.plugin.elements.expressions.other.ExprDistance;
import com.github.skriptdev.skript.plugin.elements.expressions.other.ExprLocationDirection;
import com.github.skriptdev.skript.plugin.elements.expressions.other.ExprLocationOf;
import com.github.skriptdev.skript.plugin.elements.expressions.other.ExprLocationOffset;
import com.github.skriptdev.skript.plugin.elements.expressions.other.ExprLocationRotation;
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
import com.github.skriptdev.skript.plugin.elements.expressions.player.ExprPlayerClientViewRadius;
import com.github.skriptdev.skript.plugin.elements.expressions.player.ExprPlayerMovementFlySpeed;
import com.github.skriptdev.skript.plugin.elements.expressions.player.ExprPlayerMovementJumpForce;
import com.github.skriptdev.skript.plugin.elements.expressions.player.ExprPlayerMovementMass;
import com.github.skriptdev.skript.plugin.elements.expressions.player.ExprPlayerMovementSwimJumpForce;
import com.github.skriptdev.skript.plugin.elements.expressions.player.ExprPlayerPermissionGroup;
import com.github.skriptdev.skript.plugin.elements.expressions.player.ExprPlayerPermissions;
import com.github.skriptdev.skript.plugin.elements.expressions.player.ExprPlayerRespawnLocations;
import com.github.skriptdev.skript.plugin.elements.expressions.server.ExprConsole;
import com.github.skriptdev.skript.plugin.elements.expressions.server.ExprServerViewRadius;
import com.github.skriptdev.skript.plugin.elements.expressions.world.ExprChunkAtLocation;
import com.github.skriptdev.skript.plugin.elements.expressions.world.ExprEnvironmentAtLocation;
import com.github.skriptdev.skript.plugin.elements.expressions.world.ExprRelativePositionResolve;
import com.github.skriptdev.skript.plugin.elements.expressions.world.ExprWorld;
import com.github.skriptdev.skript.plugin.elements.expressions.world.ExprWorldCurrentMSPT;
import com.github.skriptdev.skript.plugin.elements.expressions.world.ExprWorldCurrentTPS;
import com.github.skriptdev.skript.plugin.elements.expressions.world.ExprWorldOf;
import com.github.skriptdev.skript.plugin.elements.expressions.world.ExprWorldSpawn;
import com.github.skriptdev.skript.plugin.elements.expressions.world.ExprWorldDateTime;
import com.github.skriptdev.skript.plugin.elements.expressions.world.ExprWorldTPS;
import com.github.skriptdev.skript.plugin.elements.expressions.world.ExprWorldTimeDurations;

public class ExpressionHandler {

    public static void register(SkriptRegistration registration) {
        // BLOCK
        ExprBlockAt.register(registration);
        ExprBlockFluid.register(registration);
        ExprBlockFluidLevel.register(registration);
        ExprBlockHealth.register(registration);
        ExprBlockIterator.register(registration);
        ExprBlockRotation.register(registration);
        ExprBlockSphere.register(registration);
        ExprBlockTypeAtLocation.register(registration);
        ExprBlockTypeOfBlock.register(registration);
        ExprHighestBlock.register(registration);
        ExprTargetBlockOfPlayer.register(registration);

        // ENTITY
        ExprActiveSlot.register(registration);
        ExprEntitiesInRadius.register(registration);
        ExprEntityComponents.register(registration);
        ExprEntityHeadRotation.register(registration);
        ExprEntityHealth.register(registration);
        ExprEntityScale.register(registration);
        ExprEntityStamina.register(registration);
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
        ExprInventoryAmountOfItems.register(registration);
        ExprInventorySlot.register(registration);
        ExprInventorySlots.register(registration);
        ExprItemContainer.register(registration);
        ExprItemQuality.register(registration);
        ExprItemsInInventory.register(registration);
        ExprItemStack.register(registration);
        ExprItemStackName.register(registration);
        ExprItemStackQuantity.register(registration);
        ExprItemStackWithQuantity.register(registration);
        ExprItemType.register(registration);

        // OTHER
        ExprCast.register(registration);
        ExprClassInfoOf.register(registration);
        ExprCoordinates.register(registration);
        ExprDistance.register(registration);
        ExprLocationDirection.register(registration);
        ExprLocationOf.register(registration);
        ExprLocationOffset.register(registration);
        ExprLocationRotation.register(registration);
        ExprMessage.register(registration);
        ExprMessageColor.register(registration);
        ExprMessageLink.register(registration);
        ExprMessageParam.register(registration);
        ExprMessageProperties.register(registration);
        ExprUUID.register(registration);
        ExprUUIDRandom.register(registration);
        ExprValueWithin.register(registration);
        ExprVector3d.register(registration);
        ExprVector3f.register(registration);
        ExprVector3i.register(registration);

        // PLAYER
        ExprAllPlayers.register(registration);
        ExprChatMessage.register(registration);
        ExprGameMode.register(registration);
        ExprPlayerClientViewRadius.register(registration);
        ExprPlayerDefenseLevel.register(registration);
        ExprPlayerMovementBaseSpeed.register(registration);
        ExprPlayerMovementFlySpeed.register(registration);
        ExprPlayerMovementJumpForce.register(registration);
        ExprPlayerMovementMass.register(registration);
        ExprPlayerMovementSwimJumpForce.register(registration);
        ExprPlayerPermissionGroup.register(registration);
        ExprPlayerPermissions.register(registration);
        ExprPlayerRespawnLocations.register(registration);

        // SERVER
        ExprConsole.register(registration);
        ExprServerViewRadius.register(registration);

        // WORLD
        ExprChunkAtLocation.register(registration);
        ExprEnvironmentAtLocation.register(registration);
        ExprRelativePositionResolve.register(registration);
        ExprWorld.register(registration);
        ExprWorldCurrentMSPT.register(registration);
        ExprWorldCurrentTPS.register(registration);
        ExprWorldDateTime.register(registration);
        ExprWorldOf.register(registration);
        ExprWorldSpawn.register(registration);
        ExprWorldTimeDurations.register(registration);
        ExprWorldTPS.register(registration);

        // TEST
        ExprCurrentContext.register(registration);

    }

}
