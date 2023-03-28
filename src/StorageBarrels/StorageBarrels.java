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
    private String file = "src/StorageBarrels/file.obj";

    static HashMap<Integer,User> usersBackup = new HashMap<Integer,User>();

    //FIXME: Penso que não devia estar static
    private static  ConcurrentHashMap<Word,HashSet<Url>> index = new  ConcurrentHashMap<Word,HashSet<Url>>();

    public StorageBarrels() throws RemoteException {
        super();
        this.id = -1;
    }

    //FIXME: RMI Callback -> INPUT QUE VEM DO CLIENTE E QUE REQUERE IR BUSCAR INFO AO BARREL
    public String ShareInfoToBarrel(String s) throws RemoteException {


        String[] str= s.split(" ");
        if (str[0].equals("register")){

            try{
                File f = new File(file);
                if(f.exists()){
                    if(f.length() != 0) {
                        try {
                            System.out.println("aqui");
                            FileInputStream fin = new FileInputStream(f);
                            ObjectInputStream in = new ObjectInputStream(fin);

                            FileOutputStream fout = new FileOutputStream(f);
                            ObjectOutputStream out = new ObjectOutputStream(fout);


                            try {

                                System.out.println("1");
                                //get oque estava no ficheiro
                                User usr = (User) in.readObject();
                                System.out.println("1.1");
                                usersBackup.put(usr.getId(), usr);

                                System.out.println("2");
                                //adicionar novo user ao hashmap
                                User u = new User(str[1], str[2], Integer.parseInt(str[3]));
                                usersBackup.put(u.getId(), u);

                                System.out.println("3");
                                //enviar para o ficheiro
                                out.writeObject(usersBackup);

                                System.out.println("4");
                                //teste: ler
                                User usr1 = (User) in.readObject();
                                usersBackup.put(usr1.getId(), usr1);

                                System.out.println("here");
                                for (Integer name: usersBackup.keySet()) {
                                    String key = name.toString();
                                    String value = usersBackup.get(name).toString();
                                    System.out.println(key + " " + value);
                                }


                            } catch (EOFException e) {
                                // EOF
                                System.out.println("EOF");
                            }


                            /*in.close();
                            fin.close();*/
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }  catch (NumberFormatException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }else{
                    f.createNewFile();
                }

                /*FileOutputStream file = new FileOutputStream("src/StorageBarrels/file.obj");
                ObjectOutputStream out = new ObjectOutputStream(file);

                out.writeObject(u);
                out.close();
                file.close();*/


            }catch (FileNotFoundException e) {
                System.out.println("File not found");
            }catch (IOException e) {
                System.out.println("Error writing file: " + e.getMessage());
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

