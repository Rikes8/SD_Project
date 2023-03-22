package src.StorageBarrels;

import src.SearchModule.ServerInterface;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.net.*;
import java.io.*;

public class StorageBarrels extends UnicastRemoteObject implements BarrelsInterface {

    StorageBarrels() throws RemoteException {
        super();
    }

    //Func RMI Callback to send info to searchModule and receive sth
    public String ShareInfoToBarrel(String s) throws RemoteException {
        //System.out.println(">> " + s);

        return "RECEBER INFO DO BARREL";
    }

    public static void main(String args[]) {

        // usage: java HelloClient username
        //System.getProperties().put("java.security.policy", "policy.all");
        //System.setSecurityManager(new RMISecurityManager());

        String ClientInput;
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        try {
            String nome = "localhost";
            ServerInterface h = (ServerInterface) LocateRegistry.getRegistry(8000).lookup("BARREL");
            StorageBarrels b = new StorageBarrels();
            h.subscribe_barrel(nome, (BarrelsInterface) b);
            System.out.println("Barrel sent subscription to server");
            while (true) {
                System.out.print("> ");
                ClientInput = reader.readLine();
                h.ShareInfoToServer(nome, ClientInput);
            }
        } catch (Exception e) {
            System.out.println("Exception in main: " + e);
        }
    }

}