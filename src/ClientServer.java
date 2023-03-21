package src;
import java.rmi.*;
public interface ClientServer extends Remote {
    public String sayHello(String s) throws java.rmi.RemoteException;
}



