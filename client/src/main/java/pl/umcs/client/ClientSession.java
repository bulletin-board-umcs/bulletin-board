package pl.umcs.client;

import pl.umcs.BulletinBoardService;
import pl.umcs.data.Announcement;
import pl.umcs.data.AnnouncementCategory;
import pl.umcs.data.Comment;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;


public class ClientSession {

    private final BulletinBoardService service;
    private final Scanner scanner;

    private String token = null;
    private String currentUsername = null;

    private final SubscriptionManager subscriptionManager;

    public ClientSession(BulletinBoardService service) {
        this.service = service;
        this.scanner = new Scanner(System.in);
        this.subscriptionManager = new SubscriptionManager(service);
    }

    public void run() {
        boolean running = true;

        while (running) {
            if (token == null) {
                running = handleAuthMenu();
            } else {
                running = handleMainMenu();
            }
        }

        if (token != null) {
            performLogout();
        }

        System.out.println("Goodbye!");
        scanner.close();
    }


    private boolean handleAuthMenu() {
        System.out.println("\n── Authentication ────────────────────");
        System.out.println("  1) Login");
        System.out.println("  2) Register");
        System.out.println("  0) Exit");
        System.out.print("Choice: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1" -> doLogin();
            case "2" -> doRegister();
            case "0" -> { return false; }
            default  -> System.out.println("Unknown option. Try again.");
        }
        return true;
    }

    private void doLogin() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        try {
            token = service.login(username, password);
            currentUsername = username;
            System.out.println("Logged in as " + username + ".");

            restoreSubscriptions();
        } catch (RemoteException e) {
            printError("Login failed", e);
        }
    }

    private void restoreSubscriptions() {
        try {
            List<AnnouncementCategory> saved = service.getSubscribedCategories(token);
            if (saved.isEmpty()) return;

            for (AnnouncementCategory category : saved) {
                subscriptionManager.subscribe(token, category);
            }
        } catch (RemoteException e) {
            System.err.println("Warning: could not restore subscriptions: " + e.getMessage());
        }
    }

    private void doRegister() {
        System.out.print("Choose username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Choose password (min 4 chars): ");
        String password = scanner.nextLine().trim();

        try {
            service.register(username, password);
            System.out.println("Registration successful. You can now log in.");
        } catch (RemoteException e) {
            printError("Registration failed", e);
        }
    }


    private boolean handleMainMenu() {
        System.out.println("\n── Main Menu [" + currentUsername + "] ─────────────");
        System.out.println("  1) Browse announcements");
        System.out.println("  2) Search announcements by category");
        System.out.println("  3) Show announcement details & comments");
        System.out.println("  4) Add announcement");
        System.out.println("  5) Delete announcement");
        System.out.println("  6) Add comment");
        System.out.println("  7) Subscribe to category");
        System.out.println("  8) Unsubscribe from category");
        System.out.println("  9) Show active subscriptions");
        System.out.println("  10) Browse in subscribed categories");
        System.out.println("  0) Logout");
        System.out.print("Choice: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1" -> doBrowse();
            case "2" -> doSearchByCategory();
            case "3" -> doShowDetails();
            case "4" -> doAddAnnouncement();
            case "5" -> doDeleteAnnouncement();
            case "6" -> doAddComment();
            case "7" -> doSubscribe();
            case "8" -> doUnsubscribe();
            case "9" -> subscriptionManager.printSubscriptions();
            case "10" -> doBrowseSubscribed();
            case "0" -> { performLogout(); return true; }
            default  -> System.out.println("Unknown option. Try again.");
        }
        return true;
    }


    private void doBrowse() {
        try {
            List<Announcement> list = service.getAnnouncements();
            if (list.isEmpty()) {
                System.out.println("No announcements found.");
                return;
            }
            printAnnouncementList(list);
        } catch (RemoteException e) {
            printError("Could not fetch announcements", e);
        }
    }

    private void doBrowseSubscribed() {
        Set<AnnouncementCategory> subscribed = subscriptionManager.getSubscribedCategories();

        if (subscribed.isEmpty()) {
            System.out.println("You have no active subscriptions. Use option 7 to subscribe.");
            return;
        }

        try {
            List<Announcement> all = service.getAnnouncements();
            List<Announcement> filtered = all.stream()
                    .filter(a -> subscribed.contains(a.getCategory()))
                    .toList();

            if (filtered.isEmpty()) {
                System.out.println("No announcements found in your subscribed categories: " + subscribed);
                return;
            }

            System.out.println("\nAnnouncements in your subscribed categories " + subscribed + ":");
            printAnnouncementList(filtered);
        } catch (RemoteException e) {
            printError("Could not fetch announcements", e);
        }
    }


    private void doSearchByCategory() {
        AnnouncementCategory category = promptCategory();
        if (category == null) return;

        try {
            List<Announcement> all = service.getAnnouncements();
            List<Announcement> filtered = all.stream()
                    .filter(a -> a.getCategory() == category)
                    .toList();

            if (filtered.isEmpty()) {
                System.out.println("No announcements in category: " + category);
                return;
            }
            System.out.println("\nResults for [" + category + "]:");
            printAnnouncementList(filtered);
        } catch (RemoteException e) {
            printError("Could not fetch announcements", e);
        }
    }

    private void doShowDetails() {
        UUID id = promptUUID("announcement ID");
        if (id == null) return;

        try {
            List<Announcement> all = service.getAnnouncements();
            Announcement found = all.stream()
                    .filter(a -> a.getId().equals(id))
                    .findFirst()
                    .orElse(null);

            if (found == null) {
                System.out.println("Announcement not found: " + id);
                return;
            }
            printAnnouncementDetails(found);
        } catch (RemoteException e) {
            printError("Could not fetch announcement", e);
        }
    }

    private void doAddAnnouncement() {
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();

        System.out.print("Content: ");
        String content = scanner.nextLine().trim();

        AnnouncementCategory category = promptCategory();
        if (category == null) return;

        try {
            UUID id = service.addAnnouncement(token, title, content, category);
            System.out.println("Announcement created with ID: " + id);
        } catch (RemoteException e) {
            printError("Could not add announcement", e);
        }
    }

    private void doDeleteAnnouncement() {
        UUID id = promptUUID("announcement ID to delete");
        if (id == null) return;

        System.out.print("Are you sure? (yes/no): ");
        String confirm = scanner.nextLine().trim();
        if (!confirm.equalsIgnoreCase("yes")) {
            System.out.println("Cancelled.");
            return;
        }

        try {
            service.deleteAnnouncement(token, id);
            System.out.println("Announcement deleted.");
        } catch (RemoteException e) {
            printError("Could not delete announcement", e);
        }
    }

    private void doAddComment() {
        UUID id = promptUUID("announcement ID");
        if (id == null) return;

        System.out.print("Comment: ");
        String content = scanner.nextLine().trim();

        try {
            service.addComment(token, id, content);
            System.out.println("Comment added.");
        } catch (RemoteException e) {
            printError("Could not add comment", e);
        }
    }


    private void doSubscribe() {
        AnnouncementCategory category = promptCategory();
        if (category == null) return;

        try {
            subscriptionManager.subscribe(token, category);
        } catch (RemoteException e) {
            printError("Subscription failed", e);
        }
    }

    private void doUnsubscribe() {
        AnnouncementCategory category = promptCategory();
        if (category == null) return;

        try {
            subscriptionManager.unsubscribe(token, category);
        } catch (RemoteException e) {
            printError("Unsubscription failed", e);
        }
    }


    private void performLogout() {
        try {
            service.logout(token);
            System.out.println("Logged out.");
        } catch (RemoteException e) {
            System.err.println("Warning: logout error: " + e.getMessage());
        } finally {
            token = null;
            currentUsername = null;
        }
    }


    private AnnouncementCategory promptCategory() {
        AnnouncementCategory[] values = AnnouncementCategory.values();
        System.out.println("Categories:");
        for (int i = 0; i < values.length; i++) {
            System.out.printf("  %d) %s%n", i + 1, values[i]);
        }
        System.out.print("Choice (1-" + values.length + "): ");

        String input = scanner.nextLine().trim();
        try {
            int idx = Integer.parseInt(input) - 1;
            if (idx < 0 || idx >= values.length) {
                System.out.println("Invalid category index.");
                return null;
            }
            return values[idx];
        } catch (NumberFormatException e) {
            System.out.println("Invalid input – please enter a number.");
            return null;
        }
    }


    private UUID promptUUID(String label) {
        System.out.print("Enter " + label + ": ");
        String input = scanner.nextLine().trim();
        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid UUID format.");
            return null;
        }
    }


    private void printAnnouncementList(List<Announcement> list) {
        System.out.printf("%n%-36s  %-20s  %-12s  %s%n",
                "ID", "Author", "Category", "Title");
        System.out.println("─".repeat(90));
        for (Announcement a : list) {
            System.out.printf("%-36s  %-20s  %-12s  %s%n",
                    a.getId(),
                    a.getAuthorUsername(),
                    a.getCategory(),
                    truncate(a.getTitle(), 30));
        }
    }

    private void printAnnouncementDetails(Announcement a) {
        System.out.println("\n══════════════════════════════════════════════════");
        System.out.println("Title   : " + a.getTitle());
        System.out.println("Author  : " + a.getAuthorUsername());
        System.out.println("Category: " + a.getCategory());
        System.out.println("ID      : " + a.getId());
        System.out.println("──────────────────────────────────────────────────");
        System.out.println(a.getContent());

        List<Comment> comments = a.getComments();
        if (comments == null || comments.isEmpty()) {
            System.out.println("\n[No comments yet]");
        } else {
            System.out.println("\nComments (" + comments.size() + "):");
            for (Comment comment : comments) {
                System.out.println("  • " + comment.getAuthorUsername() + " • " + comment.getContent());
            }
        }
        System.out.println("══════════════════════════════════════════════════");
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    private void printError(String context, RemoteException e) {
        String msg = e.getMessage();
        System.err.println("[ERROR] " + context + ": " + (msg != null ? msg : e.toString()));
    }
}