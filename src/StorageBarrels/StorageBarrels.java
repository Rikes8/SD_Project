package src.StorageBarrels;

import src.Classes.Word;
import src.SearchModule.ServerInterface;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

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

            //List<Url> to_delete = new ArrayList<>();
            //List<Url> repetidos = new ArrayList<>();
            LinkedBlockingQueue<Url> to_delete = new LinkedBlockingQueue<>();
            LinkedBlockingQueue<Url> repetidos = new LinkedBlockingQueue<>();

            System.out.println(index);
            System.out.println("print");

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


            for (Url u:repetidos) {
                message = message + u.getName() +"\n" + u.getTitle() + "\n" + u.getQuote() + "\n\n";
            }


            /*for (Map.Entry<Url, Integer> en : repetidos.entrySet()) {
                message = message + en.getKey().getName() +"\n" + en.getKey().getTitle() + "\n" + en.getKey().getQuote() + "\n\n";
            }*/

            /*for (int i = 0; i < repetidos.size(); i++) {
                message = message + repetidos.get(i).getName() +"\n" + repetidos.get(i).getTitle() + "\n" + repetidos.get(i).getQuote() + "\n\n";
            }*/

            return message;

        }else if(str[0].equals("conn")){


            for (Map.Entry<String, ArrayList<String>> entry : index_apontados_cpy.entrySet()) {
                String url = entry.getKey();
                if (url.equalsIgnoreCase(str[1])){

                    //message with links linked
                    message = message + entry.getValue() + "\n\n";
                    break;
                }

            }
            return message;

        }




        return "Command not found";
    }

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
        try {

            FileInputStream fin22 = new FileInputStream(file2_aux);
            ObjectInputStream in22 = new ObjectInputStream(fin22);

            if (file2_aux.length() != 0) {
                System.out.println("entrei");
                while (true) {
                    try {
                        index_apontados_cpy = (ConcurrentHashMap<String,ArrayList<String>>) in22.readObject();
                        //index_apontados_cpy.putAll(in_file);

                    } catch (Exception e) {
                        break; // eof
                    }
                }
            }

            in22.close();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String file3_aux = "src/StorageBarrels/words.obj";
        try {
            FileInputStream fin3 = new FileInputStream(file3_aux);
            ObjectInputStream in3 = new ObjectInputStream(fin3);

            //System.out.println(index + "\n...");

            if (file3_aux.length() != 0) {
                //System.out.println("entre2");
                while (true) {
                    //System.out.println("->2");
                    try {
                        //System.out.println("->2.1");
                        index = (ConcurrentHashMap<Word,HashSet<Url>>) in3.readObject();
                        //System.out.println(index);
                        //index.putAll(in_file);
                        //System.out.println();
                    } catch (Exception e) {
                        //System.out.println("out");
                        break; // eof
                    }
                }

            }
            in3.close();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String file_aux = "src/StorageBarrels/index_apontados.obj";


        try {
            FileInputStream finn3 = new FileInputStream(file_aux);
            ObjectInputStream inn33 = new ObjectInputStream(finn3);

            if (file_aux.length() != 0) {

                while (true) {
                    try {
                        index_apontados = (ConcurrentHashMap<Url,ArrayList<String>>) inn33.readObject();
                    } catch (Exception e) {
                        break; // eof
                    }
                }
            }
            inn33.close();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
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

                    //System.out.println("debug1");
                    //System.out.println(wordsToIndex);


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
                        System.out.println(index);

                        oos.close();
                        in.close();

                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }



                    //System.out.println(index);

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
                    String file = "src/StorageBarrels/urls.obj";
                    /*try {
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
                        //oos.writeObject(index_apontados);



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

