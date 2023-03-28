package src.SearchModule;

import java.io.*;
import java.lang.reflect.Proxy;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import src.Client.Client;
import src.Client.ClientInterface;
import src.Downloader.Downloader;
import src.Downloader.DownloaderInterface;
import src.StorageBarrels.BarrelsInterface;
import src.StorageBarrels.StorageBarrels;

//url       mete url
//admin     consola administrador
//pesquisa  pesquisa de palavras

public class SearchModule extends UnicastRemoteObject implements ServerInterface{

    private static int serversocket = 6000;

    private static final long serialVersionUID = 1L;

    private int flag = 0;

    static ArrayList<ClientInterface> clients = new ArrayList<>();
    static HashMap<Integer,String> IpClients = new HashMap<>();

    static ArrayList<BarrelsInterface> barrels = new ArrayList<>();
    static HashMap<Integer,String> IpBarrels = new HashMap<>();

    static ArrayList<DownloaderInterface> downloaders = new ArrayList<>();
    static HashMap<Integer,String> IpDownloaders = new HashMap<>();



    public SearchModule() throws RemoteException {
        super();
    }

    public int subscribe_barrel(String name, BarrelsInterface barrel) throws RemoteException, ServerNotActiveException {
        System.out.println("Barrel Subscribing " + name);
        System.out.print("> ");
        barrels.add(barrel);

        int key = IpBarrels.size();
        IpBarrels.put(key,StorageBarrels.getClientHost());

        if (flag == 1){
            printStats(IpDownloaders,IpBarrels);
        }

        return key;
    }

    public int subscribe_downloader(String name, DownloaderInterface downloader) throws RemoteException, ServerNotActiveException {
        System.out.println("Barrel Subscribing " + name);
        System.out.print("> ");
        downloaders.add(downloader);

        int key = IpDownloaders.size();
        IpDownloaders.put(key,Downloader.getClientHost());

        if (flag == 1){
            printStats(IpDownloaders,IpBarrels);
        }


        return key;
    }

    public int subscribe_client(String name, ClientInterface client) throws RemoteException, ServerNotActiveException {
        System.out.println("Client Subscribing " + name);
        System.out.print("> ");
        clients.add(client);

        int key = IpClients.size();
        IpClients.put(key, Client.getClientHost());

        return key;
    }

    public void printStats(HashMap<Integer,String> IpDownloaders, HashMap<Integer,String> IpBarrels) throws RemoteException {
        String message = "---Server Statistics---\n";

        message = message +"Active Downloaders("+IpDownloaders.size() +"):\n";

        for (Map.Entry<Integer, String> entry : IpDownloaders.entrySet()) {
            //System.out.println(entry.getKey() + " -> " + entry.getValue());
            message = message + "ID: 1." + entry.getKey() + "     "+ entry.getValue() +"\n";
        }

        message = message + "Active Barrels("+IpBarrels.size()+"):\n";

        for (Map.Entry<Integer, String> entry : IpBarrels.entrySet()) {
            //System.out.println(entry.getKey() + " -> " + entry.getValue());
            message = message + "ID: 2." + entry.getKey() + "     "+ entry.getValue() +"\n";
        }


        for (int i = 0; i < clients.size(); i++) {
            clients.get(i).print_on_client(message);
        }
    }


    public String ShareInfoToServer(int id, String s) throws RemoteException {
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

        }else if(str[0].equals("search")){

            message = barrels.get(0).ShareInfoToBarrel(str[1]);

        }else if(str[0].equals("stats")){

            if (flag == 0){
                flag = 1;
                printStats(IpDownloaders,IpBarrels);
            }else{
                flag = 0;
            }

        }else if(str[0].equals("-1b")){
            for (Map.Entry<Integer, String> entry : IpBarrels.entrySet()) {
                //System.out.println(entry.getKey() + " -> " + entry.getValue());
                if (entry.getKey() == id){
                    IpBarrels.remove(id);
                }
            }
            if (flag == 1){
                printStats(IpDownloaders,IpBarrels);
            }


        }else if (str[0].equals("-1d")) {
            for (Map.Entry<Integer, String> entry : IpDownloaders.entrySet()) {
                //System.out.println(entry.getKey() + " -> " + entry.getValue());
                if (entry.getKey() == id) {
                    IpDownloaders.remove(id);
                }
            }
            if (flag == 1) {
                printStats(IpDownloaders, IpBarrels);
            }
        }


        return message;
    }



    // =======================================================

    public static void main(String args[]) {
        String a;

        //System.getProperties().put("java.security.policy", "policy.all");
        //System.setSecurityManager(new RMISecurityManager());

        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        try {

            SearchModule h = new SearchModule();

            Registry r = LocateRegistry.createRegistry(7000);
            r.rebind("XPTO", h);

            Registry sto = LocateRegistry.createRegistry(8000);
            sto.rebind("BARREL", h);

            Registry downloader = LocateRegistry.createRegistry(9000);
            downloader.rebind("DOWNLOADER", h);


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
