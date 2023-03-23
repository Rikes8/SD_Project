package src.SearchModule;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.util.*;
import src.Client.ClientInterface;
import src.StorageBarrels.BarrelsInterface;

//url       mete url
//admin     consola administrador
//pesquisa  pesquisa de palavras

public class SearchModule extends UnicastRemoteObject implements ServerInterface{

    private static int serversocket = 6000;

    private static final long serialVersionUID = 1L;

    static ArrayList<ClientInterface> client = new ArrayList<>();
    static ArrayList<String> nomes = new ArrayList<>();
    static ArrayList<BarrelsInterface> barrels = new ArrayList<>();
    static ArrayList<String> nomes_barrels = new ArrayList<>();

    public SearchModule() throws RemoteException {
        super();
    }

    public void subscribe_barrel(String name, BarrelsInterface b) throws RemoteException {
        System.out.println("Barrel Subscribing " + name);
        System.out.print("> ");
        barrels.add(b);
        nomes_barrels.add(name);
    }

    public String ShareInfoToServer(String name, String s) throws RemoteException {
        String message = "";
        String[] str= s.split(" ");
        if (str[0].equals("index")){

            try (Socket socket = new Socket("localhost", serversocket)) {

                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                str[1] = "toQueue " + str[1];
                out.writeUTF(str[1]);

            } catch (UnknownHostException e) {
                System.out.println("Sock:" + e.getMessage());
            } catch (EOFException e) {
                System.out.println("EOF:" + e.getMessage());
            } catch (IOException e) {
                System.out.println("IO:" + e.getMessage());
            }
            //System.out.println("Client send: " + str[1]);

            message = "link indexado!";

        }else{
            message = barrels.get(0).ShareInfoToBarrel("");
        }


        return message;
    }

    public void subscribe(String name, ClientInterface c) throws RemoteException {
        System.out.println("Client Subscribing " + name);
        System.out.print("> ");
        client.add(c);
        nomes.add(name);
    }

    // =======================================================

    public static void main(String args[]) {
        String a;

        //System.getProperties().put("java.security.policy", "policy.all");
        //System.setSecurityManager(new RMISecurityManager());

        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        try {
            //User user = new User();
            SearchModule h = new SearchModule();

            Registry r = LocateRegistry.createRegistry(7000);
            r.rebind("XPTO", h);

            Registry sto = LocateRegistry.createRegistry(8000);
            sto.rebind("BARREL", h);


            System.out.println("Hello Server ready.");
            while (true) {
                System.out.print("> ");
                a = reader.readLine();

            }
        } catch (Exception re) {
            System.out.println("Exception in HelloImpl.main: " + re);
        }
    }
}
