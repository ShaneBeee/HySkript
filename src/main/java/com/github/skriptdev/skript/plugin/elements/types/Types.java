package com.github.skriptdev.skript.plugin.elements.types;

import com.github.skriptdev.skript.api.skript.registration.AssetStoreRegistry;
import com.github.skriptdev.skript.api.skript.registration.EnumRegistry;
import com.github.skriptdev.skript.api.skript.registration.NPCRegistry;
import com.github.skriptdev.skript.api.utils.Utils;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.builtin.hytalegenerator.assets.biomes.BiomeAsset;
import com.hypixel.hytale.common.util.java.ManifestUtil;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InventoryActionType;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.asset.type.item.config.BlockGroup;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ResourceType;
import com.hypixel.hytale.server.core.asset.type.projectile.config.Projectile;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.asset.type.weather.config.Weather;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.receiver.IMessageReceiver;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.types.TypeManager;

import java.util.UUID;

public class Types {

    public static void register(SkriptRegistration registration) {
        Utils.log("Setting up Types");
        registerJavaTypes(registration);
        registerServerTypes(registration);
        registerEntityTypes(registration);
        registerItemTypes(registration);
        registerBlockTypes(registration);
        registerWorldTypes(registration);
        registerAssetStoreTypes(registration);

        TypeManager.register(registration);
    }

    private static void registerJavaTypes(SkriptRegistration registration) {
        registration.newType(UUID.class, "uuid", "uuid@s")
            .name("UUID")
            .description("Represents a UUID.")
            .examples("set {_uuid} to uuid of {_player}")
            .since("INSERT VERSION")
            .register();
    }

    private static void registerServerTypes(SkriptRegistration registration) {
        registration.newType(CommandSender.class, "commandsender", "commandSender@s")
            .name("Command Sender")
            .description("Represents a command sender such as a player or the console.")
            .since("INSERT VERSION")
            .toStringFunction(CommandSender::getDisplayName)
            .register();
        registration.newType(IMessageReceiver.class, "messagereceiver", "messageReceiver@s")
            .name("Message Receiver")
            .description("Represents a receiver of messages such as a player or the console.")
            .since("INSERT VERSION")
            .register();
        registration.newType(HytaleServer.class, "server", "server@s")
            .name("Server")
            .description("Represents the Hytale server.")
            .since("INSERT VERSION")
            .register();
        registration.newType(Vector3f.class, "vector3f", "vector3f@s")
            .name("Vector3f")
            .description("Represents a vector in 3D space using floats.",
                "Often used for the rotation of entities in a world.")
            .since("INSERT VERSION")
            .register();
        registration.newType(Vector3d.class, "vector3d", "vector3d@s")
            .name("Vector3d")
            .description("Represents a vector in 3D space using doubles.",
                "Often used for the position of entities in a world.")
            .since("INSERT VERSION")
            .register();
        registration.newType(Vector3i.class, "vector3i", "vector3i@s")
            .name("Vector3i")
            .description("Represents a vector in 3D space using integers.",
                "Often used for the position of blocks in a world.")
            .since("INSERT VERSION")
            .register();
        registration.newType(Location.class, "location", "location@s")
            .name("Location")
            .description("Represents a location in a world.",
                "A location contains a world, a position (vector3d) and a rotation (vector3f).")
            .since("INSERT VERSION")
            .register();
    }

    private static void registerEntityTypes(SkriptRegistration registration) {
        registration.newType(NPCRegistry.NPCRole.class, "npcrole", "npcrole@s")
            .name("NPC Role")
            .description("Represents the type of NPCs in the game.")
            .examples("coming soon") // TODO
            .usage(NPCRegistry.getTypeUsage())
            .since("INSERT VERSION")
            .toStringFunction(NPCRegistry.NPCRole::name)
            .literalParser(NPCRegistry::parse)
            .register();
        registration.newType(Entity.class, "entity", "entit@y@ies")
            .toStringFunction(Entity::toString) // TODO get its name or something
            .name("Entity")
            .description("Represents any entity in the game, including players and mobs.")
            .since("INSERT VERSION")
            .register();
        registration.newType(LivingEntity.class, "livingentity", "livingEntit@y@ies")
            .name("Living Entity")
            .description("Represents any living entity in the game, including players and mobs.")
            .since("INSERT VERSION")
            .toStringFunction(LivingEntity::getLegacyDisplayName)
            .register();
        registration.newType(NPCEntity.class, "npcentity", "npcEntit@y@ies")
            .name("NPC Entity")
            .description("Represents an NPC entity in the game.")
            .since("INSERT VERSION")
            .toStringFunction(NPCRegistry::stringify)
            .register();
        registration.newType(Player.class, "player", "player@s")
            .name("Player")
            .description("Represents a player in the game.")
            .since("INSERT VERSION")
            .toStringFunction(Player::getDisplayName)
            .register();
        registration.newType(PlayerRef.class, "playerref", "playerRef@s")
            .name("Player Ref")
            .description("Represents a reference to a player in the game.")
            .since("INSERT VERSION")
            .toStringFunction(PlayerRef::getUsername)
            .register();
    }

    private static void registerItemTypes(SkriptRegistration registration) {
        registration.newType(ItemStack.class, "itemstack", "itemstack@s")
            .name("Item Stack")
            .description("Represents an item in an inventory slot.")
            .examples("set {_i} to itemstack of Food_Fish_Grilled")
            .since("INSERT VERSION")
            .toStringFunction(itemStack -> {
                String quantity = itemStack.getQuantity() == 1 ? "" : itemStack.getQuantity() + " of ";
                return "itemstack of " + quantity + itemStack.getItem().getId();
            })
            .register();
        registration.newType(Inventory.class, "inventory", "inventor@y@ies")
            .name("Inventory")
            .description("Represents an inventory of an entity or block.")
            .since("INSERT VERSION")
            .toStringFunction(Inventory::toString)
            .register();
        EnumRegistry.register(registration, InventoryActionType.class, "inventoryactiontype", "inventoryActionType@s")
            .name("Inventory Action Type")
            .description("Represents the types of actions that can be performed in an inventory.")
            .since("INSERT VERSION")
            .register();
    }

    private static void registerBlockTypes(SkriptRegistration registration) {
    }

    private static void registerWorldTypes(SkriptRegistration registration) {
        registration.newType(World.class, "world", "world@s")
            .name("World")
            .description("Represents a world in the game.")
            .since("INSERT VERSION")
            .toStringFunction(World::getName)
            .register();
        registration.newType(WorldChunk.class, "chunk", "chunk@s")
            .name("Chunk")
            .description("Represents a chunk in a world. A chunk is a 32x32x(world height) set of blocks.")
            .since("INSERT VERSION")
            .toStringFunction(worldChunk -> "chunk (x=" + worldChunk.getX() + ",z=" + worldChunk.getZ() + ") in world '" + worldChunk.getWorld().getName() + "'")
            .register();
    }

    private static void registerAssetStoreTypes(SkriptRegistration registration) {
        AssetStoreRegistry.register(registration, BiomeAsset.class, BiomeAsset.getAssetStore().getAssetMap(), "biome", "biome@s")
            .name("Biome")
            .description("Represents the types of biomes in the game.", autoGenMessage())
            .since("INSERT VERSION")
            .toStringFunction(BiomeAsset::getId)
            .register();
        AssetStoreRegistry.register(registration, BlockGroup.class, AssetRegistry.getAssetStore(BlockGroup.class).getAssetMap(),
                "blockgroup", "blockgroup@s")
            .name("Block Group")
            .description("Represents the groups of blocks in the game.", autoGenMessage())
            .since("INSERT VERSION")
            .toStringFunction(BlockGroup::getId)
            .register();
        AssetStoreRegistry.register(registration, CraftingRecipe.class, CraftingRecipe.getAssetMap(), "craftingrecipe", "craftingrecipe@s")
            .name("Crafting Recipe")
            .description("Represents the crafting recipes in the game.", autoGenMessage())
            .since("INSERT VERSION")
            .toStringFunction(CraftingRecipe::getId)
            .register();
        AssetStoreRegistry.register(registration, BlockType.class, BlockType.getAssetMap(), "blocktype", "blockType@s")
            .name("BlockType")
            .description("Represents the types of blocks in the game.", autoGenMessage())
            .examples("set {_block} to blocktype of block at player")
            .since("INSERT VERSION")
            .toStringFunction(BlockType::getId)
            .register();
        AssetStoreRegistry.register(registration, Fluid.class, Fluid.getAssetMap(), "fluid", "fluid@s")
            .name("Fluid")
            .description("Represents the types of fluids in the game.", autoGenMessage())
            .since("INSERT VERSION")
            .toStringFunction(Fluid::getId)
            .register();
        AssetStoreRegistry.register(registration, EntityEffect.class, EntityEffect.getAssetMap(),
                "entityeffect", "entityEffect@s")
            .name("Entity Effect")
            .description("Represents the types of effects that can be applied to entities.", autoGenMessage())
            .since("INSERT VERSION")
            .toStringFunction(EntityEffect::getId)
            .register();
        AssetStoreRegistry.register(registration, Environment.class, Environment.getAssetMap(),
                "environment", "environment@s")
            .name("Environment")
            .description("Represents the types of environments in the game.", autoGenMessage())
            .since("INSERT VERSION")
            .toStringFunction(Environment::getId)
            .register();
        AssetStoreRegistry.register(registration, DamageCause.class, DamageCause.getAssetMap(),
                "damagecause", "damageCause@s")
            .name("Damage Cause")
            .description("Represents the types of damage that can be caused to entities.", autoGenMessage())
            .since("INSERT VERSION")
            .toStringFunction(DamageCause::getId)
            .register();
        AssetStoreRegistry.register(registration, Interaction.class, Interaction.getAssetMap(),
                "interaction", "interaction@s")
            .name("Interaction")
            .description("Represents the types of interactions that can be performed by entities.", autoGenMessage())
            .since("INSERT VERSION")
            .toStringFunction(Interaction::getId)
            .register();
        AssetStoreRegistry.register(registration, Item.class, Item.getAssetMap(), "item", "item@s")
            .name("Item")
            .description("Represents the types of items in the game.", autoGenMessage())
            .examples("set {_i} to itemstack of Food_Fish_Grilled")
            .since("INSERT VERSION")
            .toStringFunction(Item::getId)
            .register();
        AssetStoreRegistry.register(registration, Projectile.class, Projectile.getAssetMap(), "projectile", "projectile@s")
            .name("Projectile")
            .description("Represents the types of projectiles in the game.", autoGenMessage())
            .since("INSERT VERSION")
            .toStringFunction(Projectile::getId)
            .register();
        AssetStoreRegistry.register(registration, ResourceType.class, ResourceType.getAssetMap(), "resourcetype", "resourceType@s")
            .name("Resource Type")
            .description("Represents the types of resources in the game, such as woods and stones.", autoGenMessage())
            .since("INSERT VERSION")
            .toStringFunction(ResourceType::getId)
            .register();
        AssetStoreRegistry.register(registration, SoundEvent.class, SoundEvent.getAssetMap(), "soundevent", "soundevent@s")
            .name("Sound Event")
            .description("Represents the types of sounds in the game.", autoGenMessage())
            .since("INSERT VERSION")
            .toStringFunction(SoundEvent::getId)
            .register();
        AssetStoreRegistry.register(registration, Weather.class, Weather.getAssetMap(), "weather", "weather@s")
            .name("Weather")
            .description("Represents the types of weather in the game.", autoGenMessage())
            .since("INSERT VERSION")
            .toStringFunction(Weather::getId)
            .register();
    }

    private static String autoGenMessage() {
        String serverVersion = ManifestUtil.getImplementationVersion();
        return "This type is auto-generated with values from Hytale." +
            "\nCurrently generated with Hytale Server version `" + serverVersion + "`.";

    }

}
