package src;
import java.rmi.*;
public interface ClientServer extends Remote {
    public String exchangeInfo(String s) throws java.rmi.RemoteException;
}



