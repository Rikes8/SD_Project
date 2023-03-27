package src.SearchModule;

import src.Client.ClientInterface;
import src.Downloader.DownloaderInterface;
import src.StorageBarrels.BarrelsInterface;
import java.rmi.*;
import java.rmi.server.ServerNotActiveException;

public interface ServerInterface extends Remote {
    public String ShareInfoToServer(int id, String s) throws java.rmi.RemoteException;
    public void subscribe(String name, ClientInterface client) throws RemoteException;
    public int subscribe_barrel(String name, BarrelsInterface barrel) throws RemoteException, ServerNotActiveException;
    public int subscribe_downloader(String name, DownloaderInterface downloader) throws RemoteException, ServerNotActiveException;

}