package src.StorageBarrels;

import src.Classes.Word;
import src.SearchModule.ServerInterface;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import src.Classes.*;
import sun.misc.Signal;

//TODO:=====================ALERT!!!===============================
//downloader avisa que vai enviar pacote comn bytes, barrelsavisam quer receberem e depois acks

public class StorageBarrels extends  UnicastRemoteObject implements BarrelsInterface, Serializable{

    private static String MULTICAST_ADDRESS = "224.3.2.1";
    private static int PORT = 4321;
    private int id;
    //private String file = "src/StorageBarrels/file.obj";

    //static HashMap<Integer,User> usersBackup = new HashMap<Integer,User>();
    private ArrayList<User> users = new ArrayList<>();


    //FIXME: Penso que não devia estar static
    private static  ConcurrentHashMap<Word,HashSet<Url>> index = new  ConcurrentHashMap<Word,HashSet<Url>>();

    public StorageBarrels() throws RemoteException {
        super();
        this.id = -1;
    }

    //FIXME: RMI Callback -> INPUT QUE VEM DO CLIENTE E QUE REQUERE IR BUSCAR INFO AO BARREL
    public String ShareInfoToBarrel(String s) throws RemoteException {


        String[] str= s.split(" ");
        if (str[0].equals("register")) {
            //File file = new File("src/StorageBarrels/file.obj");
            String file = "src/StorageBarrels/file.obj";
            User u = new User(str[1], str[2], Integer.parseInt(str[3]));
            try {
                FileOutputStream fos = new FileOutputStream(file);
                ObjectOutputStream oos = new ObjectOutputStream(fos);

                FileInputStream fin = new FileInputStream(file);
                ObjectInputStream in = new ObjectInputStream(fin);

                if (file.length() != 0) {
                    while (true) {
                        try {
                            User in_file = (User) in.readObject();
                            users.add(in_file);
                        } catch (Exception e) {
                            break; // eof
                        }
                    }

                }

                users.add(u);

                /*for (User usr : users) {
                    System.out.println(usr);;
                }*/

                for (User usr : users) {
                    oos.writeObject(usr);
                }

                oos.close();
                in.close();
                return "Client Registed";
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }else if (str[0].equals("login")) {
            String file = "src/StorageBarrels/file.obj";
            try {
                FileInputStream ffin = new FileInputStream(file);
                ObjectInputStream iin = new ObjectInputStream(ffin);

                if (file.length() != 0) {
                    while (true) {
                        try {
                            User iin_file = (User) iin.readObject();
                            users.add(iin_file);
                        } catch (Exception e) {
                            break; // eof
                        }
                    }

                }
                iin.close();

                for (User usr : users) {
                    System.out.println(usr);;
                }

                for (User usr : users) {

                    if (usr.getName().equals(str[1]) && usr.getPassword().equals(str[2]) /*&& usr.getId() == Integer.parseInt(str[3])*/){

                        users.clear();
                        return "Client Logged";
                    }
                }



                return "Wrong Client Credential";
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        //FIXME:SO PARA TESTAR
        String[] teste = {"teste" , "http://uc.pt" , "teste2", "http://sapo.pt"};
        if (s.equals(teste[0])){
            //System.out.println("in");
            return teste[1];
        }
        if (s.equals(teste[2])){
            //System.out.println("in");
            return teste[3];
        }

        return "RECEBER INFO DO BARREL";
    }

    //FIXME: Testes.Não que não devia estar static.
   public static void printMap( ConcurrentHashMap<Word, HashSet<Url>> index)
    {
        Set<Word> key = index.keySet();
        Set<Url> url;
        for(Word e: key){
            url = index.get(e);
            System.out.println(e.getWord() + e.getCount());
            for(Url u: url){
                System.out.println(u.getName());
            }
        }

    }
    //FIXME: Testes.Não devia estar static.
    public static void AddIndexInfo(Url url, ConcurrentHashMap<Word,HashSet<Url>> index,List<String> words) {
        Set<Word> key = index.keySet();
        if(key.isEmpty()) {
            for (String s : words) {
                Word w = new Word(s);
                HashSet<Url> w_set = new HashSet<Url>();
                w_set.add(url);
                index.put(w, w_set);
            }
        }
        else {
            for (Word k : key) {
                for (String s : words) {

                    if (k.getWord().equals(s)) {
                        index.get(k).add(url);
                    } else {
                        Word w = new Word(s);
                        HashSet<Url> w_set = new HashSet<Url>();
                        w_set.add(url);
                        index.put(w, w_set);
                    }
                }
            }
        }
        printMap(index);
    }

    public static void main(String args[]) throws IOException, NotBoundException, ServerNotActiveException {
        // usage: java HelloClient username
        //System.getProperties().put("java.security.policy", "policy.all");
        //System.setSecurityManager(new RMISecurityManager());




        //FIXME: Multicast thread

        String nome = "localhost";
        ServerInterface h = (ServerInterface) LocateRegistry.getRegistry(8000).lookup("BARREL");
        StorageBarrels b = new StorageBarrels();
        b.id = h.subscribe_barrel(nome, (BarrelsInterface) b);
        System.out.println("Barrel sent subscription to server");
        //h.ShareInfoToServer(b.id,"+1b");

        MulticastSocket multicast_socket = new MulticastSocket(PORT);
        InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
        multicast_socket.joinGroup(group);
        Thread multicast = new Thread(() -> {
            try {
                while (true) {
                    //recieve handshake
                    byte[] buffer = new byte[1024]; //FIXME: PROBLEMA AQUI!!!! tamanho
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    try {
                        multicast_socket.receive(packet);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    System.out.println("Received handshake from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " with message:");
                    String size = new String(packet.getData(), 0, packet.getLength());
                    System.out.println(size);


                    byte[] sms_buffer = new byte[Integer.parseInt(size)];
                    DatagramPacket sms_packet = new DatagramPacket(sms_buffer, sms_buffer.length);
                    try {
                        multicast_socket.receive(sms_packet);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("Received message packet from " + sms_packet.getAddress().getHostAddress() + ":" + sms_packet.getPort() + " with message:");
                    String sms = new String(sms_packet.getData(), 0, sms_packet.getLength());


                   //FIXME: Meter isto numa função
                    String words = null;String garbage = null;String name = null;
                    String title = null;String quote = null;
                    List<String> url_apointed = new ArrayList<>();
                    List<String> word = new ArrayList<>();

                    StringTokenizer tokens = new StringTokenizer(sms);
                    while (tokens.hasMoreElements()) {
                        words = tokens.nextToken();
                        if (words.equals("url")) {
                            garbage = tokens.nextToken();
                            name = tokens.nextToken();
                        }
                        if(words.equals("title")) {
                            garbage = tokens.nextToken();
                            title = tokens.nextToken();
                        }
                        if(words.equals("quote")){
                            garbage = tokens.nextToken();
                            quote = tokens.nextToken();
                        }
                        if(words.equals("word")){
                            garbage = tokens.nextToken();
                            words = tokens.nextToken();
                            if(!word.contains(words))
                                word.add(words);
                        }
                        if(words.equals("url_ap")){
                            garbage = tokens.nextToken();
                            words = tokens.nextToken();
                            if(!url_apointed.contains(words))
                                url_apointed.add(words);
                        }
                    }
                    Url url = new Url(name,title,quote,url_apointed);
                    AddIndexInfo(url,index,word);
                    //////////////////////////////////////////////

                    /*
                    //send ack
                    byte[] ack = new byte[1];
                    ack[0] = 1;
                    DatagramPacket ack_packet = new DatagramPacket(ack, ack.length, group, PORT);
                    try {
                        System.out.println("entrei!!!");
                        multicast_socket.send(ack_packet);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }*/


                    //FIXME: Tratar da mensagem e meter para o hashmap
                }

            } finally {
                multicast_socket.close();
            }
        });
        multicast.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                //guardar info no ficheiro

                try {
                    h.ShareInfoToServer(b.id,"-1b");
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Storage Barrel ending...");
            }
        });


        //FIXME: RMI com SearchModule
        String ClientInput;
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        while (true){
            try {

                System.out.print("> ");
                ClientInput = reader.readLine();
                h.ShareInfoToServer(b.id, ClientInput);
            } catch (Exception e) {
                System.out.println("Exception in main: " + e);
            }
        }





    }

}

