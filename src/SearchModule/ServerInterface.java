package src.SearchModule;

import src.Client.ClientInterface;

import java.rmi.*;

public interface ServerInterface extends Remote {
    public void ShareInfo(String name, String s) throws java.rmi.RemoteException;
    public void subscribe(String name, ClientInterface client) throws RemoteException;
}