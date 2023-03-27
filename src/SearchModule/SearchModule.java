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

    static ArrayList<ClientInterface> client = new ArrayList<>();
    static ArrayList<String> nomes = new ArrayList<>();
    static ArrayList<BarrelsInterface> barrels = new ArrayList<>();
    //static ArrayList<String> IP_barrels = new ArrayList<>();
    static ArrayList<DownloaderInterface> downloaders = new ArrayList<>();
    //static ArrayList<String> IP_downloaders = new ArrayList<>();

    static HashMap<Integer,String> IpBarrels = new HashMap<>();
    static HashMap<Integer,String> IpDownloaders = new HashMap<>();

    public SearchModule() throws RemoteException {
        super();
    }

    public int subscribe_barrel(String name, BarrelsInterface b) throws RemoteException, ServerNotActiveException {
        System.out.println("Barrel Subscribing " + name);
        System.out.print("> ");
        barrels.add(b);

        int key = IpBarrels.size();
        IpBarrels.put(key,StorageBarrels.getClientHost());
        return key;
    }

    public int subscribe_downloader(String name, DownloaderInterface downloader) throws RemoteException, ServerNotActiveException {
        System.out.println("Barrel Subscribing " + name);
        System.out.print("> ");
        downloaders.add(downloader);

        int key = IpDownloaders.size();
        IpDownloaders.put(key,Downloader.getClientHost());
        return key;
    }

    public void subscribe(String name, ClientInterface c) throws RemoteException {
        System.out.println("Client Subscribing " + name);
        System.out.print("> ");
        client.add(c);
        nomes.add(name);
    }

    public String ShareInfoToServer(int id, String s) throws RemoteException {
        String message = "";
        String message_aux = "";
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

            message = "---Server Statistics---\n"+
                    "Active Downloaders("+ IpDownloaders.size()+"):\n"+
                    message_aux;

            for (Map.Entry<Integer, String> entry : IpDownloaders.entrySet()) {
                //System.out.println(entry.getKey() + " -> " + entry.getValue());
                message = message + "ID: 1." + entry.getKey() + "     "+ entry.getValue() +"\n";
            }

            message = message + "Active Barrels("+ IpBarrels.size()+"):\n";

            for (Map.Entry<Integer, String> entry : IpBarrels.entrySet()) {
                //System.out.println(entry.getKey() + " -> " + entry.getValue());
                message = message + "ID: 2." + entry.getKey() + "     "+ entry.getValue() +"\n";
            }

        }else if(str[0].equals("-1b")){
            for (Map.Entry<Integer, String> entry : IpBarrels.entrySet()) {
                //System.out.println(entry.getKey() + " -> " + entry.getValue());
                if (entry.getKey() == id){
                    IpBarrels.remove(id);
                }

                message = message + "ID: 2." + entry.getKey() + "     "+ entry.getValue() +"\n";
            }

        }else if (str[0].equals("-1d")) {
            for (Map.Entry<Integer, String> entry : IpDownloaders.entrySet()) {
                //System.out.println(entry.getKey() + " -> " + entry.getValue());
                if (entry.getKey() == id){
                    IpDownloaders.remove(id);
                }
                message = message + "ID: 1." + entry.getKey() + "     "+ entry.getValue() +"\n";
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
            //User user = new User();
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
