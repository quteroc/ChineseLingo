package com.chineselingo.persistence.json;

import com.chineselingo.persistence.PersistedDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;

public class JsonFileRepository<T> {

    private final ObjectMapper mapper;
    private final Path file;
    private final Class<T> type;

    public JsonFileRepository(Path file, Class<T> type) {
        this(JsonMapperFactory.get(), file, type);
    }

    public JsonFileRepository( ObjectMapper mapper,  Path file, Class<T> type) {
        this.mapper = mapper;
        this.file = file;
        this.type = type;
    }

    public void save(T object) throws IOException {
        if (object instanceof PersistedDto persisted) {
            persisted.markSavedNow();
        }

        Path parent = file.getParent();

        if (parent == null) {
            parent = Path.of(".").toAbsolutePath().normalize();
        }

        Files.createDirectories(parent);

        Path tmp = Files.createTempFile(
                parent,
                file.getFileName().toString(),
                ".tmp"
        );

        mapper.writeValue(tmp.toFile(), object);

        Files.move(
                tmp,
                file,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
        );
    }

    public T load() throws IOException {
        if (!Files.exists(file) || Files.size(file) == 0) {
            return null;
        }

        try {
            return mapper.readValue(file.toFile(), type);
        } catch (Exception e) {
            return null;
        }
    }
}