package com.github.skriptdev.skript.api.skript.registration;

import com.github.skriptdev.skript.api.utils.Utils;
import com.github.skriptdev.skript.plugin.Skript;
import com.hypixel.hytale.assetstore.JsonAsset;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import io.github.syst3ms.skriptparser.docs.Documentation;
import io.github.syst3ms.skriptparser.log.LogEntry;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.changers.Arithmetic;
import io.github.syst3ms.skriptparser.types.changers.Changer;
import io.github.syst3ms.skriptparser.types.changers.TypeSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An extension of {@link io.github.syst3ms.skriptparser.registration.SkriptRegistration} with additional features.
 */
public class SkriptRegistration extends io.github.syst3ms.skriptparser.registration.SkriptRegistration {

    private final Skript skript;

    public SkriptRegistration(Skript registerer) {
        super(registerer, new SkriptLogger(true));
        this.skript = registerer;
    }

    public Skript getSkript() {
        return this.skript;
    }

    @Override
    public List<LogEntry> register() {
        for (LogEntry logEntry : super.register()) {
            Utils.log(null, logEntry);
        }
        return List.of();
    }

    /**
     * Register a new asset store type.
     *
     * @param assetClass Class of AssetStore
     * @param assetMap   AssetMap for the AssetStore
     * @param name       Name of the AssetStore
     * @param pattern    Pattern for the AssetStore
     * @param <C>        Type of Asset
     * @param <K>        Type of Asset ID
     * @return AssetStoreRegistrar for further configuration
     */
    public <C extends JsonAsset<K>, K extends String> AssetStoreRegistrar<C, K> newAssetStoreType(Class<C> assetClass,
                                                                                                  DefaultAssetMap<K, C> assetMap,
                                                                                                  String name,
                                                                                                  String pattern) {
        return new AssetStoreRegistrar<>(assetClass, assetMap, name, pattern);
    }

    public class AssetStoreRegistrar<C extends JsonAsset<K>, K extends String> {
        private final Class<C> assetClass;
        private final String baseName;
        private final String pattern;
        private Function<? super C, String> toStringFunction;
        private Function<? super C, String> toVariableNameFunction;
        private final Function<String, ? extends C> literalParser;
        @Nullable
        private Changer<? super C> defaultChanger;
        @Nullable
        private Arithmetic<C, ?> arithmetic;
        @Nullable
        private TypeSerializer<C> serializer;
        private final Supplier<Iterator<C>> supplier;
        private final Documentation documentation = new Documentation();
        @SuppressWarnings("FieldCanBeLocal")
        private final DefaultAssetMap<K, C> assetMap; // Store for later use?!?!
        private final Map<String, C> assetStoreValues = new TreeMap<>();

        public AssetStoreRegistrar(Class<C> assetClass, DefaultAssetMap<K, C> assetMap, String baseName, String pattern) {
            this.assetClass = assetClass;
            this.baseName = baseName;
            this.pattern = pattern;
            this.assetMap = assetMap;
            assetMap.getAssetMap().forEach((key, value) -> this.assetStoreValues.put(key.toLowerCase(Locale.ROOT), value));
            this.supplier = () -> assetStoreValues.values().stream().iterator();
            this.literalParser = s -> this.assetStoreValues.get(s.toLowerCase(Locale.ROOT).replace(" ", "_"));
            this.toStringFunction = JsonAsset::getId;
        }

        /**
         * @param toStringFunction a function converting an instance of the type to a String
         * @return the registrar
         */
        public AssetStoreRegistrar<C, K> toStringFunction(Function<? super C, String> toStringFunction) {
            this.toStringFunction = c -> c == null ? TypeManager.NULL_REPRESENTATION : toStringFunction.apply(c);
            return this;
        }

        public AssetStoreRegistrar<C, K> toVariableNameFunction(Function<? super C, String> toVariableNameFunction) {
            this.toVariableNameFunction = toVariableNameFunction;
            return this;
        }

        /**
         * @param defaultChanger a default {@link Changer} for this type
         * @return the registrar
         */
        public AssetStoreRegistrar<C, K> defaultChanger(Changer<? super C> defaultChanger) {
            this.defaultChanger = defaultChanger;
            return this;
        }

        /**
         * @param arithmetic a default {@link Arithmetic} for this type
         * @return the registrar
         */
        public <R> AssetStoreRegistrar<C, K> arithmetic(Arithmetic<C, R> arithmetic) {
            this.arithmetic = arithmetic;
            return this;
        }

        /**
         * @param serializer add a type serializer that allows the type to be saved to databases.
         * @return the registrar
         */
        public AssetStoreRegistrar<C, K> serializer(TypeSerializer<C> serializer) {
            this.serializer = serializer;
            return this;
        }

        public <R> AssetStoreRegistrar<C, K> noDoc() {
            this.documentation.noDoc();
            return this;
        }

        public <R> AssetStoreRegistrar<C, K> experimental() {
            this.documentation.experimental();
            return this;
        }

        public <R> AssetStoreRegistrar<C, K> experimental(String message) {
            this.documentation.experimental(message);
            return this;
        }

        public <R> AssetStoreRegistrar<C, K> name(String name) {
            this.documentation.setName(name);
            return this;
        }

        public <R> AssetStoreRegistrar<C, K> description(String... description) {
            this.documentation.setDescription(description);
            return this;
        }

        public <R> AssetStoreRegistrar<C, K> examples(String... examples) {
            this.documentation.setExamples(examples);
            return this;
        }

        public <R> AssetStoreRegistrar<C, K> usage(String usage) {
            this.documentation.setUsage(usage);
            return this;
        }

        public <R> AssetStoreRegistrar<C, K> since(String since) {
            this.documentation.setSince(since);
            return this;
        }

        /**
         * Adds this type to the list of currently registered syntaxes
         */
        public void register() {
            AssetStoreType<C, K> assetStoreType = new AssetStoreType<>(assetClass, baseName, pattern, literalParser,
                toStringFunction, toVariableNameFunction,
                defaultChanger, arithmetic, documentation, serializer, this.supplier);
            newTypes = true;
            types.add(assetStoreType);
        }
    }

    public static class AssetStoreType<C extends JsonAsset<K>, K extends String> extends Type<C> {

        public AssetStoreType(Class<C> c, String baseName, String pattern, Function<String, ? extends C> literalParser,
                              Function<? super C, String> toStringFunction,
                              Function<? super C, String> toVariableNameFunction, Changer<? super C> defaultChanger,
                              Arithmetic<C, ?> arithmetic, Documentation documentation, TypeSerializer<C> serializer,
                              Supplier<Iterator<C>> supplier) {
            super(c, baseName, pattern, literalParser, toStringFunction, toVariableNameFunction, defaultChanger,
                arithmetic, documentation, serializer, supplier);
        }
    }

    /**
     * Create a new Enum type.
     *
     * @param enumClass Class of the Enum
     * @param baseName  Name of the Enum type
     * @param pattern   Pattern for the Enum type
     * @param <E>       Type parameter for the Enum
     * @return EnumRegistrar for configuring the Enum type
     */
    public <E extends Enum<E>> EnumRegistrar<E> newEnumType(Class<E> enumClass, String baseName, String pattern) {
        return new EnumRegistrar<>(enumClass, baseName, pattern);
    }

    public class EnumRegistrar<E extends Enum<E>> {
        private final Class<E> enumClass;
        private final String baseName;
        private final String pattern;

        private Function<? super E, String> toStringFunction;
        private Function<? super E, String> toVariableNameFunction;
        private final Function<String, ? extends E> literalParser;
        @Nullable
        private Changer<? super E> defaultChanger;
        @Nullable
        private Arithmetic<E, ?> arithmetic;
        @Nullable
        private TypeSerializer<E> serializer;
        private final Supplier<Iterator<E>> supplier;
        private final Documentation documentation = new Documentation();
        private final Map<String, E> values = new TreeMap<>();

        public EnumRegistrar(Class<E> enumClass, String name, String pattern) {
            for (E e : enumClass.getEnumConstants()) {
                this.values.put(e.name().toLowerCase(Locale.ROOT), e);
            }
            this.enumClass = enumClass;
            this.baseName = name;
            this.pattern = pattern;
            this.documentation.setUsage(String.join(", ", this.values.keySet()));
            this.supplier = () -> this.values.values().iterator();
            this.literalParser = s -> this.values.get(s.toLowerCase(Locale.ROOT).replace(" ", "_"));
            this.toStringFunction = e -> e.name().toLowerCase(Locale.ROOT);
        }


        /**
         * @param toStringFunction a function converting an instance of the type to a String
         * @return the registrar
         */
        public EnumRegistrar<E> toStringFunction(Function<? super E, String> toStringFunction) {
            this.toStringFunction = c -> c == null ? TypeManager.NULL_REPRESENTATION : toStringFunction.apply(c);
            return this;
        }

        public EnumRegistrar<E> toVariableNameFunction(Function<? super E, String> toVariableNameFunction) {
            this.toVariableNameFunction = toVariableNameFunction;
            return this;
        }

        /**
         * @param defaultChanger a default {@link Changer} for this type
         * @return the registrar
         */
        public EnumRegistrar<E> defaultChanger(Changer<? super E> defaultChanger) {
            this.defaultChanger = defaultChanger;
            return this;
        }

        /**
         * @param arithmetic a default {@link Arithmetic} for this type
         * @return the registrar
         */
        public <R> EnumRegistrar<E> arithmetic(Arithmetic<E, R> arithmetic) {
            this.arithmetic = arithmetic;
            return this;
        }

        /**
         * @param serializer add a type serializer that allows the type to be saved to databases.
         * @return the registrar
         */
        public EnumRegistrar<E> serializer(TypeSerializer<E> serializer) {
            this.serializer = serializer;
            return this;
        }

        /**
         * Prevent docs from being printed.
         *
         * @return the registrar
         */
        public <R> EnumRegistrar<E> noDoc() {
            this.documentation.noDoc();
            return this;
        }

        public <R> EnumRegistrar<E> experimental() {
            this.documentation.experimental();
            return this;
        }

        public <R> EnumRegistrar<E> experimental(String message) {
            this.documentation.experimental(message);
            return this;
        }

        public <R> EnumRegistrar<E> name(String name) {
            this.documentation.setName(name);
            return this;
        }

        public <R> EnumRegistrar<E> description(String... description) {
            this.documentation.setDescription(description);
            return this;
        }

        public <R> EnumRegistrar<E> examples(String... examples) {
            this.documentation.setExamples(examples);
            return this;
        }

        public <R> EnumRegistrar<E> usage(String usage) {
            this.documentation.setUsage(usage);
            return this;
        }

        public <R> EnumRegistrar<E> since(String since) {
            this.documentation.setSince(since);
            return this;
        }

        /**
         * Adds this type to the list of currently registered syntaxes
         */
        public void register() {
            EnumType<E> enumType = new EnumType<>(this.enumClass, baseName, pattern, literalParser,
                toStringFunction, toVariableNameFunction,
                defaultChanger, arithmetic, documentation, serializer, this.supplier);
            newTypes = true;
            types.add(enumType);
        }
    }

    public static class EnumType<E extends Enum<E>> extends Type<E> {
        public EnumType(Class<E> c, String baseName, String pattern, Function<String, ? extends E> literalParser,
                        Function<? super E, String> toStringFunction, Function<? super E, String> toVariableNameFunction,
                        Changer<? super E> defaultChanger,
                        Arithmetic<E, ?> arithmetic, Documentation documentation, TypeSerializer<E> serializer,
                        Supplier<Iterator<E>> supplier) {
            super(c, baseName, pattern, literalParser, toStringFunction, toVariableNameFunction, defaultChanger,
                arithmetic, documentation, serializer, supplier);
        }
    }

}
