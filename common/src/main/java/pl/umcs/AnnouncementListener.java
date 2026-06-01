package pl.umcs;

import pl.umcs.data.Announcement;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AnnouncementListener extends Remote {
    void onNewAnnouncement(Announcement announcement) throws RemoteException;
}
