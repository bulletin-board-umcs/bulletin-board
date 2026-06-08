package pl.umcs.manager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.umcs.AnnouncementListener;
import pl.umcs.data.Announcement;
import pl.umcs.data.AnnouncementCategory;
import pl.umcs.storage.JsonStorage;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.stream.Collectors.toList;

public class NotificationManager {

    private final Map<AnnouncementCategory, List<AnnouncementListener>> subscribers;

    private final Map<String, List<AnnouncementCategory>> persistentSubscriptions;

    private final JsonStorage storage;
    private final File file;


    public NotificationManager(JsonStorage storage, String filePath) {
        subscribers = new ConcurrentHashMap<>();

        this.storage = storage;
        this.file = new File(filePath);

        for (AnnouncementCategory category : AnnouncementCategory.values()) {
            subscribers.put(category, new CopyOnWriteArrayList<>());
        }

        persistentSubscriptions = new ConcurrentHashMap<>();
        load();
    }

    public void subscribe(
            String username,
            AnnouncementCategory category,
            AnnouncementListener announcementListener
    ) {
        subscribers.get(category).add(announcementListener);

        persistentSubscriptions
                .computeIfAbsent(username, _ -> new CopyOnWriteArrayList<>());

        List<AnnouncementCategory> userCategories =
                persistentSubscriptions.get(username);

        if (!userCategories.contains(category)) {
            userCategories.add(category);
            persist();
        }
    }

    public void unsubscribe(String username, AnnouncementCategory category,
                            AnnouncementListener listener) {
        subscribers.get(category).remove(listener);

        List<AnnouncementCategory> userCategories =
                persistentSubscriptions.get(username);

        if (userCategories != null) {
            userCategories.remove(category);
            if (userCategories.isEmpty()) {
                persistentSubscriptions.remove(username);
            }
            persist();
        }
    }

    public List<AnnouncementCategory> getSubscribedCategories(String username) {
        return persistentSubscriptions.getOrDefault(username, Collections.emptyList());
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

    private void load() {
        if (!file.exists()) {
            try {
                file.createNewFile();
                //persist();
            } catch (IOException e) {
                System.out.println(
                        "An error occurred while creating subscriptions file"
                );
            }
            return;
        }

        try {
            ObjectMapper mapper = storage.getMapper();

            Map<String, List<String>> raw = mapper.readValue(file,
                    new TypeReference<>() {
                    });

            raw.forEach((username, categoryNames) -> {
                List<AnnouncementCategory> categories = categoryNames.stream()
                        .map(AnnouncementCategory::valueOf)
                        .collect(toList());
                persistentSubscriptions.put(username,
                        new CopyOnWriteArrayList<>(categories));
            });

            System.out.println("Loaded subscriptions: " + persistentSubscriptions.size());
        } catch (IOException e) {
            System.err.println(
                    "An error occurred while loading subscriptions: " +
                            e.getMessage()
            );
        }
    }

    private void persist() {
        try {
            Map<String, List<String>> raw = new HashMap<>();
            persistentSubscriptions.forEach(
                    (username, categories) -> {
                        List<String> names = categories.stream()
                                .map(AnnouncementCategory::name)
                                .collect(toList());
                        raw.put(username, names);
                    });
            storage.getMapper().writeValue(file, raw);
        } catch (IOException e) {
            System.err.println(
                    "An error occurred while saving subscriptions: " +
                            e.getMessage()
            );
        }
    }
}
