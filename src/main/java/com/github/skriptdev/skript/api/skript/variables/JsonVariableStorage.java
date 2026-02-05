package com.github.skriptdev.skript.api.skript.variables;

import com.github.skriptdev.skript.api.utils.Utils;
import com.github.skriptdev.skript.plugin.HySk;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.hypixel.hytale.server.core.util.BsonUtil;
import io.github.syst3ms.skriptparser.config.Config.ConfigSection;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.changers.TypeSerializer;
import io.github.syst3ms.skriptparser.variables.VariableStorage;
import io.github.syst3ms.skriptparser.variables.Variables;
import org.bson.BsonBinaryReader;
import org.bson.BsonBinaryWriter;
import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonDouble;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.io.BasicOutputBuffer;
import org.bson.json.JsonWriterSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Instance of {@link VariableStorage} that stores variables in a JSON file.
 */
public class JsonVariableStorage extends VariableStorage {

    public enum Type {
        JSON, BSON
    }

    private File file;
    private Type type = null;
    private BsonDocument bsonDocument;
    private final AtomicInteger changes = new AtomicInteger(0);
    private final int changesToSave = 500;
    private Thread saveThread;

    public JsonVariableStorage(SkriptLogger logger, String name) {
        super(logger, name);
    }

    @Override
    protected boolean load(@NotNull ConfigSection section) {
        String fileType = section.getString("file-type");
        if (fileType == null) {
            Utils.error("No 'file-type' specified for database '" + this.name + "'!", ErrorType.EXCEPTION);
            return false;
        }
        this.type = switch (fileType.toLowerCase(Locale.ROOT)) {
            case "json" -> Type.JSON;
            case "bson" -> Type.BSON;
            default -> {
                Utils.error("Unknown file-type '" + fileType + "' in database '" + this.name + "'", ErrorType.EXCEPTION);
                yield null;
            }
        };
        Utils.log("Database '" + this.name + "' loaded with filetype '" + this.type + "'");
        return this.type != null;
    }

    @Override
    protected void allLoaded() {
        loadVariablesFromFile();
        startFileWatcher();
    }

    private void startFileWatcher() {
        this.saveThread = new Thread( () -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    // Sleep for 5 minutes
                    TimeUnit.MINUTES.sleep(5);

                    // Start checking for variables to save
                    if (this.changes.get() >= this.changesToSave) {
                        try {
                            saveVariables(false);
                            this.changes.set(0);
                        } catch (IOException e) {
                            Utils.error("Failed to save variable file", ErrorType.EXCEPTION);
                            throw new RuntimeException(e);
                        }
                    }
                }
            } catch (InterruptedException e) {
                // Restore interrupt status and exit
                Thread.currentThread().interrupt();
                Utils.error("Variable Save Thread was interrupted, stopping...");
            }

        },"HySkript-Variable-Save-Thread");
        this.saveThread.setDaemon(true);
        this.saveThread.start();
    }

    private void loadVariablesFromFile() {
        Utils.log("Loading variables from file...");
        AtomicBoolean markForBackup = new AtomicBoolean(false);

        try {
            if (this.type == Type.JSON) {
                readJsonFile();
            } else if (this.type == Type.BSON) {
                readBsonFile();
            }
            if (this.bsonDocument == null) {
                this.bsonDocument = new BsonDocument();
            }
            BsonDocument variablesDocument;
            if (this.bsonDocument.containsKey("variables")) {
                variablesDocument = this.bsonDocument.getDocument("variables");
            } else {
                if (!this.bsonDocument.isEmpty() && !this.bsonDocument.containsKey("data")) {
                    // Legacy file format (TODO remove before first release)
                    Utils.warn("Your variables file is outdated. HySkript will create a backup then convert for you.");
                    Files.move(this.file.toPath(), this.file.toPath().resolveSibling(this.file.getName() + ".bak"));
                    variablesDocument = this.bsonDocument.clone();
                    this.bsonDocument.clear();
                    this.bsonDocument.put("variables", variablesDocument);
                } else {
                    variablesDocument = this.bsonDocument.getDocument("variables", new BsonDocument());
                }
            }
            JsonElement jsonElement = BsonUtil.translateBsonToJson(variablesDocument);
            AtomicInteger count = new AtomicInteger();
            AtomicLong start = new AtomicLong(System.currentTimeMillis());
            if (jsonElement instanceof JsonObject jsonObject) {
                jsonObject.entrySet().forEach(entry -> {
                    String name = entry.getKey();
                    JsonObject value = entry.getValue().getAsJsonObject();
                    String type = value.get("type").getAsString();
                    JsonElement jsonValue = value.get("value");
                    if (jsonValue == null) {
                        Utils.error("Skipping variable '%s' due to missing value", name);
                        return;
                    }

                    if (!tryLoadVariable(name, type, jsonValue)) {
                        markForBackup.set(true);
                        variablesDocument.remove(name);
                    }
                    count.getAndIncrement();
                    if (System.currentTimeMillis() - start.get() > 500) {
                        // If it's taking too long, log progress
                        start.set(System.currentTimeMillis());
                        Utils.log(" - Loaded " + count.get() + " variables so far...");
                    }
                });
                Utils.log("Loaded %s variables from file!", count.get());
            }
            if (markForBackup.get()) {
                Utils.warn("Failed to load some variables from file. Creating backup...");
                Files.copy(this.file.toPath(), this.file.toPath().resolveSibling(this.file.getName() + ".bak"));
            }

        } catch (IOException e) {
            Utils.error("Failed to load variables from file", ErrorType.EXCEPTION);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected boolean requiresFile() {
        return true;
    }

    @Override
    protected @Nullable File getFile(@NotNull String fileName) {
        Path resolve = HySk.getInstance().getDataDirectory().resolve(fileName);
        File varFile = resolve.toFile();
        if (!varFile.exists()) {
            try {
                if (varFile.createNewFile()) {
                    Utils.log("Created " + fileName + " file!");
                } else {
                    Utils.error("Failed to create " + fileName + " file!", ErrorType.EXCEPTION);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        this.file = varFile;
        return varFile;
    }

    @Override
    protected boolean save(@NotNull String name, @Nullable String type, @Nullable JsonElement value) {
        BsonDocument myDocument = BsonDocument.parse("{}");

        BsonDocument variablesDocument = this.bsonDocument.getDocument("variables", new BsonDocument());

        if (type != null && value != null) {
            try {
                BsonValue bsonValue = BsonUtil.translateJsonToBson(value);
                myDocument.put("type", new BsonString(type));

                if (bsonValue instanceof BsonDocument doc) {
                    myDocument.put("value", doc);
                } else if (value instanceof JsonPrimitive primitive) {
                    // Bson translate doesn't handle primitives
                    if (primitive.isNumber()) {
                        switch (primitive.getAsNumber()) {
                            case Long l -> myDocument.put("value", new BsonInt64(l));
                            case Integer i -> myDocument.put("value", new BsonInt32(i));
                            case Short s -> myDocument.put("value", new BsonInt32(s.intValue()));
                            case Byte b -> myDocument.put("value", new BsonInt32(b.intValue()));
                            case Double d -> myDocument.put("value", new BsonDouble(d));
                            case Float f -> myDocument.put("value", new BsonDouble(f));
                            case null, default -> myDocument.put("value", bsonValue);
                        }
                    } else if (primitive.isBoolean()) {
                        myDocument.put("value", new BsonBoolean(primitive.getAsBoolean()));
                    } else if (primitive.isString()) {
                        myDocument.put("value", new BsonString(primitive.getAsString()));
                    }
                } else {
                    myDocument.put("value", bsonValue);
                }
            } catch (Exception e) {
                Utils.error("Failed to parse value: " + value);
            }
            variablesDocument.put(name, myDocument);
        } else {
            variablesDocument.remove(name);
        }

        this.bsonDocument.put("variables", variablesDocument);
        this.changes.incrementAndGet();
        return true;
    }

    @Override
    public void close() throws IOException {
        Utils.log("Closing database '" + this.name + "'");
        saveVariables(true);
        this.closed = true;
    }

    private void saveVariables(boolean finalSave) throws IOException {
        if (finalSave) {
            this.saveThread.interrupt();
        }
        try {
            Variables.getLock().lock();
            writeBsonFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            Variables.getLock().unlock();
        }
    }

    @SuppressWarnings("ConstantValue")
    private boolean tryLoadVariable(@NotNull String name, @NotNull String type, @NotNull JsonElement value) {
        if (value == null || type == null) { // These shouldn't be null, but things happen
            Utils.error("value and/or typeName cannot be null");
            return false;
        }
        Object deserialize = deserialize(name, type, value);
        if (deserialize == null) {
            return false;
        }
        Variables.getVariableMap().setVariable(name, deserialize);
        return true;
    }

    @SuppressWarnings("ConstantValue")
    protected Object deserialize(String varName, @NotNull String typeName, @NotNull JsonElement value) {
        if (value == null || typeName == null) {
            Utils.error("value and/or typeName cannot be null");
            return null;
        }
        io.github.syst3ms.skriptparser.types.Type<?> type = TypeManager.getByExactName(typeName).orElse(null);
        if (type == null) {
            Utils.error("Variable '%s' with type '%s' cannot be deserialized. No type registered. This variable will be removed.", varName, typeName);
            return null;
        }
        TypeSerializer<?> serializer = type.getSerializer().orElse(null);
        if (serializer == null) {
            Utils.error("Variable '%s' cannot be deserialized. The type '%s' has no serializer. This variable will be removed.", varName, typeName);
            return null;
        }
        return serializer.deserialize(this.gson, value);
    }

    private void readJsonFile() throws IOException {
        String jsonContent = Files.readString(this.file.toPath());
        if (jsonContent.isBlank()) {
            this.bsonDocument = new BsonDocument();
        } else {
            this.bsonDocument = BsonDocument.parse(jsonContent);
        }
    }

    private void readBsonFile() throws IOException {
        if (!this.file.exists()) {
            throw new FileNotFoundException("File not found: " + this.file.getAbsolutePath());
        }

        byte[] bsonBytes = Files.readAllBytes(this.file.toPath());
        if (bsonBytes.length > 0) {
            try (BsonBinaryReader reader = new BsonBinaryReader(ByteBuffer.wrap(bsonBytes))) {
                BsonDocument doc = new BsonDocumentCodec().decode(reader, DecoderContext.builder().build());
                this.bsonDocument = doc == null ? new BsonDocument() : doc;
            }
        } else {
            this.bsonDocument = new BsonDocument();
        }

    }

    public void writeBsonFile() throws IOException {
        writePluginData();
        if (this.type == Type.JSON) {
            FileWriter fileWriter = new FileWriter(this.file);
            JsonWriterSettings.Builder indent = JsonWriterSettings.builder().indent(true);
            fileWriter.write(this.bsonDocument.toJson(indent.build()));
            fileWriter.close();
        } else if (this.type == Type.BSON) {
            BasicOutputBuffer outputBuffer = new BasicOutputBuffer();
            try (BsonBinaryWriter writer = new BsonBinaryWriter(outputBuffer)) {
                new BsonDocumentCodec().encode(writer, this.bsonDocument, EncoderContext.builder().build());
            }

            byte[] bsonBytes = outputBuffer.toByteArray();
            try (FileOutputStream fos = new FileOutputStream(this.file)) {
                fos.write(bsonBytes);
            }
        }
    }

    private void writePluginData() {
        BsonDocument pluginData = this.bsonDocument.getDocument("data", new BsonDocument());
        pluginData.put("version", new BsonString(HySk.getInstance().getManifest().getVersion().toString()));
        this.bsonDocument.put("data", pluginData);
    }

}
