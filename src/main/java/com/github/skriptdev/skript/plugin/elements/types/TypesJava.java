package com.github.skriptdev.skript.plugin.elements.types;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.github.syst3ms.skriptparser.types.changers.TypeSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TypesJava {

    static void register(SkriptRegistration registration) {
        registration.newType(UUID.class, "uuid", "uuid@s")
            .name("UUID")
            .description("Represents a UUID.")
            .examples("set {_uuid} to uuid of {_player}")
            .since("1.0.0")
            .toStringFunction(UUID::toString)
            .serializer(new TypeSerializer<>() {
                @Override
                public JsonElement serialize(@NotNull Gson gson, @NotNull UUID value) {
                    return gson.toJsonTree(value.toString());
                }

                @Override
                public UUID deserialize(@NotNull Gson gson, @NotNull JsonElement element) {
                    return UUID.fromString(element.getAsString());
                }
            })
            .register();
    }

}
