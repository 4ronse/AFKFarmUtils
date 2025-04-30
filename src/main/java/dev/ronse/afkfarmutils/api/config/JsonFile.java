package dev.ronse.afkfarmutils.api.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonFile {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected transient final Gson gson = new GsonBuilder()
            .registerTypeAdapter(getClass(), (InstanceCreator<JsonFile>) t -> this)
            .setPrettyPrinting()
            .create();

    private transient Path file;

    JsonFile() { }

    public JsonFile(Path file) {
        this.file = file;
    }

    public final void load() {
        if (Files.isReadable(file)) {
            try (Reader reader = Files.newBufferedReader(file)) {
                load(reader);
            } catch (Exception e) {
                LOGGER.error("Failed to load config", e);
            }
        }

        save();
    }

    public final void load(Reader reader) {
        gson.fromJson(reader, getClass());
    }

    public final void save() {
        try {
            Files.createDirectories(file.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(file)) {
                gson.toJson(this, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save config", e);
        }
    }
}
