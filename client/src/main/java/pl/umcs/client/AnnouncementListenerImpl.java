package pl.umcs.client;

import pl.umcs.AnnouncementListener;
import pl.umcs.data.Announcement;
import pl.umcs.data.AnnouncementCategory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


public class AnnouncementListenerImpl extends UnicastRemoteObject
        implements AnnouncementListener {

    private final AnnouncementCategory category;

    public AnnouncementListenerImpl(AnnouncementCategory category)
            throws RemoteException {
        super();
        this.category = category;
    }

    public AnnouncementCategory getCategory() {
        return category;
    }

    @Override
    public void onNewAnnouncement(Announcement announcement) throws RemoteException {
        System.out.println();
        System.out.println("╔══  NEW ANNOUNCEMENT [" + category + "] ══");
        System.out.println("║  Title  : " + announcement.getTitle());
        System.out.println("║  Author : " + announcement.getAuthorUsername());
        System.out.println("║  ID     : " + announcement.getId());
        System.out.println("╚" + "═".repeat(50));
        System.out.print("Choice: ");
    }
}