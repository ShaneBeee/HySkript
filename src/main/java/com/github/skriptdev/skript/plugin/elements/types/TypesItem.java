package com.github.skriptdev.skript.plugin.elements.types;

import com.github.skriptdev.skript.api.skript.registration.EnumRegistry;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.api.skript.variables.SerializerUtils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.protocol.InventoryActionType;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import io.github.syst3ms.skriptparser.types.changers.TypeSerializer;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.jetbrains.annotations.NotNull;

public class TypesItem {

    static void register(SkriptRegistration registration) {
        // Please keep in alphabetical order
        registration.newType(ItemComponent.class, "itemcomponent", "itemComponent@s")
            .name("Item Component")
            .description("Represents the component of a dropped item.")
            .since("INSERT VERSION")
            .toStringFunction(ic -> String.format("ItemComponent{itemstack=%s}", ic.getItemStack()))
            .serializer(SerializerUtils.getCodecSerializer(ItemComponent.CODEC))
            .register();
        registration.newType(ItemContainer.class, "itemcontainer", "itemContainer@s")
            .name("Item Container")
            .description("Represents an item container within an inventory (such as the armor container).")
            .since("1.0.0")
            .experimental()
            .toStringFunction(ItemContainer::toString)
            .serializer(new TypeSerializer<>() {
                @Override
                public JsonElement serialize(@NotNull Gson gson, @NotNull ItemContainer value) {
                    BsonDocument document;
                    if (value instanceof CombinedItemContainer cic) {
                        document = CombinedItemContainer.CODEC.encode(cic, new ExtraInfo()).asDocument();
                        document.put("type", new BsonString("combined"));
                    } else {
                        document = ItemContainer.CODEC.encode(value, new ExtraInfo()).asDocument();
                        document.put("type", new BsonString("container"));
                    }

                    return gson.fromJson(document.toJson(), JsonElement.class);
                }

                @Override
                public ItemContainer deserialize(@NotNull Gson gson, @NotNull JsonElement element) {
                    BsonDocument parse = BsonDocument.parse(element.toString());
                    String type = parse.getString("type").getValue();
                    if (type == null) return null;

                    if (type.equals("combined")) {
                        return CombinedItemContainer.CODEC.decode(parse, new ExtraInfo());
                    } else if (type.equals("container")) {
                        return ItemContainer.CODEC.decode(parse, new ExtraInfo());
                    }
                    return null;
                }
            })
            .register();
        registration.newType(ItemStack.class, "itemstack", "itemStack@s")
            .name("Item Stack")
            .description("Represents an item in an inventory slot.")
            .examples("set {_i} to itemstack of Food_Fish_Grilled")
            .since("1.0.0")
            .toStringFunction(itemStack -> {
                String quantity = itemStack.getQuantity() == 1 ? "" : itemStack.getQuantity() + " of ";
                return "itemstack of " + quantity + itemStack.getItem().getId();
            })
            .serializer(new TypeSerializer<>() {
                @Override
                public JsonElement serialize(@NotNull Gson gson, @NotNull ItemStack value) {
                    BsonDocument encode = ItemStack.CODEC.encode(value, new ExtraInfo());
                    String json = encode.toJson();
                    return gson.fromJson(json, JsonElement.class);
                }

                @Override
                public ItemStack deserialize(@NotNull Gson gson, @NotNull JsonElement element) {
                    return ItemStack.CODEC.decode(BsonDocument.parse(element.toString()), new ExtraInfo());
                }
            })
            .register();
        registration.newType(Inventory.class, "inventory", "inventor@y@ies")
            .name("Inventory")
            .description("Represents an inventory of an entity or block.")
            .since("1.0.0")
            .toStringFunction(Inventory::toString)
            .serializer(new TypeSerializer<>() {
                @Override
                public JsonElement serialize(@NotNull Gson gson, @NotNull Inventory value) {
                    BsonDocument encode = Inventory.CODEC.encode(value, new ExtraInfo());
                    return gson.fromJson(encode.toJson(), JsonElement.class);
                }

                @Override
                public Inventory deserialize(@NotNull Gson gson, @NotNull JsonElement element) {
                    return Inventory.CODEC.decode(BsonDocument.parse(element.toString()), new ExtraInfo());
                }
            })
            .register();
        EnumRegistry.register(registration, InventoryActionType.class, "inventoryactiontype", "inventoryActionType@s")
            .name("Inventory Action Type")
            .description("Represents the types of actions that can be performed in an inventory.")
            .since("1.0.0")
            .register();
    }

}
