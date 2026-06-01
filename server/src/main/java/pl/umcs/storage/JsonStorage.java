package pl.umcs.storage;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonStorage {
    private final ObjectMapper mapper;

    public JsonStorage() {
        this.mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .build();
    }

    public <T> void saveList(File file, List<T> items) throws IOException {
        mapper.writeValue(file, items);
    }

    public <T> List<T> loadList(File file, Class<T> type) throws IOException {
        if (!file.exists()) {
            return new ArrayList<>();
        }
        return mapper.readValue(file,
                mapper.getTypeFactory().constructCollectionType(List.class, type));
    }
}