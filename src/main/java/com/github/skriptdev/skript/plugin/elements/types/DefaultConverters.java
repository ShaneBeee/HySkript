package com.github.skriptdev.skript.plugin.elements.types;

import com.github.skriptdev.skript.api.hytale.utils.EntityUtils;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.util.MessageUtil;
import io.github.syst3ms.skriptparser.types.conversions.Converters;

import java.util.Optional;

public class DefaultConverters {

    public static void register() {
        entity();
        inventory();
        other();
    }

    @SuppressWarnings("removal")
    private static void entity() {
        // Player to PlayerRef
        Converters.registerConverter(Player.class, PlayerRef.class, (player) ->
            Optional.ofNullable(player.getPlayerRef()));

        // Entity to Location
        Converters.registerConverter(Entity.class, Location.class, entity -> {
            World world = entity.getWorld();
            if (world == null) return Optional.empty();

            TransformComponent component = EntityUtils.getComponent(entity, TransformComponent.getComponentType());
            if (component == null) return Optional.empty();

            Vector3d pos = component.getPosition();
            Vector3f rotation = component.getRotation();
            Location location = new Location(world.getName(), pos, rotation);
            return Optional.of(location);
        });

        // Player to Location
        Converters.registerConverter(Player.class, Location.class, entity -> {
            World world = entity.getWorld();
            if (world == null) return Optional.empty();

            TransformComponent component = EntityUtils.getComponent(entity, TransformComponent.getComponentType());
            if (component == null) return Optional.empty();

            Vector3d pos = component.getPosition();
            Vector3f rotation = component.getRotation();
            Location location = new Location(world.getName(), pos, rotation);
            return Optional.of(location);
        });
    }

    private static void inventory() {
        // Item to BlockType
        Converters.registerConverter(Item.class, BlockType.class, (item) -> {
            if (item.hasBlockType()) {
                String blockId = item.getBlockId();
                BlockType asset = BlockType.getAssetMap().getAsset(blockId);
                if (asset != null) return Optional.of(asset);
                return Optional.empty();
            }
            return Optional.empty();
        });

        // BlockType to Item
        Converters.registerConverter(BlockType.class, Item.class, (blockType) -> {
            Item item = blockType.getItem();
            if (item != null) return Optional.of(item);
            return Optional.empty();
        });
    }

    private static void other() {
        Converters.registerConverter(Message.class, String.class, (message) ->
            Optional.of(MessageUtil.toAnsiString(message).toAnsi()));
    }

}
