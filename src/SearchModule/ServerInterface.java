package src.SearchModule;

import src.Client.ClientInterface;
import src.StorageBarrels.BarrelsInterface;
import java.rmi.*;

public interface ServerInterface extends Remote {
    public String ShareInfoToServer(String name, String s) throws java.rmi.RemoteException;
    public void subscribe(String name, ClientInterface client) throws RemoteException;
    public void subscribe_barrel(String name, BarrelsInterface barrel) throws RemoteException;
}