package pl.umcs.storage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.umcs.data.Announcement;
import pl.umcs.data.AnnouncementCategory;
import pl.umcs.data.Comment;

import java.io.File;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AnnouncementStoreTest {

    private static final String TEST_FILE = "test_announcements.json";
    private AnnouncementStore store;
    private JsonStorage jsonStorage;

    @BeforeEach
    void setUp() {
        jsonStorage = new JsonStorage();
        new File(TEST_FILE).delete();
        store = new AnnouncementStore(jsonStorage, TEST_FILE);
    }

    @AfterEach
    void tearDown() {
        new File(TEST_FILE).delete();
    }

    @Test
    void testAddAndGetAnnouncement() {
        Announcement announcement = Announcement.create(
                "title", "content", "username", AnnouncementCategory.OTHER
        );
        store.add(announcement);

        Announcement retrieved = store.getById(announcement.getId());
        assertNotNull(retrieved);
        assertEquals("title", retrieved.getTitle());
        assertEquals("username", retrieved.getAuthorUsername());
    }

    @Test
    void testPersistence() {
        Announcement announcement = Announcement.create(
                "title", "content", "username", AnnouncementCategory.JOBS
        );
        store.add(announcement);

        AnnouncementStore newStore = new AnnouncementStore(jsonStorage, TEST_FILE);
        Announcement retrieved = newStore.getById(announcement.getId());

        assertNotNull(retrieved);
        assertEquals("title", retrieved.getTitle());
        assertEquals(AnnouncementCategory.JOBS, retrieved.getCategory());
    }

    @Test
    void testRemoveById() {
        Announcement announcement = Announcement.create(
                "title", "content", "admin", AnnouncementCategory.OTHER
        );
        store.add(announcement);
        assertTrue(store.removeById(announcement.getId(), "admin", true));

        assertFalse(store.removeById(announcement.getId(), "user", false));

        store.add(announcement);
        assertTrue(store.removeById(announcement.getId(), "user", true));
    }

    @Test
    void testAddComment() {
        Announcement announcement = Announcement.create(
                "title", "content", "username", AnnouncementCategory.OTHER
        );
        store.add(announcement);
        
        Comment comment = Comment.create("username", "content");
        store.addComment(announcement.getId(), comment);
        
        Announcement retrieved = store.getById(announcement.getId());
        assertEquals(1, retrieved.getComments().size());
        assertEquals("content", retrieved.getComments().getFirst().getContent());
    }

    @Test
    void testGetAll() {
        store.add(Announcement.create("title", "content", "username", AnnouncementCategory.OTHER));
        store.add(Announcement.create("title", "content", "username", AnnouncementCategory.OTHER));
        
        List<Announcement> all = store.getAll();
        assertEquals(2, all.size());
    }

    @Test
    void testRemoveExpired() {
        Announcement active = Announcement.create("title", "content", "username", AnnouncementCategory.OTHER);
        store.add(active);

        java.time.LocalDateTime past = java.time.LocalDateTime.now().minusDays(2);
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

        int removedCount = store.removeExpired();

        assertEquals(1, removedCount, "Should remove exactly one expired announcement");
        assertNotNull(store.getById(active.getId()), "Active announcement should still exist");
        assertNull(store.getById(expired.getId()), "Expired announcement should be gone");
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        int threadCount = 10;
        int operationsPerThread = 50;
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threadCount);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        store.add(Announcement.create("title", "content", "username", AnnouncementCategory.OTHER));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertEquals(threadCount * operationsPerThread, store.getAll().size(), "All concurrent additions should be persisted");
    }

    @Test
    void testAbsoluteConcurrentCommentUpdate() throws InterruptedException {
        int threadCount = 50;
        Announcement announcement = Announcement.create("title", "content", "username", AnnouncementCategory.OTHER);
        store.add(announcement);
        UUID id = announcement.getId();

        java.util.concurrent.CyclicBarrier barrier = new java.util.concurrent.CyclicBarrier(threadCount);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(threadCount);
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    barrier.await();
                    store.addComment(id, Comment.create("username", "content"));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        Announcement retrieved = store.getById(id);
        assertEquals(threadCount, retrieved.getComments().size(), "No comments should be lost during concurrent updates");
        
        AnnouncementStore newStore = new AnnouncementStore(jsonStorage, TEST_FILE);
        assertEquals(threadCount, newStore.getById(id).getComments().size(), "Persistence should be consistent after concurrent updates");
    }

    @Test
    void testReadWriteContention() throws InterruptedException {
        int readers = 20;
        int writers = 5;
        int ops = 100;
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(readers + writers);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(readers + writers);
        java.util.concurrent.atomic.AtomicBoolean failed = new java.util.concurrent.atomic.AtomicBoolean(false);

        for (int i = 0; i < writers; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < ops; j++) {
                        store.add(Announcement.create("title", "content", "username", AnnouncementCategory.OTHER));
                    }
                } catch (Exception e) {
                    failed.set(true);
                } finally {
                    latch.countDown();
                }
            });
        }

        for (int i = 0; i < readers; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < ops; j++) {
                        store.getAll();
                    }
                } catch (Exception e) {
                    failed.set(true);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertFalse(failed.get(), "No exceptions should occur during read/write contention");
    }
}
