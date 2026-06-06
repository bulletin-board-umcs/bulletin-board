package pl.umcs.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.umcs.AnnouncementListener;
import pl.umcs.BulletinBoardService;
import pl.umcs.data.Announcement;
import pl.umcs.data.AnnouncementCategory;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SubscriptionManagerTest {

    private SubscriptionManager manager;
    private MockBulletinBoardService service;

    @BeforeEach
    void setUp() {
        service = new MockBulletinBoardService();
        manager = new SubscriptionManager(service);
    }

    @Test
    void testSubscribe() throws RemoteException {
        manager.subscribe("token", AnnouncementCategory.OTHER);
        
        assertTrue(manager.getSubscribedCategories().contains(AnnouncementCategory.OTHER));
        assertEquals(1, service.subscribedCategories.size());
    }

    @Test
    void testUnsubscribe() throws RemoteException {
        manager.subscribe("token", AnnouncementCategory.OTHER);
        manager.unsubscribe("token", AnnouncementCategory.OTHER);
        
        assertFalse(manager.getSubscribedCategories().contains(AnnouncementCategory.OTHER));
        assertEquals(0, service.subscribedCategories.size());
    }

    @Test
    void testSubscribeAlreadySubscribed() throws RemoteException {
        manager.subscribe("token", AnnouncementCategory.OTHER);
        manager.subscribe("token", AnnouncementCategory.OTHER);
        
        assertEquals(1, manager.getSubscribedCategories().size());
        assertEquals(1, service.subscribedCategories.size());
    }

    private static class MockBulletinBoardService implements BulletinBoardService {
        List<AnnouncementCategory> subscribedCategories = new ArrayList<>();

        @Override
        public String login(String u, String p) { return "token"; }
        @Override
        public void register(String u, String p) {}
        @Override
        public void logout(String t) {}
        @Override
        public List<Announcement> getAnnouncements() { return List.of(); }
        @Override
        public UUID addAnnouncement(String t, String ti, String c, AnnouncementCategory cat) { return null; }
        @Override
        public void deleteAnnouncement(String t, UUID id) {}
        @Override
        public void addComment(String t, UUID id, String c) {}
        @Override
        public void subscribe(String t, AnnouncementCategory c, AnnouncementListener l) { subscribedCategories.add(c); }
        @Override
        public void unsubscribe(String t, AnnouncementCategory c, AnnouncementListener l) { subscribedCategories.remove(c); }
        @Override
        public List<AnnouncementCategory> getSubscribedCategories(String t) { return subscribedCategories; }
    }
}
