package pl.umcs;

import pl.umcs.data.Announcement;
import pl.umcs.data.AnnouncementCategory;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

public interface BulletinBoardService extends Remote {

    String login(String username, String password) throws RemoteException;
    void register(String username, String password) throws RemoteException;
    void logout(String token) throws RemoteException;

    List<Announcement> getAnnouncements() throws RemoteException;
    UUID addAnnouncement(
            String token,
            String title,
            String content,
            AnnouncementCategory category
    ) throws RemoteException;
    void deleteAnnouncement(String token, UUID id) throws RemoteException;

    void addComment(String token, UUID announcementId,
                    String content) throws RemoteException;

    void subscribe(String token, AnnouncementCategory category,
                   AnnouncementListener listener) throws RemoteException;
    void unsubscribe(String token, AnnouncementCategory category,
                     AnnouncementListener listener) throws RemoteException;

    List<AnnouncementCategory> getSubscribedCategories(String token) throws RemoteException;
}
