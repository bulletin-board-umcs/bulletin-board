package pl.umcs;

import pl.umcs.manager.NotificationManager;
import pl.umcs.manager.SessionManager;
import pl.umcs.scheduler.ExpirationScheduler;
import pl.umcs.storage.AnnouncementStore;
import pl.umcs.storage.JsonStorage;
import pl.umcs.storage.UserStore;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
    private static final int PORT = 6999;
    private static final String SERVICE_NAME = "BulletinBoard";
    private static final String ANNOUNCEMENTS_FILE = "announcements.json";
    private static final String USERS_FILE = "users.json";

    public static void main(String[] args) {

        try {
            JsonStorage jsonStorage = new JsonStorage();

            UserStore userStore = new UserStore(
                    jsonStorage,
                    USERS_FILE
            );

            AnnouncementStore announcementStore = new AnnouncementStore(
                    jsonStorage,
                    ANNOUNCEMENTS_FILE
            );

            SessionManager sessionManager = new SessionManager();
            NotificationManager notificationManager = new NotificationManager();

            BulletinBoardService service = new BulletinBoardServiceImplementation(
                    userStore,
                    sessionManager,
                    announcementStore,
                    notificationManager
            );

            ExpirationScheduler scheduler =
                    new ExpirationScheduler(announcementStore);
            scheduler.start();

            Registry registry = LocateRegistry.createRegistry(PORT);
            registry.rebind(SERVICE_NAME, service);

            System.out.println("Server started on port: " + PORT);
            System.out.println("Service name: " + SERVICE_NAME);
            System.out.println(
                    "Data files: " + USERS_FILE + ", " + ANNOUNCEMENTS_FILE
            );

        } catch (RemoteException e) {
            System.out.println("Failed to start server: " + e.getMessage());
        }
    }
}
