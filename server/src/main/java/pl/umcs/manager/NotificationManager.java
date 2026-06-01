package pl.umcs.manager;

import pl.umcs.AnnouncementListener;
import pl.umcs.data.Announcement;
import pl.umcs.data.AnnouncementCategory;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationManager {

    private final Map<AnnouncementCategory, List<AnnouncementListener>> subscribers;

    public NotificationManager() {
        subscribers = new ConcurrentHashMap<>();

        for (AnnouncementCategory category : AnnouncementCategory.values()) {
            subscribers.put(category, new CopyOnWriteArrayList<>());
        }
    }

    public void subscribe(
            AnnouncementCategory category,
            AnnouncementListener announcementListener
    ) {
        subscribers.get(category).add(announcementListener);
    }

    public void unsubscribe(
            AnnouncementCategory category,
            AnnouncementListener announcementListener
    ) {
        subscribers.get(category).remove(announcementListener);
    }

    public void notify(Announcement announcement) {
        List<AnnouncementListener> listeners =
                subscribers.get(announcement.getCategory());

        for (AnnouncementListener listener : listeners) {
            try {
                listener.onNewAnnouncement(announcement);
            } catch (RemoteException e) {
                listeners.remove(listener);
                System.out.println("A listener has been removed: " + e.getMessage());
            }
        }
    }
}
