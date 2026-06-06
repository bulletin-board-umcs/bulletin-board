package pl.umcs.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CommentTest {

    private Comment comment;
    private UUID uid;
    private LocalDateTime localDateTime;
    @BeforeEach
    void setup() {
        uid = UUID.randomUUID();
        localDateTime = LocalDateTime.of(2026, Month.JUNE, 3, 10, 0);
        comment = new Comment(uid, "username", "content", localDateTime);
    }

    @Test
    public void testCommentDefaultConstructor() {
        Comment comment = new Comment();

        assertNull(comment.getId());
        assertNull(comment.getAuthorUsername());
        assertNull(comment.getContent());
        assertNull(comment.getCreatedAt());
    }
    @Test
    void testCreate() {
            Comment comment = Comment.create("username", "content");

            assertNotNull(comment.getId());
            assertEquals("username", comment.getAuthorUsername());
            assertEquals("content", comment.getContent());
            assertNotNull(comment.getCreatedAt());

    }

    @Test
    void testGetId() {
        assertEquals(uid, comment.getId());
    }

    @Test
    void testGetAuthorUsername() {
        assertEquals("username", comment.getAuthorUsername());
    }

    @Test
    void testGetContent() {
        assertEquals("content", comment.getContent());
    }

    @Test
    void testGetCreatedAt() {
        assertEquals(localDateTime, comment.getCreatedAt());
    }
}