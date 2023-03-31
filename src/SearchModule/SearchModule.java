package src.SearchModule;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.util.*;


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

    private int counter;

    private int client_id;
    private int flag = client_id;

    static ArrayList<ClientInterface> clients = new ArrayList<>();
    static HashMap<Integer,String> IpClients = new HashMap<>();



    static HashMap<Integer,String> statistics = new HashMap<>();

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

        printStats(IpDownloaders,IpBarrels,statistics);

        return key;
    }

    public int subscribe_downloader(String name, DownloaderInterface downloader) throws RemoteException, ServerNotActiveException {
        System.out.println("Barrel Subscribing " + name);
        System.out.print("> ");
        downloaders.add(downloader);

        int key = IpDownloaders.size();
        IpDownloaders.put(key,Downloader.getClientHost());

        printStats(IpDownloaders,IpBarrels,statistics);

        return key;
    }

    public int subscribe_client(String name, ClientInterface client) throws RemoteException, ServerNotActiveException {
        System.out.println("Client Subscribing " + name);
        System.out.print("> ");
        clients.add(client);

        int key = IpClients.size();
        IpClients.put(key, Client.getClientHost());
        statistics.put(key, "false");


        return key;
    }

    public void printStats(HashMap<Integer,String> IpDownloaders, HashMap<Integer,String> IpBarrels, HashMap<Integer,String> statistics) throws RemoteException {
        String message = "---Server Statistics---\n";

        message = message +"Active Downloaders("+IpDownloaders.size() +"):\n";

        for (Map.Entry<Integer, String> entry : IpDownloaders.entrySet()) {
            message = message + "ID: 1." + entry.getKey() + "     "+ entry.getValue() +"\n";
        }

        message = message + "Active Barrels("+IpBarrels.size()+"):\n";

        for (Map.Entry<Integer, String> entry : IpBarrels.entrySet()) {
            message = message + "ID: 2." + entry.getKey() + "     "+ entry.getValue() +"\n";
        }


        for (Map.Entry<Integer, String> entry : statistics.entrySet()) {
            if(entry.getValue().equals("true")){
                //System.out.println("entrei");
                clients.get(entry.getKey()).print_on_client(message);
            }
        }

    }


    public String ShareInfoToServer(int id, String s) throws RemoteException {
        String message = "";

        String[] str= s.split(" ");
        String username = "";
        String password = "";
        String send = "";


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

        }else if(str[0].equals("search") || str[0].equals("conn")){

            if (str[0].equals("conn")){
                for (Map.Entry<Integer, String> entry : IpClients.entrySet()) {
                    //System.out.println(entry.getKey() + " -> " + entry.getValue());
                    if (entry.getKey() == id){
                        IpClients.remove(id);
                    }
                }
                clients.remove(id);
            }

            System.out.println("entrei");
            String mm = "";
            for (String value : str) {
                mm = mm + value +" ";
            }
            System.out.println(mm);

            if (counter < barrels.size()){
                message = barrels.get(counter).ShareInfoToBarrel(mm);
                counter++;
            }else{
                counter = 0;
                message = barrels.get(counter).ShareInfoToBarrel(mm);
                counter ++;
            }




        }else if(str[0].equals("stats")){

            for (Map.Entry<Integer, String> entry : statistics.entrySet()) {
                if(entry.getValue().equals("false")){
                    statistics.replace(id, "true");
                }else{
                    statistics.replace(id, "false");
                }

            }


        }else if(str[0].equals("-1b")){
            for (Map.Entry<Integer, String> entry : IpBarrels.entrySet()) {
                //System.out.println(entry.getKey() + " -> " + entry.getValue());
                if (entry.getKey() == id){
                    IpBarrels.remove(id);

                }
            }

            barrels.remove(id);
            printStats(IpDownloaders,IpBarrels,statistics);



        }else if (str[0].equals("-1d")) {
            for (Map.Entry<Integer, String> entry : IpDownloaders.entrySet()) {
                //System.out.println(entry.getKey() + " -> " + entry.getValue());
                if (entry.getKey() == id) {
                    IpDownloaders.remove(id);
                }
            }

            downloaders.remove(id);
            printStats(IpDownloaders, IpBarrels,statistics);

        }else if(str[0].equals("-1c")){
            for (Map.Entry<Integer, String> entry : IpClients.entrySet()) {
                //System.out.println(entry.getKey() + " -> " + entry.getValue());
                if (entry.getKey() == id){
                    IpClients.remove(id);
                }
            }
            clients.remove(id);

        } else if (str[0].equals("register")) {
            username = str[1];
            password = str[2];
            send = "register" + " " + username + " " + password + " "+ client_id;
            //Pode ser substituido por data

            barrels.get(0).ShareInfoToBarrel(send);

        }else if (str[0].equals("login")) {
            username = str[1];
            password = str[2];
            send = "login" + " " + username + " " + password + " "+ client_id;
            //Pode ser substituido por data


            //FIXME: recebe mensagem a dizer que o login foi aceite.
            message = barrels.get(0).ShareInfoToBarrel(send);

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
