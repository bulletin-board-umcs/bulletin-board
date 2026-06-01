package pl.umcs.client;

import pl.umcs.BulletinBoardService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class Client {

    private static final String HOST = "localhost";
    private static final int PORT = 6999;
    private static final String SERVICE_NAME = "BulletinBoard";

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║         BulletinBoard Client         ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.printf("Connecting to %s:%d/%s ...%n", HOST, PORT, SERVICE_NAME);

        try {
            Registry registry = LocateRegistry.getRegistry(HOST, PORT);
            BulletinBoardService service =
                    (BulletinBoardService) registry.lookup(SERVICE_NAME);

            System.out.println("Connected successfully.\n");

            ClientSession session = new ClientSession(service);
            session.run();

        } catch (Exception e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
        }
    }
}