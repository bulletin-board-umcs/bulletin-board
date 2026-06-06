package pl.umcs.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    private User user;

    @BeforeEach
    void setup() {
        user = new User("username", "password", true);
    }

    @Test
    public void testUserCreation() {
        User user = User.create("username", "password", true);
        
        assertEquals("username", user.getUsername());
        assertEquals("password", user.getPassword());
        assertTrue(user.isAdmin());
    }

    @Test
    public void testUserDefaultConstructor() {
        User user = new User();
        
        assertNull(user.getUsername());
        assertNull(user.getPassword());
        assertFalse(user.isAdmin());
    }

    @Test
    void testGetUsername() {
        assertEquals("username", user.getUsername());

    }

    @Test
    void testGetPassword() {
        assertEquals("password", user.getPassword());
    }

    @Test
    void testIsAdmin() {
        assertTrue(user.isAdmin());
    }
}
