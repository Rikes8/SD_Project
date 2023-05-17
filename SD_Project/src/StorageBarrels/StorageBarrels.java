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

    private static HashMap<String, Integer> words_searched = new HashMap<>();


    public StorageBarrels() throws RemoteException {
        super();
        this.id = -1; //id
    }

    //RMI Callback -> Input from client and requires information from barrel
    public ArrayList<String> ShareInfoToBarrel(String s) throws RemoteException {
        ArrayList<String> aux_str = new ArrayList<>();

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
                aux_str.add("Client Registed");
                return aux_str;
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
                        aux_str.add("Client Logged");
                        return aux_str;

                    }
                }

                aux_str.add("Wrong Client Credential");
                return aux_str;
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else if (str[0].equals("search")) {

            //gets the links that are in commun with the works inputed
            LinkedBlockingQueue<Url> to_delete = new LinkedBlockingQueue<>();

            String file_r2 = "src/StorageBarrels/wordscount.obj";
            try {
                FileOutputStream fosr2 = new FileOutputStream(file_r2);
                ObjectOutputStream oosr2 = new ObjectOutputStream(fosr2);

                FileInputStream finr2 = new FileInputStream(file_r2);
                ObjectInputStream inr2 = new ObjectInputStream(finr2);

                if (file_r2.length() != 0) {
                    while (true) {
                        try {
                            HashMap<String, Integer> inrl2 = (HashMap<String, Integer>) inr2.readObject();
                            words_searched.putAll(inrl2);
                        } catch (Exception e) {
                            break; // eof
                        }
                    }
                }

                oosr2.writeObject(words_searched);

                oosr2.close();
                inr2.close();

                for(int j = 1; j < str.length ; j++){
                    if (words_searched.containsKey(str[j])){
                        int value = words_searched.get(str[j]);
                        words_searched.put(str[j], value +1);
                    }else {
                        words_searched.put(str[j], 1);
                    }
                }

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }




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



            /*
            int count_rep = 0;
            for (Url u:repetidos) {
                if (count_rep >= 9){
                    //message = message + "\nWrite \"plus\" for more!\n";
                    aux_str.add("\nWrite \"plus\" for more!\n");

                    return aux_str;
                }
                message = message + u.getName() +"\n" + u.getTitle() + "\n" + u.getQuote() + "\n\n";
                aux_str.add(u.getName());
                aux_str.add(u.getTitle());
                aux_str.add(u.getQuote());


                repetidos.remove(u);
                count_rep++;
            }*/

            for (Url u: repetidos){
                aux_str.add(u.getName());
                aux_str.add(u.getTitle());
                aux_str.add(u.getQuote());
                u.setTotalTargetUrls(u.getTotalTargetUrls() +1);
                //repetidos.remove(u);
            }






        }else if(str[0].equals("conn")){
            System.out.println("entrei1");

            for (Map.Entry<String, ArrayList<String>> entry : index_apontados_cpy.entrySet()) {
                String url = entry.getKey();

                if (url.equalsIgnoreCase(str[1])){
                    System.out.println("entrei2");
                    for (String url_h : entry.getValue()){
                        message = message + url_h + "\n";
                        aux_str.add(url_h);
                    }
                    break;
                }

            }
            //message = message + "\n";
            return aux_str;

        }else if(str[0].equals("statsv2")) {
            String file_r = "src/StorageBarrels/wordscount.obj";
            try {
                FileOutputStream fosr = new FileOutputStream(file_r);
                ObjectOutputStream oosr = new ObjectOutputStream(fosr);

                FileInputStream finr = new FileInputStream(file_r);
                ObjectInputStream inr = new ObjectInputStream(finr);

                if (file_r.length() != 0) {
                    while (true) {
                        try {
                            HashMap<String, Integer> inrl = (HashMap<String, Integer>) inr.readObject();
                            words_searched.putAll(inrl);
                        } catch (Exception e) {
                            break; // eof
                        }
                    }
                }

                oosr.writeObject(words_searched);

                oosr.close();
                inr.close();

                HashMap<String,Integer> aux = words_searched;

                for(int j = 0; j < 10; j++){
                    int max = 0;
                    int index = 0;
                    String key="";
                    for (Map.Entry<String, Integer> entry : aux.entrySet()) {
                        if (entry.getValue() > max && !aux_str.contains(entry.getKey())) {
                            max = entry.getValue();
                            key = entry.getKey();
                        }
                    }

                    if (!aux_str.contains(key)){
                        aux_str.add(key);
                    }
                }

                return aux_str;
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }else if(str[0].equals("pag")){
            //System.out.println("entrei");
            /*
            int count_rep_2 = 0;
            for (Url u:repetidos) {
                if (count_rep_2 > 9){
                    aux_str.add("press plus for more");
                    return aux_str;
                }
                //message = message + u.getName() +"\n" + u.getTitle() + "\n" + u.getQuote() + "\n\n";
                aux_str.add(u.getName());
                aux_str.add(u.getTitle());
                aux_str.add(u.getQuote());

                u.setTotalTargetUrls(u.getTotalTargetUrls() +1);
                repetidos.remove(u);
                count_rep_2++;
            }*/

            for (Url u: repetidos){
                aux_str.add(u.getName());
                aux_str.add(u.getTitle());
                aux_str.add(u.getQuote());
                u.setTotalTargetUrls(u.getTotalTargetUrls() +1);
                //repetidos.remove(u);
            }

            return aux_str;

        }



        //aux_str.add("Command not found");
        return aux_str;
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

        /*
        String file23_aux = "src/StorageBarrels/wordscount.obj";
        File file2_23 = new File(file23_aux);
        if(!file2_23.exists()){
            file2_23.createNewFile();
        }
        try {
            if (file2_23.length() != 0) {

                FileInputStream fin223 = new FileInputStream(file2_23);
                ObjectInputStream in223 = new ObjectInputStream(fin223);
                System.out.println("entrei");
                while (true) {
                    try {
                        words_searched = (HashMap<String,Integer>) in223.readObject();

                    } catch (Exception e) {
                        break; // eof
                    }
                }
                in223.close();
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }*/


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

