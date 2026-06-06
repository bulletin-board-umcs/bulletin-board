package pl.umcs.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AnnouncementTest {

    private Announcement announcement;
    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    @BeforeEach
    void setup() {
        id = UUID.randomUUID();
        createdAt = LocalDateTime.now();
        expiresAt = createdAt.plusHours(24);
        announcement = new Announcement(
                id,
                "title",
                "content",
                "username",
                AnnouncementCategory.OTHER,
                createdAt,
                expiresAt,
                null
        );
    }

    @Test
    public void testAnnouncementDefaultConstructor() {
        Announcement a = new Announcement();

        assertNull(a.getId());
        assertNull(a.getTitle());
        assertNull(a.getContent());
        assertNull(a.getAuthorUsername());
        assertNull(a.getCategory());
        assertNull(a.getCreatedAt());
        assertNull(a.getExpiresAt());
        assertTrue(a.getComments().isEmpty());
    }

    @Test
    void testCreate() {
        Announcement a = Announcement.create("title", "content", "username", AnnouncementCategory.ELECTRONICS);

        assertNotNull(a.getId());
        assertEquals("title", a.getTitle());
        assertEquals("content", a.getContent());
        assertEquals("username", a.getAuthorUsername());
        assertEquals(AnnouncementCategory.ELECTRONICS, a.getCategory());
        assertNotNull(a.getCreatedAt());
        assertNotNull(a.getExpiresAt());
        assertTrue(a.getComments().isEmpty());
        assertTrue(a.getExpiresAt().isAfter(a.getCreatedAt().plusHours(23)));
    }

    @Test
    void testGetId() {
        assertEquals(id, announcement.getId());
    }

    @Test
    void testGetTitle() {
        assertEquals("title", announcement.getTitle());
    }

    @Test
    void testGetContent() {
        assertEquals("content", announcement.getContent());
    }

    @Test
    void testGetAuthorUsername() {
        assertEquals("username", announcement.getAuthorUsername());
    }

    @Test
    void testGetCategory() {
        assertEquals(AnnouncementCategory.OTHER, announcement.getCategory());
    }

    @Test
    void testGetCreatedAt() {
        assertEquals(createdAt, announcement.getCreatedAt());
    }

    @Test
    void testGetExpiresAt() {
        assertEquals(expiresAt, announcement.getExpiresAt());
    }

    @Test
    void testAddComment() {
        Comment comment = Comment.create("username", "content");
        announcement.addComment(comment);
        
        assertEquals(1, announcement.getComments().size());
        assertEquals(comment, announcement.getComments().getFirst());
    }

    @Test
    void testIsExpired() {
        assertFalse(announcement.isExpired());


        Announcement expired = new Announcement(
                UUID.randomUUID(),
                "title",
                "content",
                "username",
                AnnouncementCategory.OTHER,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1),
                null
        );
        assertTrue(expired.isExpired());
    }
}
