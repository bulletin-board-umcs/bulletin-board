package pl.umcs.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonStorage {
    private final ObjectMapper mapper;

    public JsonStorage() {
        this.mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public <T> void saveList(File file, List<T> items) throws IOException {
        mapper.writeValue(file, items);
    }

    public <T> List<T> loadList(File file, Class<T> type) throws IOException {
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }
        return mapper.readValue(file,
                mapper.getTypeFactory().constructCollectionType(List.class, type));
    }

    public ObjectMapper getMapper() {
        return mapper;
    }
}