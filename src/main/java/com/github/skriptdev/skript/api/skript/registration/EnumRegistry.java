package com.github.skriptdev.skript.api.skript.registration;

import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration.TypeRegistrar;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Registry related to enums.
 *
 * @param <E> Enum class.
 */
public class EnumRegistry<E extends Enum<E>> {

    private final Map<String, E> values = new TreeMap<>();

    /**
     * Create a new {@link TypeRegistrar} with for an enum.
     * Remember to register it with {@link TypeRegistrar#register()}
     *
     * @param registration Registration to register to
     * @param enumClass    Enum class to register
     * @param name         Name of the new type
     * @param pattern      Pattern for the type
     * @param <T>          Enum class
     * @return New {@link TypeRegistrar}
     */
    public static <T extends Enum<T>> TypeRegistrar<T> register(SkriptRegistration registration,
                                                                Class<T> enumClass,
                                                                String name,
                                                                String pattern) {
        if (enumClass == null || !enumClass.isEnum()) {
            throw new IllegalArgumentException("Cannot register null enum");
        }
        EnumRegistry<T> eEnumRegistry = new EnumRegistry<>();
        for (T e : enumClass.getEnumConstants()) {
            eEnumRegistry.values.put(e.name(), e);
        }
        return registration.newType(enumClass, name, pattern)
            .usage(String.join(", ", eEnumRegistry.values.keySet()))
            .supplier(() -> eEnumRegistry.values.values().iterator())
            .literalParser(s -> eEnumRegistry.values.get(s.toLowerCase(Locale.ROOT).replace(" ", "_")))
            .toStringFunction(Enum::name);
    }

}
