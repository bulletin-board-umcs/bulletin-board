package pl.umcs.client;

import org.junit.jupiter.api.Test;
import pl.umcs.data.Announcement;
import pl.umcs.data.AnnouncementCategory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.*;

class AnnouncementListenerImplTest {

    @Test
    void testOnNewAnnouncement() throws RemoteException {
        AnnouncementListenerImpl listener = new AnnouncementListenerImpl(AnnouncementCategory.ELECTRONICS);
        Announcement announcement = Announcement.create("title", "content", "username", AnnouncementCategory.ELECTRONICS);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        listener.onNewAnnouncement(announcement);

        String output = outContent.toString();
        assertTrue(output.contains("NEW ANNOUNCEMENT [ELECTRONICS]"));
        assertTrue(output.contains("Title  : title"));
        assertTrue(output.contains("Author : username"));

        System.setOut(System.out);
    }

    @Test
    void testGetCategory() throws RemoteException {
        AnnouncementListenerImpl listener = new AnnouncementListenerImpl(AnnouncementCategory.JOBS);
        assertEquals(AnnouncementCategory.JOBS, listener.getCategory());
    }
}
