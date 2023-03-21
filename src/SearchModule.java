package src;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;

public class SearchModule extends UnicastRemoteObject implements ClientServer {

    private static final long serialVersionUID = 1L;

    public SearchModule() throws RemoteException {
        super();
    }

    public String sayHello(String s) throws RemoteException {
        System.out.println("Servidor recebeu: " + s);

        return "Hello, World!";
    }

    // =========================================================
    public static void main(String args[]) {

        try {
            SearchModule h = new SearchModule();
            Registry r = LocateRegistry.createRegistry(7000);
            r.rebind("benfica", h);
            System.out.println("SearchModule is ready!"); //FIXME: SearchModel iniciou
        } catch (RemoteException re) {
            System.out.println("Exception in HelloImpl.main: " + re);
        }
    }

}
