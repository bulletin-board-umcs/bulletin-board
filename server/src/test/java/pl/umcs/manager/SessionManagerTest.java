package pl.umcs.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.umcs.data.User;

import static org.junit.jupiter.api.Assertions.*;

public class SessionManagerTest {

    private SessionManager sessionManager;
    private User testUser;

    @BeforeEach
    void setUp() {
        sessionManager = new SessionManager();
        testUser = new User("username", "password", false);
    }

    @Test
    void testCreateAndRequireSession() {
        String token = sessionManager.createSession(testUser);
        assertNotNull(token);
        
        User retrievedUser = sessionManager.requireUser(token);
        assertEquals(testUser.getUsername(), retrievedUser.getUsername());
    }

    @Test
    void testRequireInvalidToken() {
        assertThrows(IllegalArgumentException.class, () -> {
            sessionManager.requireUser("invalid");
        });
    }

    @Test
    void testRemoveSession() {
        String token = sessionManager.createSession(testUser);
        sessionManager.removeSession(token);
        
        assertThrows(IllegalArgumentException.class, () -> {
            sessionManager.requireUser(token);
        });
    }
}
