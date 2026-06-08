package pl.umcs.storage;

import pl.umcs.data.User;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class UserStore {
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final JsonStorage storage;
    private final File file;

    public UserStore(JsonStorage storage, String filePath) {
        this.storage = storage;
        this.file    = new File(filePath);
        load();
        createAdminIfNotExists();
    }

    private void load() {
        try {
            List<User> loaded = storage.loadList(file, User.class);
            loaded.forEach(u -> users.put(u.getUsername(), u));
        } catch (IOException e) {
            System.err.println(
                    "An error occurred while trying to load" +
                    " users from data storage: " +
                    e.getMessage()
            );
        }
    }

    private void persist() {
        try {
            storage.saveList(file, new ArrayList<>(users.values()));
        } catch (IOException e) {
            System.err.println("An error occurred while trying to save users: " +
                    e.getMessage()
            );
        }
    }

    private void createAdminIfNotExists() {
        if (!users.containsKey("admin")) {
            users.put("admin", new User(
                    "admin",
                    hash("admin"),
                    true
            ));

            persist();

            System.out.println("Successfully created the admin account" +
                    " (login: admin, password: admin)"
            );
        }
    }

    public boolean checkIfUserExists(String username) {
        lock.readLock().lock();
        try {
            return users.containsKey(username);
        } finally {
            lock.readLock().unlock();
        }
    }

    public User getUser(String username) {
        lock.readLock().lock();
        try {
            return users.get(username);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean addUser(User user) {
        lock.writeLock().lock();
        try {
            if (users.containsKey(user.getUsername())) {
                return false;
            }
            users.put(user.getUsername(), user);
            persist();
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

//    public void addUser(User user) {
//        lock.writeLock().lock();
//        try {
//            users.put(user.getUsername(), user);
//            persist();
//        } finally {
//            lock.writeLock().unlock();
//        }
//    }

    public String hash(String text) {
        return Integer.toHexString(text.hashCode());
    }
}
