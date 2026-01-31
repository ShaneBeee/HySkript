package com.github.skriptdev.skript.plugin.elements.types;

import com.github.skriptdev.skript.api.skript.registration.AssetStoreRegistry;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.builtin.hytalegenerator.assets.biomes.BiomeAsset;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.common.util.java.ManifestUtil;
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
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import io.github.syst3ms.skriptparser.types.changers.TypeSerializer;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.jetbrains.annotations.NotNull;

public class TypesAssetStore {

    static void register(SkriptRegistration registration) {
        AssetStoreRegistry.register(registration, BiomeAsset.class, BiomeAsset.getAssetStore().getAssetMap(), "biome", "biome@s")
            .name("Biome")
            .description("Represents the types of biomes in the game.", autoGenMessage())
            .since("1.0.0")
            .toStringFunction(BiomeAsset::getId)
            .register();
        AssetStoreRegistry.register(registration, BlockGroup.class, AssetRegistry.getAssetStore(BlockGroup.class).getAssetMap(),
                "blockgroup", "blockgroup@s")
            .name("Block Group")
            .description("Represents the groups of blocks in the game.", autoGenMessage())
            .since("1.0.0")
            .toStringFunction(BlockGroup::getId)
            .register();
        AssetStoreRegistry.register(registration, CraftingRecipe.class, CraftingRecipe.getAssetMap(), "craftingrecipe", "craftingrecipe@s")
            .name("Crafting Recipe")
            .description("Represents the crafting recipes in the game.", autoGenMessage())
            .since("1.0.0")
            .toStringFunction(CraftingRecipe::getId)
            .register();
        AssetStoreRegistry.register(registration, BlockType.class, BlockType.getAssetMap(), "blocktype", "blockType@s")
            .name("BlockType")
            .description("Represents the types of blocks in the game.", autoGenMessage())
            .examples("set {_block} to blocktype of block at player")
            .since("1.0.0")
            .toStringFunction(BlockType::getId)
            .register();
        AssetStoreRegistry.register(registration, Fluid.class, Fluid.getAssetMap(), "fluid", "fluid@s")
            .name("Fluid")
            .description("Represents the types of fluids in the game.", autoGenMessage())
            .since("1.0.0")
            .toStringFunction(Fluid::getId)
            .register();
        AssetStoreRegistry.register(registration, EntityEffect.class, EntityEffect.getAssetMap(),
                "entityeffect", "entityEffect@s")
            .name("Entity Effect")
            .description("Represents the types of effects that can be applied to entities.", autoGenMessage())
            .since("1.0.0")
            .toStringFunction(EntityEffect::getId)
            .register();
        AssetStoreRegistry.register(registration, EntityStatType.class, EntityStatType.getAssetMap(),
                "entitystattype", "entityStatType@s")
            .name("Entity Stat Type")
            .description("Represents the types of stats that can be applied to entities.", autoGenMessage())
            .since("1.0.0")
            .register();
        AssetStoreRegistry.register(registration, Environment.class, Environment.getAssetMap(),
                "environment", "environment@s")
            .name("Environment")
            .description("Represents the types of environments in the game.", autoGenMessage())
            .since("1.0.0")
            .toStringFunction(Environment::getId)
            .register();
        AssetStoreRegistry.register(registration, DamageCause.class, DamageCause.getAssetMap(),
                "damagecause", "damageCause@s")
            .name("Damage Cause")
            .description("Represents the types of damage that can be caused to entities.", autoGenMessage())
            .since("1.0.0")
            .toStringFunction(DamageCause::getId)
            .register();
        AssetStoreRegistry.register(registration, Interaction.class, Interaction.getAssetMap(),
                "interaction", "interaction@s")
            .name("Interaction")
            .description("Represents the types of interactions that can be performed by entities.", autoGenMessage())
            .since("1.0.0")
            .toStringFunction(Interaction::getId)
            .register();
        AssetStoreRegistry.register(registration, Item.class, Item.getAssetMap(), "item", "item@s")
            .name("Item")
            .description("Represents the types of items in the game.", autoGenMessage())
            .examples("set {_i} to itemstack of Food_Fish_Grilled")
            .since("1.0.0")
            .toStringFunction(Item::getId)
            .serializer(new TypeSerializer<>() {
                @Override
                public JsonElement serialize(@NotNull Gson gson, @NotNull Item value) {
                    BsonValue encode = Item.CODEC.encode(value, new ExtraInfo());
                    return gson.fromJson(encode.asDocument().toJson(), JsonElement.class);
                }

                @Override
                public Item deserialize(@NotNull Gson gson, @NotNull JsonElement element) {
                    return Item.CODEC.decode(BsonDocument.parse(element.toString()), new ExtraInfo());
                }
            })
            .register();
        AssetStoreRegistry.register(registration, Projectile.class, Projectile.getAssetMap(), "projectile", "projectile@s")
            .name("Projectile")
            .description("Represents the types of projectiles in the game.", autoGenMessage())
            .since("1.0.0")
            .toStringFunction(Projectile::getId)
            .register();
        AssetStoreRegistry.register(registration, ResourceType.class, ResourceType.getAssetMap(), "resourcetype", "resourceType@s")
            .name("Resource Type")
            .description("Represents the types of resources in the game, such as woods and stones.", autoGenMessage())
            .since("1.0.0")
            .toStringFunction(ResourceType::getId)
            .register();
        AssetStoreRegistry.register(registration, SoundEvent.class, SoundEvent.getAssetMap(), "soundevent", "soundevent@s")
            .name("Sound Event")
            .description("Represents the types of sounds in the game.", autoGenMessage())
            .since("1.0.0")
            .toStringFunction(SoundEvent::getId)
            .register();
        AssetStoreRegistry.register(registration, Weather.class, Weather.getAssetMap(), "weather", "weather@s")
            .name("Weather")
            .description("Represents the types of weather in the game.", autoGenMessage())
            .since("1.0.0")
            .toStringFunction(Weather::getId)
            .register();
    }

    private static String autoGenMessage() {
        String serverVersion = ManifestUtil.getImplementationVersion();
        String patchline = ManifestUtil.getPatchline();
        return "This type is auto-generated with values from Hytale." +
            "\nCurrently generated with Hytale Server version `" + serverVersion + " (" + patchline + ")`.";
    }

}
