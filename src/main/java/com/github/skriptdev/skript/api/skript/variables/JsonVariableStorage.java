package com.github.skriptdev.skript.api.skript.variables;

import com.github.skriptdev.skript.api.utils.Utils;
import com.github.skriptdev.skript.plugin.HySk;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.util.BsonUtil;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.variables.VariableStorage;
import io.github.syst3ms.skriptparser.variables.Variables;
import org.bson.BsonBinaryReader;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonReader;
import org.bson.BsonString;
import org.bson.BsonType;
import org.bson.BsonValue;
import org.bson.ByteBufNIO;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.io.BasicOutputBuffer;
import org.bson.io.ByteBufferBsonInput;
import org.bson.json.JsonWriterSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
    protected boolean load(@NotNull FileSection section) {
        String fileType = getConfigurationValue(section, "file-type");
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
        this.logger.error("Database '" + this.name + "' loaded with filetype '" + this.type + "'", ErrorType.EXCEPTION);
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
                    this.logger.info("Saved " + this.changes.get() + " changes to '" + this.file.getName() + "'"); // TODO REMOVE (debug)
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

                    this.logger.debug("Loading variable '" + name + "' of type '" + type + "' from file. With data '" + jsonValue.toString() + "'");
                    loadVariable(name, type, jsonValue);
                });
            }

        } catch (IOException e) {
            this.logger.error("Failed to load variables from file", ErrorType.EXCEPTION);
            throw new RuntimeException(e);
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

        try (FileInputStream fis = new FileInputStream(file); FileChannel fc = fis.getChannel()) {
            // Read the entire file into a ByteBuffer
            ByteBuffer buffer = ByteBuffer.allocate((int) fc.size());
            fc.read(buffer);
            buffer.flip();

            try (ByteBufferBsonInput bib = new ByteBufferBsonInput(new ByteBufNIO(buffer))) {
                // Use BsonBinaryReader to read the BSON data
                BsonReader reader = new BsonBinaryReader(bib);

                // Use a BsonDocumentCodec to decode the BSON into a BsonDocument object
                BsonDocumentCodec codec = new BsonDocumentCodec();
                DecoderContext decoderContext = DecoderContext.builder().build();
                BsonType type = reader.getCurrentBsonType();
                if (type == null || type == BsonType.NULL || type == BsonType.END_OF_DOCUMENT) {
                    this.bsonDocument = new BsonDocument();
                } else {
                    this.bsonDocument = codec.decode(reader, decoderContext);
                }
            }
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
            writeBsonDocumentToFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            Variables.getLock().unlock();
        }
    }

    public void writeBsonDocumentToFile() throws IOException {
        if (this.type == Type.JSON) {
            FileWriter fileWriter = new FileWriter(file);
            JsonWriterSettings.Builder indent = JsonWriterSettings.builder().indent(true);
            fileWriter.write(this.bsonDocument.toJson(indent.build()));
            fileWriter.close();
        } else if (this.type == Type.BSON) {
            try (BasicOutputBuffer outputBuffer = new BasicOutputBuffer(); FileOutputStream fos = new FileOutputStream(this.file)) {

                BsonBinaryWriter writer = new BsonBinaryWriter(outputBuffer);
                // Use BsonDocumentCodec to encode the BsonDocument to the writer
                new BsonDocumentCodec().encode(writer, this.bsonDocument, EncoderContext.builder().isEncodingCollectibleDocument(true).build());
                writer.close();

                // Write the byte array to the file
                fos.write(outputBuffer.toByteArray());
            }
        }
    }

}
