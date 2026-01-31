package com.github.skriptdev.skript.plugin.elements.types;

import com.github.skriptdev.skript.api.skript.command.ArgUtils;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.receiver.IMessageReceiver;
import io.github.syst3ms.skriptparser.types.changers.TypeSerializer;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.jetbrains.annotations.NotNull;

public class TypesServer {

    static void register(SkriptRegistration registration) {
        registration.newType(ArgumentType.class, "argumenttype", "argumentType@s")
            .name("Argument Type")
            .description("Represents the types of arguments that can be used in commands.")
            .usage(ArgUtils.getTypeUsage())
            .since("1.0.0")
            .register();
        registration.newType(CommandSender.class, "commandsender", "commandSender@s")
            .name("Command Sender")
            .description("Represents a command sender such as a player or the console.")
            .since("1.0.0")
            .toStringFunction(CommandSender::getDisplayName)
            .register();
        registration.newType(Damage.class, "damage", "damage@s")
            .name("Damage")
            .description("Represents information about damage dealt to an entity.")
            .since("1.0.0")
            .register();
        registration.newType(Damage.Source.class, "damagesource", "damageSource@s")
            .name("Damage Source")
            .description("Represents the source of damage when an entity is damaged/killed.")
            .since("1.0.0")
            .register();
        registration.newType(IMessageReceiver.class, "messagereceiver", "messageReceiver@s")
            .name("Message Receiver")
            .description("Represents a receiver of messages such as a player or the console.")
            .since("1.0.0")
            .register();
        registration.newType(Message.class, "message", "message@s")
            .name("Message")
            .description("Represents a stylized message sent to a message receiver.")
            .since("1.0.0")
            .serializer(new TypeSerializer<>() {
                @Override
                public JsonElement serialize(@NotNull Gson gson, @NotNull Message value) {
                    BsonValue encode = Message.CODEC.encode(value, new ExtraInfo());
                    return gson.fromJson(encode.asDocument().toJson(), JsonElement.class);
                }

                @Override
                public Message deserialize(@NotNull Gson gson, @NotNull JsonElement element) {
                    BsonDocument decode = BsonDocument.parse(element.toString());
                    return Message.CODEC.decode(decode, new ExtraInfo());
                }
            })
            .register();
        registration.newType(HytaleServer.class, "server", "server@s")
            .name("Server")
            .description("Represents the Hytale server.")
            .since("1.0.0")
            .register();
        registration.newType(Vector3f.class, "vector3f", "vector3f@s")
            .name("Vector3f")
            .description("Represents a vector in 3D space using floats.",
                "Often used for the rotation of entities in a world.")
            .since("1.0.0")
            .serializer(new TypeSerializer<>() {
                @Override
                public JsonElement serialize(@NotNull Gson gson, @NotNull Vector3f value) {
                    BsonDocument encode = Vector3f.CODEC.encode(value, new ExtraInfo());
                    return gson.fromJson(encode.toJson(), JsonElement.class);
                }

                @Override
                public Vector3f deserialize(@NotNull Gson gson, @NotNull JsonElement element) {
                    BsonDocument decode = BsonDocument.parse(element.toString());
                    return Vector3f.CODEC.decode(decode, new ExtraInfo());
                }
            })
            .register();
        registration.newType(Vector3d.class, "vector3d", "vector3d@s")
            .name("Vector3d")
            .description("Represents a vector in 3D space using doubles.",
                "Often used for the position of entities in a world.")
            .since("1.0.0")
            .serializer(new TypeSerializer<>() {
                @Override
                public JsonElement serialize(@NotNull Gson gson, @NotNull Vector3d value) {
                    BsonDocument encode = Vector3d.CODEC.encode(value, new ExtraInfo());
                    return gson.fromJson(encode.toJson(), JsonElement.class);
                }

                @Override
                public Vector3d deserialize(@NotNull Gson gson, @NotNull JsonElement element) {
                    BsonDocument decode = BsonDocument.parse(element.toString());
                    return Vector3d.CODEC.decode(decode, new ExtraInfo());
                }
            })
            .register();
        registration.newType(Vector3i.class, "vector3i", "vector3i@s")
            .name("Vector3i")
            .description("Represents a vector in 3D space using integers.",
                "Often used for the position of blocks in a world.")
            .since("1.0.0")
            .serializer(new TypeSerializer<>() {
                @Override
                public JsonElement serialize(@NotNull Gson gson, @NotNull Vector3i value) {
                    BsonDocument encode = Vector3i.CODEC.encode(value, new ExtraInfo());
                    return gson.fromJson(encode.toJson(), JsonElement.class);
                }

                @Override
                public Vector3i deserialize(@NotNull Gson gson, @NotNull JsonElement element) {
                    BsonDocument decode = BsonDocument.parse(element.toString());
                    return Vector3i.CODEC.decode(decode, new ExtraInfo());
                }
            })
            .register();
        registration.newType(Location.class, "location", "location@s")
            .name("Location")
            .description("Represents a location in a world.",
                "A location contains a world, a position (vector3d) and a rotation (vector3f).")
            .serializer(new TypeSerializer<>() {
                @Override
                public JsonElement serialize(@NotNull Gson gson, @NotNull Location location) {
                    BsonDocument bsonDocument = new BsonDocument();
                    String world = location.getWorld();
                    if (world != null) {
                        // This shouldn't be null, but let's be safe
                        bsonDocument.put("world", new BsonString(world));
                    }
                    bsonDocument.put("position", Vector3d.CODEC.encode(location.getPosition(), new ExtraInfo()));
                    bsonDocument.put("rotation", Vector3f.CODEC.encode(location.getRotation(), new ExtraInfo()));

                    return gson.fromJson(bsonDocument.toJson(), JsonElement.class);
                }

                @Override
                public Location deserialize(@NotNull Gson gson, @NotNull JsonElement element) {
                    BsonDocument decode = BsonDocument.parse(element.toString());
                    Vector3d position = Vector3d.CODEC.decode(decode.get("position"), new ExtraInfo());
                    Vector3f rotation = Vector3f.CODEC.decode(decode.get("rotation"), new ExtraInfo());

                    String world = null;
                    if (decode.containsKey("world")) {
                        // This shouldn't be null, but let's be safe
                        world = decode.getString("world").getValue();
                    }
                    assert position != null;
                    assert rotation != null;
                    return new Location(world, position, rotation);
                }
            })
            .since("1.0.0")
            .register();
    }

}
