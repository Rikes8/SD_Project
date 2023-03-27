package src.Client;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.*;
import java.io.*;
import src.SearchModule.ServerInterface;

public class Client extends UnicastRemoteObject implements ClientInterface {

    Client() throws RemoteException {
        super();
    }

    //Func RMI Callback to send info to searchModule and receive sth
    //public String ShareInfoToClient(String s) throws RemoteException {
    //    System.out.println(">> " + s);
    //    return "";
    //}

    public static void main(String args[]) {

        // usage: java HelloClient username
        //System.getProperties().put("java.security.policy", "policy.all");
        //System.setSecurityManager(new RMISecurityManager());

        String ClientInput;
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        try {
            String nome = "localhost";
            ServerInterface h = (ServerInterface) LocateRegistry.getRegistry(7000).lookup("XPTO"); //r.lookup("XPTO");
            Client c = new Client();
            h.subscribe(nome, (ClientInterface) c);
            System.out.println("Client sent subscription to server");
            while (true) {
                System.out.print("> ");
                ClientInput = reader.readLine();
                System.out.println(h.ShareInfoToServer(-1, ClientInput));
            }
        } catch (Exception e) {
            System.out.println("Exception in main: " + e);
        }
    }
}