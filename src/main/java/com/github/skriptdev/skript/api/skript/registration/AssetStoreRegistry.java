package com.github.skriptdev.skript.api.skript.registration;

import com.hypixel.hytale.assetstore.JsonAsset;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration.TypeRegistrar;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Registry related to {@link com.hypixel.hytale.assetstore.AssetStore AssetStores}.
 *
 * @param <R> Asset store type
 */
public class AssetStoreRegistry<R> {

    private final Map<String, R> assetStoreValues = new TreeMap<>();

    /**
     * Create a new {@link TypeRegistrar} with for an {@link com.hypixel.hytale.assetstore.AssetStore AssetStore}.
     * Remember to register it with {@link TypeRegistrar#register()}
     *
     * @param registration The registration to register to
     * @param c            Asset store class
     * @param assetMap     Asset map for the store
     * @param name         Name of the new type
     * @param pattern      Pattern for the type
     * @param <K>          Key type for the asset map
     * @param <T>          Asset type
     * @return New {@link TypeRegistrar}
     */
    public static <K extends String, T extends JsonAsset<K>> TypeRegistrar<T> register(
        SkriptRegistration registration,
        Class<T> c,
        DefaultAssetMap<K, T> assetMap,
        String name,
        String pattern) {

        AssetStoreRegistry<T> store = new AssetStoreRegistry<>();
        assetMap.getAssetMap().forEach((key, value) -> store.assetStoreValues.put(key.toLowerCase(Locale.ROOT), value));
        return registration.newType(c, name, pattern)
            .usage(String.join(", ", store.assetStoreValues.keySet()))
            .supplier(() -> store.assetStoreValues.values().stream().iterator())
            .literalParser(s -> store.assetStoreValues.get(s.toLowerCase(Locale.ROOT).replace(" ", "_")))
            .toStringFunction(Object::toString);
    }

}
