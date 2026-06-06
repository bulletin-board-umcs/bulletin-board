package pl.umcs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.umcs.data.Announcement;
import pl.umcs.data.AnnouncementCategory;
import pl.umcs.manager.NotificationManager;
import pl.umcs.manager.SessionManager;
import pl.umcs.storage.AnnouncementStore;
import pl.umcs.storage.JsonStorage;
import pl.umcs.storage.UserStore;

import java.io.File;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BulletinBoardServiceImplementationTest {

    private BulletinBoardService service;
    private UserStore userStore;
    private AnnouncementStore announcementStore;
    private NotificationManager notificationManager;
    private SessionManager sessionManager;

    private static final String USERS_FILE = "test_users_impl.json";
    private static final String ANNOUNCEMENTS_FILE = "test_announcements_impl.json";
    private static final String SUBSCRIPTIONS_FILE = "test_subscriptions_impl.json";

    @BeforeEach
    void setup() throws RemoteException {
        new File(USERS_FILE).delete();
        new File(ANNOUNCEMENTS_FILE).delete();
        new File(SUBSCRIPTIONS_FILE).delete();

        JsonStorage storage = new JsonStorage();
        userStore = new UserStore(storage, USERS_FILE);
        announcementStore = new AnnouncementStore(storage, ANNOUNCEMENTS_FILE);
        sessionManager = new SessionManager();
        notificationManager = new NotificationManager(storage, SUBSCRIPTIONS_FILE);

        service = new BulletinBoardServiceImplementation(
                userStore, sessionManager, announcementStore, notificationManager
        );
    }

    @AfterEach
    void tearDown() {
        new File(USERS_FILE).delete();
        new File(ANNOUNCEMENTS_FILE).delete();
        new File(SUBSCRIPTIONS_FILE).delete();
    }

    @Test
    void BulletinBoardServiceImplementationConstructor() {
        assertNotNull(service);
    }

    @Test
    void LoginSuccessful() throws RemoteException {
        service.register("username", "password");
        String token = service.login("username", "password");
        assertNotNull(token);
    }

    @Test
    void LoginInvalid() {
        assertThrows(RemoteException.class, () -> service.login("invalid", "password"));
    }

    @Test
    void RegisterSuccessful() throws RemoteException {
        service.register("username", "password");
        assertTrue(userStore.checkIfUserExists("username"));
    }

    @Test
    void RegisterUsernameIsBlank() {
        assertThrows(RemoteException.class, () -> service.register("", "password"));
        assertThrows(RemoteException.class, () -> service.register(null, "password"));
    }

    @Test
    void RegisterPasswordTooShort() {
        assertThrows(RemoteException.class, () -> service.register("username", "123"));
    }

    @Test
    void RegisterUsernameAlreadyExists() throws RemoteException {
        service.register("username", "password");
        assertThrows(RemoteException.class, () -> service.register("username", "other"));
    }

    @Test
    void testConcurrentRegistrationAbsolute() throws InterruptedException {
        int threadCount = 30;
        java.util.concurrent.CyclicBarrier barrier = new java.util.concurrent.CyclicBarrier(threadCount);
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threadCount);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(threadCount);
        java.util.concurrent.atomic.AtomicInteger successCount = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicInteger failureCount = new java.util.concurrent.atomic.AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    barrier.await(); 
                    service.register("username", "password");
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertEquals(1, successCount.get(), "Only one registration should succeed even with absolute concurrency");
        assertEquals(threadCount - 1, failureCount.get());
    }

    @Test
    void logout() throws RemoteException {
        service.register("username", "password");
        String token = service.login("username", "password");
        service.logout(token);

        assertThrows(RemoteException.class, () -> service.addAnnouncement(token, "title", "content", AnnouncementCategory.OTHER));
    }

    @Test
    void getAnnouncements() throws RemoteException {
        service.register("username", "password");
        String token = service.login("username", "password");
        service.addAnnouncement(token, "title", "content", AnnouncementCategory.OTHER);
        
        List<Announcement> list = service.getAnnouncements();
        assertEquals(1, list.size());
    }

    @Test
    void addAnnouncementSuccessful() throws RemoteException {
        service.register("username", "password");
        String token = service.login("username", "password");
        UUID id = service.addAnnouncement(token, "title", "content", AnnouncementCategory.OTHER);
        assertNotNull(id);
    }

    @Test
    void addAnnouncementTitleIsBlank() throws RemoteException {
        service.register("username", "password");
        String token = service.login("username", "password");
        assertThrows(RemoteException.class, () -> service.addAnnouncement(token, "", "content", AnnouncementCategory.OTHER));
    }

    @Test
    void addAnnouncementContentIsBlank() throws RemoteException {
        service.register("username", "password");
        String token = service.login("username", "password");
        assertThrows(RemoteException.class, () -> service.addAnnouncement(token, "title", "  ", AnnouncementCategory.OTHER));
    }

    @Test
    void deleteAnnouncementSuccessful() throws RemoteException {
        service.register("username", "password");
        String token = service.login("username", "password");
        UUID id = service.addAnnouncement(token, "title", "content", AnnouncementCategory.OTHER);
        
        service.deleteAnnouncement(token, id);
        assertEquals(0, service.getAnnouncements().size());
    }

    @Test
    void deleteAnnouncementThatDoesNotExist() throws RemoteException {
        service.register("username", "password");
        String token = service.login("username", "password");
        assertThrows(RemoteException.class, () -> service.deleteAnnouncement(token, UUID.randomUUID()));
    }

    @Test
    void deleteAnnouncementPermissionDenied() throws RemoteException {
        service.register("username1", "password");
        service.register("username2", "password");
        String token1 = service.login("username1", "password");
        String token2 = service.login("username2", "password");
        
        UUID id = service.addAnnouncement(token1, "title", "content", AnnouncementCategory.OTHER);
        assertThrows(RemoteException.class, () -> service.deleteAnnouncement(token2, id));
    }

    @Test
    void addCommentSuccessful() throws RemoteException {
        service.register("username", "password");
        String token = service.login("username", "password");
        UUID id = service.addAnnouncement(token, "title", "content", AnnouncementCategory.OTHER);
        
        service.addComment(token, id, "content");
        assertEquals(1, service.getAnnouncements().getFirst().getComments().size());
    }

    @Test
    void addCommentContentIsBlank() throws RemoteException {
        service.register("username", "password");
        String token = service.login("username", "password");
        UUID id = service.addAnnouncement(token, "title", "content", AnnouncementCategory.OTHER);
        
        assertThrows(RemoteException.class, () -> service.addComment(token, id, ""));
    }

    @Test
    void subscribe() throws RemoteException {
        service.register("username", "password");
        String token = service.login("username", "password");

        service.subscribe(token, AnnouncementCategory.JOBS, null);
        assertEquals(1, service.getSubscribedCategories(token).size());
    }

    @Test
    void unsubscribe() throws RemoteException {
        service.register("username", "password");
        String token = service.login("username", "password");
        service.subscribe(token, AnnouncementCategory.JOBS, null);
        service.unsubscribe(token, AnnouncementCategory.JOBS, null);
        assertEquals(0, service.getSubscribedCategories(token).size());
    }

    @Test
    void getSubscribedCategories() throws RemoteException {
        service.register("username", "password");
        String token = service.login("username", "password");
        service.subscribe(token, AnnouncementCategory.JOBS, null);
        List<AnnouncementCategory> cats = service.getSubscribedCategories(token);
        assertTrue(cats.contains(AnnouncementCategory.JOBS));
    }

    @Test
    void addAnnouncementInvalidToken() {
        assertThrows(RemoteException.class, () -> 
            service.addAnnouncement("invalid_token", "title", "content", AnnouncementCategory.OTHER));
    }

    @Test
    void deleteAnnouncementInvalidToken() throws RemoteException {
        service.register("username", "password");
        String token = service.login("username", "password");
        UUID id = service.addAnnouncement(token, "title", "content", AnnouncementCategory.OTHER);
        
        assertThrows(RemoteException.class, () -> service.deleteAnnouncement("wrong_token", id));
    }

    @Test
    void deleteAnnouncementByAdmin() throws RemoteException {
        service.register("username", "password");
        String userToken = service.login("username", "password");
        UUID id = service.addAnnouncement(userToken, "title", "content", AnnouncementCategory.OTHER);
        
        String adminToken = service.login("admin", "admin");
        service.deleteAnnouncement(adminToken, id);
        
        assertEquals(0, service.getAnnouncements().size(), "Admin should be able to delete any announcement");
    }

    @Test
    void addCommentInvalidToken() throws RemoteException {
        service.register("username", "password");
        String token = service.login("username", "password");
        UUID id = service.addAnnouncement(token, "title", "content", AnnouncementCategory.OTHER);
        
        assertThrows(RemoteException.class, () -> service.addComment("invalid", id, "content"));
    }

    @Test
    void addCommentAnnouncementNotFound() throws RemoteException {
        service.register("username", "password");
        String token = service.login("username", "password");
        
        assertThrows(RemoteException.class, () -> service.addComment(token, UUID.randomUUID(), "content"));
    }

    @Test
    void subscribeInvalidToken() {
        assertThrows(RemoteException.class, () -> 
            service.subscribe("invalid", AnnouncementCategory.JOBS, null));
    }

    @Test
    void logoutTwice() throws RemoteException {
        service.register("username", "password");
        String token = service.login("username", "password");
        service.logout(token);
        service.logout(token); 
    }
}
