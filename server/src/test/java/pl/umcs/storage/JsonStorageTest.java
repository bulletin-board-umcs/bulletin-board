package pl.umcs.storage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.umcs.data.Announcement;
import pl.umcs.data.AnnouncementCategory;
import pl.umcs.data.Comment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonStorageTest {

    private static final String TEST_FILE = "test_json_storage.json";
    private JsonStorage storage;
    private File file;

    @BeforeEach
    void setUp() {
        storage = new JsonStorage();
        file = new File(TEST_FILE);
        file.delete();
    }

    @AfterEach
    void tearDown() {
        file.delete();
    }

    @Test
    void testSaveAndLoadList() throws IOException {
        List<Announcement> list = new ArrayList<>();
        Announcement a = Announcement.create("title", "content", "username", AnnouncementCategory.SERVICES);
        a.addComment(Comment.create("username", "content"));
        list.add(a);

        storage.saveList(file, list);

        List<Announcement> loaded = storage.loadList(file, Announcement.class);
        assertEquals(1, loaded.size());
        assertEquals("title", loaded.getFirst().getTitle());
        assertEquals(1, loaded.getFirst().getComments().size());
        assertEquals("content", loaded.getFirst().getComments().getFirst().getContent());
    }

    @Test
    void testLoadNonExistentFile() throws IOException {
        List<Announcement> loaded = storage.loadList(new File("non_existent.json"), Announcement.class);
        assertNotNull(loaded);
        assertTrue(loaded.isEmpty());
    }

    @Test
    void testLoadEmptyFile() throws IOException {
        file.createNewFile();
        List<Announcement> loaded = storage.loadList(file, Announcement.class);
        assertNotNull(loaded);
        assertTrue(loaded.isEmpty());
    }
}
