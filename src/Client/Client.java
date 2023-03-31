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
        this.id = -1; //id
    }

    //Func RMI Callback to send info to searchModule and receive sth
    //public String ShareInfoToClient(String s) throws RemoteException {
    //    System.out.println(">> " + s);
    //    return "";
    //}

    public void print_on_client(String s) throws RemoteException {
        //To print in client the message recieved by RMI Callback
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

            //return is the id of client
            c.id = h.subscribe_client(nome, (ClientInterface) c);
            System.out.println("Client sent subscription to server");

            //catchs the Crtl+C signal
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        //say to server that client with this id ended
                        h.ShareInfoToServer(c.id,"-1c");
                    } catch (RemoteException e) {
                    }
                    System.out.println("Client ending...");
                }
            });

            while (true) {
                System.out.print("> ");
                ClientInput = reader.readLine();
                //send client input by RMI Callback and print what is returned
                System.out.println(h.ShareInfoToServer(c.id, ClientInput));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("SearchModule is not activated, try again later!");
        }
    }
}