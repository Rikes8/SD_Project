package src.Client;
import java.rmi.*;
public interface ClientInterface extends Remote {
    public void ShareInfo(String s) throws java.rmi.RemoteException;
}

