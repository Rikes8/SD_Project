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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import src.Classes.*;
import sun.misc.Signal;

//TODO:=====================ALERT!!!===============================
//downloader avisa que vai enviar pacote comn bytes, barrelsavisam quer receberem e depois acks


//index.get para ir buscar a palavra -> retorna arraylist de url
//array.add(URL)


public class StorageBarrels extends  UnicastRemoteObject implements BarrelsInterface, Serializable{

    private static String MULTICAST_ADDRESS = "224.3.2.1";
    private static int PORT = 4321;
    private int id;
    //private String file = "src/StorageBarrels/file.obj";


    private ArrayList<User> users = new ArrayList<>();


    //index invertido
    private static  ConcurrentHashMap<Word,HashSet<Url>> index = new  ConcurrentHashMap<Word,HashSet<Url>>();

    //guardar para saber quais os link que aponta cada site, para depois meter no hashMap os que apontam para ele
    private static ConcurrentHashMap<Url,ArrayList<String>> index_apontados = new ConcurrentHashMap<Url,ArrayList<String>>();

    private static ConcurrentHashMap<String,ArrayList<String>> index_apontados_cpy = new ConcurrentHashMap<String,ArrayList<String>>();



    //guardar info multicast (idk static)


    public StorageBarrels() throws RemoteException {
        super();
        this.id = -1;
    }

    //FIXME: RMI Callback -> INPUT QUE VEM DO CLIENTE E QUE REQUERE IR BUSCAR INFO AO BARREL
    public String ShareInfoToBarrel(String s) throws RemoteException {
        String message ="";
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

        } else if (str[0].equals("search")) {


            List<Url> to_delete = new ArrayList<>();
            List<Url> repetidos = new ArrayList<>();

            System.out.println("entrei!!!");
            for (int j = 1; j < str.length; j++){

                Set<Word> key = index.keySet();

                for (Word k : key) {
                    if (k.getWord().equalsIgnoreCase(str[j])) {
                        HashSet<Url> ul_aux = index.get(k);
                        if (repetidos.isEmpty()){
                            for (Url u: ul_aux) {
                                repetidos.add(u);

                            }
                        }else{

                            for (Url aux: repetidos) {

                                boolean flag = false;
                                for (Url u : ul_aux) {
                                    if (u.getName().equals(aux.getName())) {
                                        flag = true;
                                        break;
                                    }

                                }
                                if (!flag) {
                                    to_delete.add(aux);
                                }
                            }
                            for (Url rep: repetidos) {
                                for (Url del : to_delete){
                                    if (rep.getName().equals(del.getName())){
                                        repetidos.remove(rep);
                                    }
                                }
                            }

                        }

                    }

                }
            }


            //ordenar
            //private static ConcurrentHashMap<Url,ArrayList<String>> index_apontados = new ConcurrentHashMap<Url,ArrayList<String>>();

            ConcurrentHashMap<Url,Integer> to_sort = new ConcurrentHashMap<Url,Integer>();
            List<String> relevancia = new ArrayList<>();
            int rev ;


            for (Url u:repetidos) {
                for (Map.Entry<Url, ArrayList<String>> entry : index_apontados.entrySet()) {
                    Url url = entry.getKey();
                    relevancia = entry.getValue();
                    rev = relevancia.size();

                    if (url.getName().equalsIgnoreCase(u.getName())){
                        to_sort.put(u,rev);
                    }
                }
            }

            List<Map.Entry<Url, Integer> > list = new LinkedList<Map.Entry<Url, Integer> >(to_sort.entrySet());

            // Sort the list using lambda expression
            Collections.sort(list, Comparator.comparing(Map.Entry::getValue));

            HashMap<Url, Integer> temp = new LinkedHashMap<Url, Integer>();
            for (Map.Entry<Url, Integer> aa : list) {
                temp.put(aa.getKey(), aa.getValue());
            }


            for (Map.Entry<Url, Integer> en : temp.entrySet()) {
                message = message + "Key = " + en.getKey() + ", Value = " + en.getValue() +"\n\n";
            }






            //System.out.println(repetidos.size());

            /*for (int i = 0; i< temp.size(); i++){
                message = message + temp.get(i). +"\n\n";
            }*/



            return message;

        }else if(str[0].equals("conn")){
            //ConcurrentHashMap<String,ArrayList<String>> index_apontados_cpy = new ConcurrentHashMap<Strin
            System.out.println("entei");

            for (Map.Entry<String, ArrayList<String>> entry : index_apontados_cpy.entrySet()) {
                String url = entry.getKey();
                if (url.equalsIgnoreCase(str[1])){

                    message = message + entry.getValue() + "\n\n";
                    break;
                }

            }
            System.out.println("saia");
            return message;

        }




        return "RECEBER INFO DO BARREL";
    }

    //FIXME: Testes.Não que não devia estar static.
   public static void printMap( ConcurrentHashMap<Word, HashSet<Url>> index)
    {
        System.out.println("entrei");
        Set<Word> key = index.keySet();
        Set<Url> url;
        for(Word e: key){
            url = index.get(e);
            System.out.println(e.getWord() + e.getCount());
            for(Url u: url){
                System.out.println(u.getName());
            }
        }
        System.out.println("sai");

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
        try {
            String file_c = "src/StorageBarrels/urls.obj";
            File file = new File(file_c);

            if (file.exists()) {
                FileInputStream fin = new FileInputStream(file);
                ObjectInputStream in = new ObjectInputStream(fin);

                while (true) {
                    try {
                        ConcurrentHashMap<Word,HashSet<Url>> in_url_obj = (ConcurrentHashMap<Word,HashSet<Url>>) in.readObject();
                        index.putAll(in_url_obj);
                    } catch (Exception e) {
                        break; // eof
                    }
                }
                printMap(index);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
                    String handshake = new String(packet.getData(),0, packet.getLength());
                    String[] idSize = handshake.split(";");


                    byte[] sms_buffer = new byte[Integer.parseInt(idSize[1])];
                    DatagramPacket sms_packet = new DatagramPacket(sms_buffer, sms_buffer.length);
                    try {
                        multicast_socket.receive(sms_packet);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("Received message packet from " + sms_packet.getAddress().getHostAddress() + ":" + sms_packet.getPort() + " with message:");
                    String sms = new String(sms_packet.getData(), 0, sms_packet.getLength());

                    //System.out.println(sms);
                   //FIXME: Meter isto numa função

                    ArrayList<String> links_array = new ArrayList<>();
                    ArrayList<String> words_array = new ArrayList<>();
                    String sup_link = "";
                    String title = "";
                    String quote = "";

                    int counter_pal = 0;
                    String[] aux;
                    String[] sms_parse = sms.split(";");
                    for (String type: sms_parse){
                        aux = type.split("\\|");
                        //System.out.println(aux[0] + "---" + aux[1]);
                        if (aux[0].contains("url ")){
                            sup_link = aux[1].replace(" ", "");
                            //System.out.println(sup_link);
                        }else if(aux[0].contains("title")){
                            title = aux[1];
                            //System.out.println(title);
                        } else if (aux[0].contains("qoute")) {
                            quote = aux[1];
                            //System.out.println(quote);
                        }else if (aux[0].contains("word ")) {
                            words_array.add(aux[1].replace(" ", ""));
                            //System.out.println(aux[1]);
                        } else if (aux[0].contains("url_ap")) {
                            links_array.add(aux[1].replace(" ", ""));
                            //System.out.println(aux[1]);
                        }
                    }


                    //FIXME: CRIAR OBJETO
                    //File file = new File("src/StorageBarrels/file.obj");

                    String file = "src/StorageBarrels/urls.obj";

                    /*try {
                        FileOutputStream fos = new FileOutputStream(file);
                        ObjectOutputStream oos = new ObjectOutputStream(fos);*/

                    Url url = new Url(sup_link, title,quote);

                    /*for (int i = 0; i < words_array.size(); i++){
                        HashSet<Url> set = index.get(words_array[i]);
                    }*/

                    //TODO LA DENTRO
                    index_apontados.put(url, links_array);
                    ArrayList<String> auxlinks = new ArrayList<>();
                    for (String link : links_array) {

                        if (index_apontados_cpy.get(link) == null) {
                            auxlinks.add(url.getName());
                            index_apontados_cpy.put(link, auxlinks);
                        } else {
                            auxlinks = index_apontados_cpy.get(link);
                            if (!auxlinks.contains(url.getName())) {
                                auxlinks.add(link);
                                index_apontados_cpy.put(link, auxlinks);
                            }

                        }
                    }




                    for (Url key : index_apontados.keySet()) {
                        ArrayList<String> sH = index_apontados.get(key);
                        for(String s: sH){
                            //System.out.println(s +"  " +sup_link);
                            if(s.equals(sup_link)){
                                //System.out.println("entrei");
                                url.addAimUrls(key.getName());
                            }
                        }
                    }


                    ArrayList<String> wordsToIndex = new ArrayList<>();
                    if(words_array.size()>0){
                        for(String s: words_array){
                            if(wordsToIndex.isEmpty()){
                                wordsToIndex.add(s);
                            }
                            else if(!wordsToIndex.contains(s)){
                                wordsToIndex.add(s);
                            }
                        }
                    }

                    //System.out.println("debug1");
                    //System.out.println(wordsToIndex);

                    for(String s:wordsToIndex) {
                        Word wAux = new Word(s);

                        Set<Word> key = index.keySet();
                        boolean flag = false;
                        for (Word k : key) {
                            if (k.getWord().equals(s)) {
                                index.get(k).add(url);
                                flag = true;
                            }
                        }
                        if(flag == false){
                            HashSet<Url> ul = new HashSet<>();
                            ul.add(url);
                            index.put(wAux, ul);
                        }
                    }
                    try (DatagramSocket aSocket = new DatagramSocket()) {
                        byte [] m = idSize[0].getBytes();
                        int UDP_PORT = Integer.parseInt(idSize[1]);
                        DatagramPacket request = new DatagramPacket(m,m.length,sms_packet.getAddress(),UDP_PORT);
                        aSocket.send(request);
                    }catch (SocketException e){
                        System.out.println("Socket: " + e.getMessage());
                    }catch (IOException e){
                        System.out.println("IO: " + e.getMessage());
                    }

                    //System.out.println("debug2");
                    //printMap(index);


                        //escreve o hasmap
                        //oos.writeObject(index);

                    words_array.clear();
                    links_array.clear();
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
                    /*String file = "src/StorageBarrels/urls.obj";
                    try {
                        FileOutputStream fos = new FileOutputStream(file);
                        ObjectOutputStream oos = new ObjectOutputStream(fos);

                        FileInputStream fin = new FileInputStream(file);
                        ObjectInputStream in = new ObjectInputStream(fin);

                        if (file.length() != 0) {
                            while (true) {
                                try {
                                    ConcurrentHashMap<Url,ArrayList<String>> in_url_obj = (ConcurrentHashMap<Url,ArrayList<String>>) in.readObject();
                                    index_apontados.putAll(in_url_obj);
                                } catch (Exception e) {
                                    break; // eof
                                }
                            }
                        }

                        //escreve o hasmap
                        oos.writeObject(index_apontados);

                        oos.close();
                        in.close();

                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }*/




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

