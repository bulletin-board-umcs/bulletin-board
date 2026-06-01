package pl.umcs.storage;

import pl.umcs.data.Announcement;
import pl.umcs.data.Comment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AnnouncementStore {

    private final Map<UUID, Announcement> announcements = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final JsonStorage storage;
    private final File file;

    public AnnouncementStore(JsonStorage storage, String filePath) {
        this.storage = storage;
        this.file = new File(filePath);
        load();
    }

    private void load() {
        try {
            List<Announcement> loaded = storage.loadList(file, Announcement.class);
            loaded.forEach(a -> announcements.put(a.getId(), a));
            System.out.println("Loaded announcements: " + loaded.size());
        } catch (IOException e) {
            System.err.println("An error occurred while loading announcements: " +
                    e.getMessage()
            );
        }
    }

    private void persist() {
        try {
            storage.saveList(file, new ArrayList<>(announcements.values()));
        } catch (IOException e) {
            System.err.println("An error occurred while saving announcements: " +
                    e.getMessage()
            );
        }
    }

    public void add(Announcement announcement) {
        lock.writeLock().lock();
        try {
            announcements.put(announcement.getId(), announcement);
            persist();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<Announcement> getAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(announcements.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    public Announcement getById(UUID id) {
        lock.readLock().lock();
        try {
            return announcements.get(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean removeById(UUID id) {
        lock.writeLock().lock();
        try {
            boolean removed = announcements.remove(id) != null;

            if (removed) {
                persist();
            }

            return removed;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addComment(UUID announcementId, Comment comment) {
        lock.writeLock().lock();
        try {
            boolean exists = announcements
                    .computeIfPresent(announcementId,
                            (_, announcement) -> {
                                announcement.addComment(comment);
                                return announcement;
                            }) != null;

            if (!exists) {
                throw new IllegalArgumentException(
                        "This announcement does not exist: " +
                                announcementId
                );
            }

            persist();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int removeExpired() {
        lock.writeLock().lock();
        try {
            List<UUID> expired = announcements.values().stream()
                    .filter(Announcement::isExpired)
                    .map(Announcement::getId)
                    .toList();

            expired.forEach(announcements::remove);
            if(!expired.isEmpty()) {
                persist();
            }

            return expired.size();
        } finally {
            lock.writeLock().unlock();
        }
    }
}
