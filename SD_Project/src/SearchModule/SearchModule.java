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


public class SearchModule extends UnicastRemoteObject implements ServerInterface{

    private static int serversocket = 6000;
    private static final long serialVersionUID = 1L;

    private int counter;
    private int client_id;
    private int flag = client_id;

    //Store informations about clients
    static ArrayList<ClientInterface> clients = new ArrayList<>();
    static HashMap<Integer,String> IpClients = new HashMap<>();
    static HashMap<Integer,String> statistics = new HashMap<>();

    //Store informations about barrels
    static ArrayList<BarrelsInterface> barrels = new ArrayList<>();
    static HashMap<Integer,String> IpBarrels = new HashMap<>();

    //Store informations about downloaders
    static ArrayList<DownloaderInterface> downloaders = new ArrayList<>();
    static HashMap<Integer,String> IpDownloaders = new HashMap<>();


    public SearchModule() throws RemoteException {
        super();
    }

    //Method of ServerInterface that permits to recieve information about the new barrel that entered and to store it
    public int subscribe_barrel(String name, BarrelsInterface barrel) throws RemoteException, ServerNotActiveException {
        System.out.println("Barrel Subscribing " + name);
        System.out.print("> ");
        barrels.add(barrel);

        //key equals to the next position available in the hashmap key's
        int key = IpBarrels.size();
        IpBarrels.put(key,StorageBarrels.getClientHost());

        //if client requested to know if new barrel enters, give them the information
        printStats(IpDownloaders,IpBarrels,statistics);
        return key;
    }

    //Method of ServerInterface that permits to recieve information about the new downloader that entered and to store it
    public int subscribe_downloader(String name, DownloaderInterface downloader) throws RemoteException, ServerNotActiveException {
        System.out.println("Barrel Subscribing " + name);
        System.out.print("> ");
        downloaders.add(downloader);

        //key equals to the next position available in the hashmap key's
        int key = IpDownloaders.size();
        IpDownloaders.put(key,Downloader.getClientHost());

        //if client requested to know if new downloader enters, give them the information
        printStats(IpDownloaders,IpBarrels,statistics);
        return key;
    }

    //Method of ServerInterface
    public int subscribe_client(String name, ClientInterface client) throws RemoteException, ServerNotActiveException {
        System.out.println("Client Subscribing " + name);
        System.out.print("> ");
        clients.add(client);

        //key equals to the next position available in the hashmap key's
        int key = IpClients.size();
        IpClients.put(key, Client.getClientHost());

        //new client entered, the statistics for him are set as false(not shown)
        statistics.put(key, "false");
        return key;
    }

    //to print downloaders and storageBarrels that are active
    public void printStats(HashMap<Integer,String> IpDownloaders, HashMap<Integer,String> IpBarrels, HashMap<Integer,String> statistics) throws RemoteException {
        String message = "---Server Statistics---\n";

        message = message +"Active Downloaders("+IpDownloaders.size() +"):\n";

        //add to message all active downloaders
        for (Map.Entry<Integer, String> entry : IpDownloaders.entrySet()) {
            message = message + "ID: 1." + entry.getKey() + "     "+ entry.getValue() +"\n";
        }

        message = message + "Active Barrels("+IpBarrels.size()+"):\n";

        //add to message all active barrels
        for (Map.Entry<Integer, String> entry : IpBarrels.entrySet()) {
            message = message + "ID: 2." + entry.getKey() + "     "+ entry.getValue() +"\n";
        }

        //only send the message for the clients that have requested to recieve if changes happens
        for (Map.Entry<Integer, String> entry : statistics.entrySet()) {
            //if == "true" represents that client already activated the feature to recieve changes.
            if(entry.getValue().equals("true")){
                clients.get(entry.getKey()).print_on_client(message);
            }
        }


    }


    public String ShareInfoToServer(int id, String s) throws RemoteException {
        String message = "";
        String username = "";
        String password = "";
        String send = "";

        //split input by " "
        String[] str= s.split(" ");

        //index url in the Queue
        if (str[0].equals("index")){

            //create socket TCP
            try (Socket socket = new Socket("localhost", serversocket)) {

                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                str[1] = "toQueue " + str[1];

                //send to the Queue
                out.writeUTF(str[1]);

            } catch (UnknownHostException e) {
                System.out.println("Sock:" + e.getMessage());
            } catch (EOFException e) {
                System.out.println("EOF:" + e.getMessage());
            } catch (IOException e) {
                System.out.println("IO:" + e.getMessage());
            }

            //to return (client gets this message)
            message = "link indexado!";

        }else if(str[0].equals("search") || str[0].equals("conn") || str[0].equals("statsv2")|| str[0].equals("plus")){

            /*if (str[0].equals("conn")){
                for (Map.Entry<Integer, String> entry : IpClients.entrySet()) {
                    if (entry.getKey() == id){

                    }
                }
            }*/
            //FIXME: falta verificar se logado

            String mm = "";
            for (String value : str) {
                mm = mm + value +" ";
            }

            //send message to active barrels in sequence
            if (counter < barrels.size()){
                message = barrels.get(counter).ShareInfoToBarrel(mm);
                counter++;
            }else{
                counter = 0;
                message = barrels.get(counter).ShareInfoToBarrel(mm);
                counter ++;
            }

        }else if(str[0].equals("stats")){

            //atualize the state of statistics
            for (Map.Entry<Integer, String> entry : statistics.entrySet()) {
                if(entry.getValue().equals("false")){
                    statistics.replace(id, "true");
                }else{
                    statistics.replace(id, "false");
                }
            }


        }else if(str[0].equals("-1b")){ //barrels ended
            for (Map.Entry<Integer, String> entry : IpBarrels.entrySet()) {
                if (entry.getKey() == id){
                    //remove barrel of active's list
                    IpBarrels.remove(id);
                }
            }
            //remove barrel of active's list
            barrels.remove(id);
            //print stats
            printStats(IpDownloaders,IpBarrels,statistics);

        }else if (str[0].equals("-1d")) { //downloader ended
            for (Map.Entry<Integer, String> entry : IpDownloaders.entrySet()) {
                if (entry.getKey() == id) {
                    //remove downloader of active's list
                    IpDownloaders.remove(id);
                }
            }
            //remove downloader of active's list
            downloaders.remove(id);
            //print stats
            printStats(IpDownloaders, IpBarrels,statistics);

        }else if(str[0].equals("-1c")){ //client ended
            for (Map.Entry<Integer, String> entry : IpClients.entrySet()) {
                if (entry.getKey() == id){
                    //remove client of active's list
                    IpClients.remove(id);
                }
            }
            //remove downloader of active's list
            clients.remove(id);
            statistics.remove(id);

        } else if (str[0].equals("register")) {
            username = str[1];
            password = str[2];
            send = "register" + " " + username + " " + password + " "+ client_id;

            //send to barrel the username + password to regist
            if (counter < barrels.size()){
                message = barrels.get(counter).ShareInfoToBarrel(send);
                counter++;
            }else{
                counter = 0;
                message = barrels.get(counter).ShareInfoToBarrel(send);
                counter ++;
            }

        }else if (str[0].equals("login")) {
            username = str[1];
            password = str[2];
            send = "login" + " " + username + " " + password + " "+ client_id;

            //send to barrel the username + password to do loggin
            if (counter < barrels.size()){
                message = barrels.get(counter).ShareInfoToBarrel(send);
                counter++;
            }else{
                counter = 0;
                message = barrels.get(counter).ShareInfoToBarrel(send);
                counter ++;
            }
        }

        //return of message ->sent to client
        return message;
    }


    public static void main(String args[]) {
        String a;

        //System.getProperties().put("java.security.policy", "policy.all");
        //System.setSecurityManager(new RMISecurityManager());

        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        try {
            SearchModule h = new SearchModule();

            //Registry client
            Registry r = LocateRegistry.createRegistry(7000);
            r.rebind("XPTO", h);

            //Registry barrel
            Registry sto = LocateRegistry.createRegistry(8000);
            sto.rebind("BARREL", h);

            //Registry downloader
            Registry downloader = LocateRegistry.createRegistry(9000);
            downloader.rebind("DOWNLOADER", h);

            //Registry webserver

            Registry webserver = LocateRegistry.createRegistry(4000);
            webserver.rebind("WEBSERVER", h);


            System.out.println("Hello Server ready.");
            while (true) {
                System.out.print("> ");
                a = reader.readLine();
            }

        } catch (Exception re) {
            System.out.println("Exception in SearchModule: " + re);
        }
    }
}
