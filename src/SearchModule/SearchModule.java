package src.SearchModule;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.util.Scanner;
import java.util.*;
import src.Client.ClientInterface;


//url       mete url
//admin     consola administrador
//pesquisa  pesquisa de palavras

public class SearchModule extends UnicastRemoteObject implements ServerInterface {

    private static int serversocket = 6000;

    private static final long serialVersionUID = 1L;

    static ArrayList<ClientInterface> client = new ArrayList<>();
    static ArrayList<String> nomes = new ArrayList<>();


    public SearchModule() throws RemoteException {
        super();
    }

    public void ShareInfo(String name, String s) throws RemoteException {
        System.out.println("> " + s);

        // 1o passo - criar socket
        try (Socket socket = new Socket("localhost", serversocket)) {
            //System.out.println("SOCKET=" + s);

            // 2o passo
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // 3o passo
            try (Scanner sc = new Scanner(System.in)) {
                //while (true) {
                
                // WRITE INTO THE SOCKET
                out.writeUTF(s);

                // READ FROM SOCKET
                //String data = in.readUTF();

                // DISPLAY WHAT WAS READ
                //System.out.println("Received: " + data);
                //}
            }

        } catch (UnknownHostException e) {
            System.out.println("Sock:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
        }

        for (int i = 0; i < client.size(); i++) {
            if(!nomes.get(i).equalsIgnoreCase(name)){
                client.get(i).ShareInfo(name + ": " + s);
            }
        }

    }

    public void subscribe(String name, ClientInterface c) throws RemoteException {
        System.out.println("Subscribing " + name);
        System.out.print("> ");
        client.add(c);
        nomes.add(name);
        //client = c;
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


            System.out.println("Hello Server ready.");
            while (true) {
                System.out.print("> ");
                a = reader.readLine();
                //client.ShareInfo(a);
                //for (ClientInterface c: client) {
                //    c.ShareInfo(a);
                //}
            }
        } catch (Exception re) {
            System.out.println("Exception in HelloImpl.main: " + re);
        }
    }
}
