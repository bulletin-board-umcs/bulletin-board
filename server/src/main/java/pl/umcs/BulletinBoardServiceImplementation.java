package pl.umcs;

import pl.umcs.data.Announcement;
import pl.umcs.data.AnnouncementCategory;
import pl.umcs.data.Comment;
import pl.umcs.data.User;
import pl.umcs.manager.NotificationManager;
import pl.umcs.manager.SessionManager;
import pl.umcs.storage.AnnouncementStore;
import pl.umcs.storage.UserStore;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.UUID;

public class BulletinBoardServiceImplementation extends UnicastRemoteObject implements BulletinBoardService {

    private final UserStore userStore;
    private final SessionManager sessionManager;
    private final AnnouncementStore announcementStore;
    private final NotificationManager notificationManager;

    public BulletinBoardServiceImplementation(
            UserStore userStore,
            SessionManager sessionManager,
            AnnouncementStore announcementStore,
            NotificationManager notificationManager
    ) throws RemoteException {
        super();
        this.userStore = userStore;
        this.sessionManager = sessionManager;
        this.announcementStore = announcementStore;
        this.notificationManager = notificationManager;
    }

    @Override
    public String login(String username, String password) throws RemoteException {
        User user = userStore.getUser(username);

        if (user == null || !user.getPassword().equals(userStore.hash(password))) {
            throw new RemoteException("Invalid username or password");
        }
        String token = sessionManager.createSession(user);
        System.out.println("Successfully logged in as " + user.getUsername());

        return token;
    }

//    @Override
//    public void register(String username, String password) throws RemoteException {
//        if (username == null || username.isBlank()) {
//            throw new RemoteException("Username cannot be null or blank.");
//        }
//
//        if (password == null || password.length() < 4) {
//            throw new RemoteException(
//                    "Password must be at least 4 characters long."
//            );
//        }
//
//        if (userStore.checkIfUserExists(username)) {
//            throw new RemoteException(
//                    "User '" + username + "' already exists."
//            );
//        }
//
//        userStore.addUser(
//                new User(username, userStore.hash(password), false)
//        );
//        System.out.println(
//                "User '" + username + "' has been successfully registered."
//        );
//    }

    @Override
    public void register(String username, String password) throws RemoteException {
        if (username == null || username.isBlank()) {
            throw new RemoteException("Username cannot be null or blank.");
        }
        if (password == null || password.length() < 4) {
            throw new RemoteException("Password must be at least 4 characters long.");
        }

        boolean added = userStore.addUser(
                new User(username, userStore.hash(password), false)
        );

        if (!added) {
            throw new RemoteException("User '" + username + "' already exists.");
        }

        System.out.println("User '" + username + "' has been successfully registered.");
    }

    @Override
    public void logout(String token) throws RemoteException {
        sessionManager.removeSession(token);
    }

    @Override
    public List<Announcement> getAnnouncements() throws RemoteException {
        return announcementStore.getAll();
    }

    @Override
    public UUID addAnnouncement(
            String token,
            String title,
            String content,
            AnnouncementCategory category
    ) throws RemoteException {
        User user = requireUser(token);

        if (title == null || title.isBlank()) {
            throw new RemoteException("Title cannot be null or blank.");
        }

        if (content == null || content.isBlank()) {
            throw new RemoteException("Content cannot be null or blank.");
        }

        Announcement announcement = Announcement.create(
                title,
                content,
                user.getUsername(),
                category
        );

        announcementStore.add(announcement);
        notificationManager.notify(announcement);

        System.out.println("New announcement [" + category + "]: "
                + title + " from " + user.getUsername());
        return announcement.getId();
    }

    @Override
    public void deleteAnnouncement(String token, UUID id) throws RemoteException {
        User user = requireUser(token);
        Announcement announcement = announcementStore.getById(id);

        if (announcement == null) {
            throw new RemoteException("This announcement does not exist.");
        }

        boolean isOwner = announcement.getAuthorUsername()
                .equals(user.getUsername());

        if (!isOwner && !user.isAdmin()) {
            throw new RemoteException(
                    "Announcement cannot be deleted: permission denied"
            );
        }

        announcementStore.removeById(id);

        System.out.println(
                "Announcement with id: " + id +
                        " has been removed by " + user.getUsername()
        );
    }

    @Override
    public void addComment(
            String token,
            UUID announcementId,
            String content
    ) throws RemoteException {
        User user = requireUser(token);

        if (content == null || content.isBlank()) {
            throw new RemoteException("Content cannot be null or blank.");
        }

        try {
            announcementStore.addComment(
                    announcementId,
                    Comment.create(user.getUsername(), content)
            );
        } catch (IllegalArgumentException e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public void subscribe(
            String token,
            AnnouncementCategory category,
            AnnouncementListener listener
    ) throws RemoteException {
        User user = requireUser(token);

        notificationManager.subscribe(user.getUsername(), category, listener);
    }

    @Override
    public void unsubscribe(
            String token,
            AnnouncementCategory category,
            AnnouncementListener listener
    ) throws RemoteException {
        User user = requireUser(token);

        notificationManager.unsubscribe(user.getUsername(), category, listener);
    }

    private User requireUser(String token) throws RemoteException {
        try {
            return sessionManager.requireUser(token);
        } catch (IllegalArgumentException e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public List<AnnouncementCategory> getSubscribedCategories(String token)
            throws RemoteException {
        User user = requireUser(token);
        return notificationManager.getSubscribedCategories(user.getUsername());
    }
}
