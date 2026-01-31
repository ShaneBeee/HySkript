package com.github.skriptdev.skript.api.skript.variables;

import com.github.skriptdev.skript.api.utils.Utils;
import com.github.skriptdev.skript.plugin.HySk;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.util.BsonUtil;
import io.github.syst3ms.skriptparser.config.Config.ConfigSection;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Instance of {@link VariableStorage} that stores variables in a JSON file.
 */
public class JsonVariableStorage extends VariableStorage {

    public enum Type {
        JSON, BSON;
    }

    private File file;
    private Type type = null;
    private BsonDocument bsonDocument;
    private final AtomicInteger changes = new AtomicInteger(0);
    private final int changesToSave = 500;
    ScheduledFuture<?> schedule;
    private final SkriptLogger logger;

    public JsonVariableStorage(SkriptLogger logger, String name) {
        super(logger, name);
        this.logger = logger;
    }

    @Override
    protected boolean load(@NotNull ConfigSection section) {
        String fileType = section.getString("file-type");
        if (fileType == null) {
            this.logger.error("No 'file-type' specified for database '" + this.name + "'!", ErrorType.EXCEPTION);
            return false;
        }
        this.type = switch (fileType.toLowerCase(Locale.ROOT)) {
            case "json" -> Type.JSON;
            case "bson" -> Type.BSON;
            default -> {
                this.logger.error("Unknown file-type '" + fileType + "' in database '" + this.name + "'", ErrorType.EXCEPTION);
                yield null;
            }
        };
        this.logger.info("Database '" + this.name + "' loaded with filetype '" + this.type + "'");
        return this.type != null;
    }

    @Override
    protected void allLoaded() {
        loadVariablesFromFile();
        startFileWatcher();
    }

    private void startFileWatcher() {
        this.schedule = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
            if (this.changes.get() >= this.changesToSave) {
                try {
                    saveVariables(false);
                    this.changes.set(0);
                } catch (IOException e) {
                    this.logger.error("Failed to save variable file", ErrorType.EXCEPTION);
                    throw new RuntimeException(e);
                }
            }
        }, 5, 5, TimeUnit.MINUTES);
    }

    private void loadVariablesFromFile() {
        this.logger.info("Loading variables from file...");

        try {
            if (this.type == Type.JSON) {
                readJsonFile();
            } else if (this.type == Type.BSON) {
                readBsonFile();
            }
            if (this.bsonDocument == null) {
                this.bsonDocument = new BsonDocument();
            }
            JsonElement jsonElement = BsonUtil.translateBsonToJson(this.bsonDocument);
            if (jsonElement instanceof JsonObject jsonObject) {
                jsonObject.entrySet().forEach(entry -> {
                    String name = entry.getKey();
                    JsonObject value = entry.getValue().getAsJsonObject();
                    String type = value.get("type").getAsString();
                    JsonElement jsonValue = value.get("value");
                    if (jsonValue == null) {
                        this.logger.error("Skipping variable '" + name + "' due to missing value", ErrorType.STRUCTURE_ERROR);
                        return;
                    }

                    this.logger.debug("Loading variable '" + name + "' of type '" + type + "' from file. With data '" + jsonValue.toString() + "'");
                    loadVariable(name, type, jsonValue);
                });
            }

        } catch (IOException e) {
            this.logger.error("Failed to load variables from file", ErrorType.EXCEPTION);
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
                    this.logger.info("Created " + fileName + " file!");
                } else {
                    this.logger.error("Failed to create " + fileName + " file!", ErrorType.EXCEPTION);
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
        } else {
            this.bsonDocument.remove(name);
        }

        this.bsonDocument.put(name, myDocument);
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
            this.schedule.cancel(true);
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

}
