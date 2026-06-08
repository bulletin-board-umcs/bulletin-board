package pl.umcs.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.umcs.AnnouncementListener;
import pl.umcs.BulletinBoardService;
import pl.umcs.data.Announcement;
import pl.umcs.data.AnnouncementCategory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ClientSessionTest {

    private MockBulletinBoardService service;
    private InputStream originalIn;
    private PrintStream originalOut;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUp() {
        service = new MockBulletinBoardService();
        originalIn = System.in;
        originalOut = System.out;
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    private void provideInput(String data) {
        System.setIn(new ByteArrayInputStream(data.getBytes()));
    }

    @Test
    void testExitFromAuthMenu() {
        provideInput("0\n");
        ClientSession session = new ClientSession(service);
        session.run();

        String output = outContent.toString();
        assertTrue(output.contains("Authentication"));
        assertTrue(output.contains("Goodbye!"));
    }

    @Test
    void testLoginSuccess() throws RemoteException {
        provideInput("1\nuser\npass\n0\n0\n");
        ClientSession session = new ClientSession(service);
        session.run();

        String output = outContent.toString();
        assertTrue(output.contains("Logged in as user."));
        assertEquals("user", service.lastLoginUsername);
    }

    @Test
    void testRegisterSuccess() throws RemoteException {
        provideInput("2\nuser\npass\n0\n");
        ClientSession session = new ClientSession(service);
        session.run();

        assertEquals("user", service.lastRegisterUsername);
        String output = outContent.toString();
        assertTrue(output.contains("Registration successful."));
    }

    @Test
    void testBrowseAnnouncements() throws RemoteException {
        Announcement a = Announcement.create("Title1", "Content1", "Author1", AnnouncementCategory.OTHER);
        service.announcements.add(a);
        
        provideInput("1\nuser\npass\n1\n0\n0\n");
        ClientSession session = new ClientSession(service);
        session.run();

        String output = outContent.toString();
        assertTrue(output.contains("Title1"));
        assertTrue(output.contains("Author1"));
    }

    private static class MockBulletinBoardService implements BulletinBoardService {
        String lastLoginUsername;
        String lastRegisterUsername;
        List<Announcement> announcements = new ArrayList<>();

        @Override
        public String login(String u, String p) { lastLoginUsername = u; return "token"; }
        @Override
        public void register(String u, String p) { lastRegisterUsername = u; }
        @Override
        public void logout(String t) {}
        @Override
        public List<Announcement> getAnnouncements() { return announcements; }
        @Override
        public UUID addAnnouncement(String t, String ti, String c, AnnouncementCategory cat) { return null; }
        @Override
        public void deleteAnnouncement(String t, UUID id) {}
        @Override
        public void addComment(String t, UUID id, String c) {}
        @Override
        public void subscribe(String t, AnnouncementCategory c, AnnouncementListener l) {}
        @Override
        public void unsubscribe(String t, AnnouncementCategory c, AnnouncementListener l) {}
        @Override
        public List<AnnouncementCategory> getSubscribedCategories(String t) { return new ArrayList<>(); }
    }
}
