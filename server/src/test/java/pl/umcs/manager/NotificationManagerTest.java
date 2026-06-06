package pl.umcs.manager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.umcs.AnnouncementListener;
import pl.umcs.data.Announcement;
import pl.umcs.data.AnnouncementCategory;
import pl.umcs.storage.JsonStorage;

import java.io.File;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class NotificationManagerTest {

    private static final String TEST_FILE = "test_subscriptions.json";
    private NotificationManager manager;
    private JsonStorage jsonStorage;

    @BeforeEach
    void setUp() {
        jsonStorage = new JsonStorage();
        new File(TEST_FILE).delete();
        manager = new NotificationManager(jsonStorage, TEST_FILE);
    }

    @AfterEach
    void tearDown() {
        new File(TEST_FILE).delete();
    }

    private static class TestListener implements AnnouncementListener {
        private final AtomicReference<Announcement> received = new AtomicReference<>();

        @Override
        public void onNewAnnouncement(Announcement announcement) throws RemoteException {
            received.set(announcement);
        }

        public Announcement getReceived() {
            return received.get();
        }
    }

    @Test
    void testSubscribeAndNotify() throws RemoteException {
        TestListener listener = new TestListener();
        manager.subscribe("username", AnnouncementCategory.ELECTRONICS, listener);

        Announcement announcement = Announcement.create(
                "title", "content", "username", AnnouncementCategory.ELECTRONICS
        );
        manager.notify(announcement);

        Announcement received = listener.getReceived();
        assertNotNull(received);
        assertEquals("title", received.getTitle());
    }

    @Test
    void testUnsubscribe() throws RemoteException {
        TestListener listener = new TestListener();
        manager.subscribe("username", AnnouncementCategory.ELECTRONICS, listener);
        manager.unsubscribe("username", AnnouncementCategory.ELECTRONICS, listener);

        Announcement announcement = Announcement.create(
                "title", "content", "username", AnnouncementCategory.ELECTRONICS
        );
        manager.notify(announcement);

        assertNull(listener.getReceived());
    }

    @Test
    void testPersistentSubscriptions() {
        TestListener listener = new TestListener();
        manager.subscribe("username", AnnouncementCategory.FASHION, listener);

        NotificationManager newManager = new NotificationManager(jsonStorage, TEST_FILE);
        List<AnnouncementCategory> categories = newManager.getSubscribedCategories("username");
        
        assertEquals(1, categories.size());
        assertEquals(AnnouncementCategory.FASHION, categories.getFirst());
    }

    @Test
    void testNotifyWrongCategory() throws RemoteException {
        TestListener listener = new TestListener();
        manager.subscribe("username", AnnouncementCategory.ELECTRONICS, listener);

        Announcement announcement = Announcement.create(
                "title", "content", "username", AnnouncementCategory.ANIMALS
        );
        manager.notify(announcement);

        assertNull(listener.getReceived());
    }

    @Test
    void testStaleListenerCleanup() throws RemoteException {
        AnnouncementListener staleListener = new AnnouncementListener() {
            @Override
            public void onNewAnnouncement(Announcement announcement) throws RemoteException {
                throw new RemoteException("Simulated connection failure");
            }
        };

        manager.subscribe("username", AnnouncementCategory.OTHER, staleListener);
        
        Announcement announcement = Announcement.create(
                "title", "content", "username", AnnouncementCategory.OTHER
        );
        
        manager.notify(announcement);

        TestListener validListener = new TestListener();
        manager.subscribe("username", AnnouncementCategory.OTHER, validListener);
        
        manager.notify(announcement);
        assertNotNull(validListener.getReceived());
    }
}
