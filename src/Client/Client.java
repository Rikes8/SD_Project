package src.Client;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.*;
import java.io.*;
import src.SearchModule.ServerInterface;

public class Client extends UnicastRemoteObject implements ClientInterface {

    private int id;

    Client() throws RemoteException {
        super();
        this.id = -1;
    }

    //Func RMI Callback to send info to searchModule and receive sth
    //public String ShareInfoToClient(String s) throws RemoteException {
    //    System.out.println(">> " + s);
    //    return "";
    //}

    public void print_on_client(String s) throws RemoteException {
        System.out.println("Updated stats:\n" + s);
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
            ServerInterface h = (ServerInterface) LocateRegistry.getRegistry(7000).lookup("XPTO"); //r.lookup("XPTO");
            Client c = new Client();
            c.id = h.subscribe_client(nome, (ClientInterface) c);
            //h.ShareInfoToServer(c.id, "+1c");
            System.out.println("Client sent subscription to server");

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    //guardar info no ficheiro

                    try {
                        h.ShareInfoToServer(c.id,"-1c");
                    } catch (RemoteException e) {
                    }
                    System.out.println("Client ending...");
                }
            });

            while (true) {
                System.out.print("> ");
                ClientInput = reader.readLine();
                System.out.println(h.ShareInfoToServer(c.id, ClientInput));
            }
        } catch (Exception e) {
            System.out.println("SearchModule is not activated, try again later!");
        }
    }
}