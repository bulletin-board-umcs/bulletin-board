package pl.umcs.scheduler;

import pl.umcs.storage.AnnouncementStore;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExpirationScheduler {

    private final ScheduledExecutorService scheduler;
    private final AnnouncementStore announcementStore;

    public ExpirationScheduler(AnnouncementStore announcementStore) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.announcementStore = announcementStore;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::removeExpired, 1, 1, TimeUnit.MINUTES);
        System.out.println("Expiration Scheduler started.");
    }

    public void stop() {
        scheduler.shutdown();
    }

    private void removeExpired() {
        int removed = announcementStore.removeExpired();

        if (removed > 0) {
            System.out.println("Removed expired announcements: " + removed);
        }
    }
}
