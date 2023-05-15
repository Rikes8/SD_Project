package src.StorageBarrels;

import src.Classes.Word;
import src.SearchModule.ServerInterface;

import java.awt.image.AreaAveragingScaleFilter;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import src.Classes.*;

public class StorageBarrels extends  UnicastRemoteObject implements BarrelsInterface, Serializable{

    private static String MULTICAST_ADDRESS = "224.3.2.1";
    private static int PORT = 4321;
    private int id;

    //array of users
    private ArrayList<User> users = new ArrayList<>();

    //inverted index
    private static  ConcurrentHashMap<Word,HashSet<Url>> index = new  ConcurrentHashMap<Word,HashSet<Url>>();


    private static ConcurrentHashMap<Url,ArrayList<String>> index_apontados = new ConcurrentHashMap<Url,ArrayList<String>>();
    private static ConcurrentHashMap<String,ArrayList<String>> index_apontados_cpy = new ConcurrentHashMap<String,ArrayList<String>>();
    private static ConcurrentHashMap<Integer,ConcurrentHashMap<String,ArrayList<String>>> relevancia = new ConcurrentHashMap<Integer,ConcurrentHashMap<String,ArrayList<String>>>();

    LinkedBlockingQueue<Url> repetidos = new LinkedBlockingQueue<>();


    public StorageBarrels() throws RemoteException {
        super();
        this.id = -1; //id
    }

    //RMI Callback -> Input from client and requires information from barrel
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

                    if (usr.getName().equals(str[1]) && usr.getPassword().equals(str[2])){
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

            //gets the links that are in commun with the works inputed
            LinkedBlockingQueue<Url> to_delete = new LinkedBlockingQueue<>();


            //System.out.println(index);
            //System.out.println("print");

            for (int j = 1; j < str.length; j++){

                Set<Word> key = index.keySet();

                for (Word k : key) {
                    if (k.getWord().equalsIgnoreCase(str[j])) {
                        HashSet<Url> ul_aux = index.get(k);
                        if (repetidos.isEmpty()){
                            for (Url u: ul_aux) {
                                //add all at first
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
                                //get non-repeated
                                if (!flag) {
                                    to_delete.add(aux);
                                }
                            }
                            //remove non-repeated
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



            ConcurrentHashMap<Url,Integer> to_sort = new ConcurrentHashMap<Url,Integer>();
            List<String> relevancia = new ArrayList<>();
            int rev ;




            int count_rep = 0;
            for (Url u:repetidos) {
                if (count_rep >= 9){
                    message = message + "\nWrite \"plus\" for more!\n";
                    return message;
                }
                message = message + u.getName() +"\n" + u.getTitle() + "\n" + u.getQuote() + "\n\n";
                u.setTotalTargetUrls(u.getTotalTargetUrls() +1);
                repetidos.remove(u);
                count_rep++;
            }






        }else if(str[0].equals("conn")){


            for (Map.Entry<String, ArrayList<String>> entry : index_apontados_cpy.entrySet()) {
                String url = entry.getKey();

                if (url.equalsIgnoreCase(str[1])){

                    for (String url_h : entry.getValue()){
                        message = message + url_h + "\n";
                    }
                    break;
                }

            }
            message = message + "\n";
            return message;

        }else if(str[0].equals("statsv2")) {

            int count = 10;
            int size = 10;
            int size_aux = 1;
            int aux = 0;
            int helper = 0;
            HashMap<Integer,Url> num = new HashMap<>();
            for (Url url_count : index_apontados.keySet()) {
                    size_aux --;
                    if (size != 0){
                        num.put(url_count.getTotalTargetUrls(),url_count);
                        size --;
                    }else{
                        for (Integer key : num.keySet()){

                            if(size_aux == 0){
                                helper = key;
                                size_aux ++;
                            }
                        }
                        num.remove(helper);
                        num.put(url_count.getTotalTargetUrls(),url_count);

                    }

                    ArrayList<Integer> sort = new ArrayList<Integer>(num.keySet());
                    Collections.sort(sort);

            }

            for ( HashMap.Entry<Integer, Url> entry : num.entrySet()) {
                if(count != 0){
                    message = message + entry.getValue().getName() +"\n";
                }else{
                    message = message +"\n";
                    break;
                }
            }

            return message;

        }else if(str[0].equals("plus")){
            //System.out.println("entrei");
            int count_rep_2 = 0;
            for (Url u:repetidos) {
                if (count_rep_2 > 9){
                    return "press space for more!";
                }
                message = message + u.getName() +"\n" + u.getTitle() + "\n" + u.getQuote() + "\n\n";
                u.setTotalTargetUrls(u.getTotalTargetUrls() +1);
                repetidos.remove(u);
                count_rep_2++;
            }

            return message;

        }




        return "Command not found: " + str[0];
    }

    public static void main(String args[]) throws IOException, NotBoundException, ServerNotActiveException {
        // usage: java HelloClient username
        //System.getProperties().put("java.security.policy", "policy.all");
        //System.setSecurityManager(new RMISecurityManager());

        //Multicast Thread
        String nome = "localhost";
        ServerInterface h = (ServerInterface) LocateRegistry.getRegistry(8000).lookup("BARREL");
        StorageBarrels b = new StorageBarrels();
        b.id = h.subscribe_barrel(nome, (BarrelsInterface) b);
        System.out.println("Barrel sent subscription to server");


        MulticastSocket multicast_socket = new MulticastSocket(PORT);
        InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
        multicast_socket.joinGroup(group);



        String file2_aux = "src/StorageBarrels/index_apontados_cpy.obj";
        File file2_2 = new File(file2_aux);
        if(!file2_2.exists()){
            file2_2.createNewFile();
        }
        try {
            if (file2_2.length() != 0) {

                FileInputStream fin22 = new FileInputStream(file2_2);
                ObjectInputStream in22 = new ObjectInputStream(fin22);
                //System.out.println("entrei");
                while (true) {
                    try {
                        index_apontados_cpy = (ConcurrentHashMap<String,ArrayList<String>>) in22.readObject();

                    } catch (Exception e) {
                        break; // eof
                    }
                }
                in22.close();
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        String file3_aux = "src/StorageBarrels/words.obj";
        File file3_3 = new File(file3_aux);
        if(!file3_3.exists()){
            file3_3.createNewFile();
        }
        try {
            if (file3_3.length() != 0) {
                FileInputStream fin3 = new FileInputStream(file3_3);
                ObjectInputStream in3 = new ObjectInputStream(fin3);
                while (true) {
                    try {
                        index = (ConcurrentHashMap<Word,HashSet<Url>>) in3.readObject();

                    } catch (Exception e) {
                        break; // eof
                    }
                }
                in3.close();
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        String file_aux = "src/StorageBarrels/index_apontados.obj";
        File file1_1 = new File(file_aux);
        if(!file1_1.exists()){
            file1_1.createNewFile();
        }
        try {

            if (file1_1.length() != 0) {
                FileInputStream finn3 = new FileInputStream(file1_1);
                ObjectInputStream inn33 = new ObjectInputStream(finn3);

                while (true) {
                    try {
                        index_apontados = (ConcurrentHashMap<Url,ArrayList<String>>) inn33.readObject();
                    } catch (Exception e) {
                        break; // eof
                    }
                }
                inn33.close();
            }

        } catch (FileNotFoundException e) {
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

                    String file = "src/StorageBarrels/index_apontados.obj";
                    Url url = new Url(sup_link, title,quote);
                    try {
                        FileOutputStream fos = new FileOutputStream(file);
                        ObjectOutputStream oos = new ObjectOutputStream(fos);

                        FileInputStream fin = new FileInputStream(file);
                        ObjectInputStream in = new ObjectInputStream(fin);

                        if (file.length() != 0) {
                            while (true) {
                                try {
                                    ConcurrentHashMap<Url,ArrayList<String>> in_file = (ConcurrentHashMap<Url,ArrayList<String>>) in.readObject();
                                    index_apontados.putAll(in_file);
                                } catch (Exception e) {
                                    break; // eof
                                }
                            }
                        }
                        index_apontados.put(url, links_array);
                        oos.writeObject(index_apontados);

                        oos.close();
                        in.close();

                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    String file2 = "src/StorageBarrels/index_apontados_cpy.obj";
                    try {
                        FileOutputStream fos = new FileOutputStream(file2);
                        ObjectOutputStream oos = new ObjectOutputStream(fos);

                        FileInputStream fin = new FileInputStream(file2);
                        ObjectInputStream in = new ObjectInputStream(fin);

                        if (file2.length() != 0) {
                            while (true) {
                                try {
                                    ConcurrentHashMap<String,ArrayList<String>> in_file = (ConcurrentHashMap<String,ArrayList<String>>) in.readObject();
                                    index_apontados_cpy.putAll(in_file);
                                } catch (Exception e) {
                                    break; // eof
                                }
                            }
                        }

                        //link e lista de links
                        //pegar em cada link de lista de links
                        //string -> cada link da lista do lado equerdo
                        //array -> próprio link da pag


                        System.out.println(links_array);
                        for (String link : links_array) {
                            if(index_apontados_cpy.get(link) != null){
                                ArrayList<String> lista_links = index_apontados_cpy.get(link);
                                if (!lista_links.contains(url.getName())){
                                    lista_links.add(url.getName());
                                    index_apontados_cpy.put(link, lista_links);
                                }
                            }else{
                                ArrayList<String> lista_links_n = new ArrayList<String>();
                                lista_links_n.add(url.getName());
                                index_apontados_cpy.put(link, lista_links_n);
                            }
                        }

                        //System.out.println("debug_2------------");
                        //System.out.println(index_apontados_cpy);
                        //System.out.println("---------------------");

                        //ordem de relevancia

                        //System.out.println(index_apontados_cpy.keySet());
                        //System.out.println(index_apontados_cpy.keySet().size());
                        //System.out.println("++++++++++++");

                        // Sort the index_apontados_cpy map by ArrayList size in ascending order


                        //System.out.println("......................");
                        //System.out.println(sortedMap);
                        //System.out.println(sortedMap.keySet());
                        //System.out.println(".......................");



                        oos.writeObject(index_apontados_cpy);
                        oos.close();
                        in.close();

                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
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



                    String file3 = "src/StorageBarrels/words.obj";

                    try {
                        FileOutputStream fos = new FileOutputStream(file3);
                        ObjectOutputStream oos = new ObjectOutputStream(fos);

                        FileInputStream fin = new FileInputStream(file3);
                        ObjectInputStream in = new ObjectInputStream(fin);

                        if (file3.length() != 0) {
                            while (true) {
                                try {
                                    ConcurrentHashMap<Word,HashSet<Url>> in_file = (ConcurrentHashMap<Word,HashSet<Url>>) in.readObject();
                                    index.putAll(in_file);
                                } catch (Exception e) {
                                    break; // eof
                                }
                            }
                        }

                        for(String s:wordsToIndex) {
                            Word wAux = new Word(s);
                            boolean flag = false;
                            if (!index.isEmpty()) {
                                Set<Word> key = index.keySet();

                                for (Word k : key) {
                                    if (k.getWord().equals(s)) {
                                        index.get(k).add(url);
                                        flag = true;
                                    }
                                }
                            }
                            if(flag == false){
                                HashSet<Url> ul = new HashSet<>();
                                ul.add(url);
                                index.put(wAux, ul);
                            }
                        }


                        oos.writeObject(index);


                        oos.close();
                        in.close();

                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
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


                    words_array.clear();
                    //links_array.clear();
                }

            }finally {
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


        //RMI with SearchModule
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

