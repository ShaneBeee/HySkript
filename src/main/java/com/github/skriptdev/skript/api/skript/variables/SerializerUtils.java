package com.github.skriptdev.skript.api.skript.variables;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import io.github.syst3ms.skriptparser.types.changers.TypeSerializer;
import org.bson.BsonDocument;
import org.jetbrains.annotations.NotNull;

/**
 * Utilities for serializers.
 */
public class SerializerUtils {

    /**
     * Create a serializer for a {@link Codec Codec}
     *
     * @param codec Codec to create serializer for
     * @param <T>   Type of the codec
     * @return Codec serializer
     */
    public static <T> TypeSerializer<T> getCodecSerializer(BuilderCodec<T> codec) {
        return new TypeSerializer<>() {
            @Override
            public JsonElement serialize(@NotNull Gson gson, @NotNull T value) {
                BsonDocument encode = codec.encode(value, new ExtraInfo());
                String json = encode.toJson();
                return gson.fromJson(json, JsonElement.class);
            }

            @Override
            public T deserialize(@NotNull Gson gson, @NotNull JsonElement element) {
                return codec.decode(BsonDocument.parse(element.toString()), new ExtraInfo());
            }
        };
    }

}
