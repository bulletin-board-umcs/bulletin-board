package pl.umcs.storage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.umcs.data.User;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class UserStoreTest {

    private static final String TEST_FILE = "test_users.json";
    private UserStore store;
    private JsonStorage jsonStorage;

    @BeforeEach
    void setUp() {
        jsonStorage = new JsonStorage();
        new File(TEST_FILE).delete();
        store = new UserStore(jsonStorage, TEST_FILE);
    }

    @AfterEach
    void tearDown() {
        new File(TEST_FILE).delete();
    }

    @Test
    void testAdminCreationOnStartup() {
        assertTrue(store.checkIfUserExists("admin"));
        User admin = store.getUser("admin");
        assertNotNull(admin);
        assertTrue(admin.isAdmin());
    }

    @Test
    void testAddAndGetUser() {
        User user = new User("username", store.hash("password"), false);
        store.addUser(user);

        assertTrue(store.checkIfUserExists("username"));
        User retrieved = store.getUser("username");
        assertNotNull(retrieved);
        assertEquals("username", retrieved.getUsername());
        assertFalse(retrieved.isAdmin());
    }

    @Test
    void testPasswordHashing() {
        String password = "password";
        String hash1 = store.hash(password);
        String hash2 = store.hash(password);
        
        assertEquals(hash1, hash2);
        assertNotEquals(password, hash1);
    }

    @Test
    void testPersistence() {
        User user = new User("username", store.hash("password"), false);
        store.addUser(user);

        UserStore newStore = new UserStore(jsonStorage, TEST_FILE);
        assertTrue(newStore.checkIfUserExists("username"));
    }
}
