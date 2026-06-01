package pl.umcs.client;

import pl.umcs.BulletinBoardService;
import pl.umcs.data.AnnouncementCategory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;


public class SubscriptionManager {

    private final BulletinBoardService service;


    private final Map<AnnouncementCategory, AnnouncementListenerImpl> listeners;

    public SubscriptionManager(BulletinBoardService service) {
        this.service = service;
        this.listeners = new EnumMap<>(AnnouncementCategory.class);
    }


    public void subscribe(String token, AnnouncementCategory category)
            throws RemoteException {

        synchronized (listeners) {
            if (listeners.containsKey(category)) {
                System.out.println("Already subscribed to " + category + ".");
                return;
            }

            AnnouncementListenerImpl listener = new AnnouncementListenerImpl(category);
            service.subscribe(token, category, listener);
            listeners.put(category, listener);

            System.out.println("Subscribed to [" + category + "]. "
                    + "You will receive push notifications for new posts.");
        }
    }


    public void unsubscribe(String token, AnnouncementCategory category)
            throws RemoteException {

        synchronized (listeners) {
            AnnouncementListenerImpl listener = listeners.remove(category);
            if (listener == null) {
                System.out.println("Not subscribed to " + category + ".");
                return;
            }

            service.unsubscribe(token, category, listener);
            unexport(listener);

            System.out.println("Unsubscribed from [" + category + "].");
        }
    }


    public void unsubscribeAll(String token) {
        synchronized (listeners) {
            for (Map.Entry<AnnouncementCategory, AnnouncementListenerImpl> entry
                    : listeners.entrySet()) {

                AnnouncementCategory category = entry.getKey();
                AnnouncementListenerImpl listener = entry.getValue();

                try {
                    service.unsubscribe(token, category, listener);
                } catch (RemoteException e) {
                    System.err.println("Warning: could not unsubscribe from "
                            + category + ": " + e.getMessage());
                } finally {
                    unexport(listener);
                }
            }
            listeners.clear();
        }
    }

    public void printSubscriptions() {
        synchronized (listeners) {
            if (listeners.isEmpty()) {
                System.out.println("No active subscriptions.");
                return;
            }
            System.out.println("Active subscriptions:");
            listeners.keySet().forEach(c -> System.out.println("  • " + c));
        }
    }


    private void unexport(AnnouncementListenerImpl listener) {
        try {
            UnicastRemoteObject.unexportObject(listener, true);
        } catch (Exception e) {}
    }

    public Set<AnnouncementCategory> getSubscribedCategories() {
        synchronized (listeners) {
            return listeners.isEmpty()
                    ? java.util.Collections.emptySet()
                    : java.util.EnumSet.copyOf(listeners.keySet());
        }
    }
}