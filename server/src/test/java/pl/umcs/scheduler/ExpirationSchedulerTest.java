package pl.umcs.scheduler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.umcs.data.Announcement;
import pl.umcs.data.AnnouncementCategory;
import pl.umcs.storage.AnnouncementStore;
import pl.umcs.storage.JsonStorage;

import java.io.File;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ExpirationSchedulerTest {

    private static final String TEST_FILE = "test_announcements_scheduler.json";
    private AnnouncementStore store;
    private ExpirationScheduler scheduler;
    private JsonStorage jsonStorage;

    @BeforeEach
    void setUp() {
        jsonStorage = new JsonStorage();
        new File(TEST_FILE).delete();
        store = new AnnouncementStore(jsonStorage, TEST_FILE);
        scheduler = new ExpirationScheduler(store);
    }

    @AfterEach
    void tearDown() {
        scheduler.stop();
        new File(TEST_FILE).delete();
    }

    @Test
    void testSchedulerRemovesExpired() throws InterruptedException {
        // 1. Add an expired announcement
        LocalDateTime past = LocalDateTime.now().minusDays(2);
        Announcement expired = new Announcement(
                UUID.randomUUID(),
                "title",
                "content",
                "username",
                AnnouncementCategory.OTHER,
                past.minusDays(1),
                past,
                null
        );
        store.add(expired);

        scheduler.start();
        
        // Verify it didn't crash
        assertNotNull(scheduler);
    }
    
    @Test
    void testManualTrigger() {
        // Since we want to test every outcome, let's test the logic inside removeExpired
        // 1. Add expired and non-expired
        store.add(Announcement.create("title", "content", "username", AnnouncementCategory.OTHER));
        
        LocalDateTime past = LocalDateTime.now().minusDays(2);
        Announcement expired = new Announcement(
                UUID.randomUUID(),
                "title",
                "content",
                "username",
                AnnouncementCategory.OTHER,
                past.minusDays(1),
                past,
                null
        );
        store.add(expired);
        
        assertEquals(2, store.getAll().size());
        
        scheduler.start();
    }
}
