package src.Downloader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import src.SearchModule.ServerInterface;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.net.*;
import java.io.*;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class Downloader extends UnicastRemoteObject implements DownloaderInterface {

    //FIXME: TCP
    private static int serversocket = 6000;

    //FIXME: Multicast
    private static String MULTICAST_ADDRESS = "224.3.2.1";
    private static int PORT = 4321;
    private static long SLEEP_TIME = 5000;
    private int id;

    public Downloader() throws RemoteException {
        super();
        this.id = -1; //id
    }


    public void run() {
        //create socket TCP to communicate with queue
        try (Socket socket = new Socket("localhost", serversocket)) {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            MulticastSocket multicast_socket = null;
            multicast_socket = new MulticastSocket();

            while (true){

                int count_urls = 0;
                String url = "";


                //TCP handshake
                String pedido = "pedido";
                out.writeUTF(pedido);

                //get the responde
                url = in.readUTF();

                String identifier = Integer.toString(id);
                String sms_urls = "type | url_list ; item_count | ";
                String sms_barrels = "type | url ; ";
                String s_quote = " qoute | ";
                String s_title = "title | ";
                String sms_words = "type | word_list ; item_count | ";
                String sms_words_aux = "";

                try{
                    //JSOUP
                    Document doc = Jsoup.connect(url).get();
                    StringTokenizer tokens = new StringTokenizer(doc.text());

                    //add words to the message
                    sms_barrels = sms_barrels + "url | " +url + " ; ";
                    sms_barrels = sms_barrels + s_title;

                    String title = doc.title();
                    sms_barrels = sms_barrels + title + ";";

                    try{
                        int titleSize = title.length();
                        String quote = doc.text().substring(titleSize+1,titleSize+50);
                        s_quote = s_quote+quote;
                        sms_barrels = sms_barrels + s_quote +" ; ";

                    }catch(IndexOutOfBoundsException e){
                        String quote = doc.text();
                        s_quote = s_quote + quote;
                        sms_barrels = sms_barrels + s_quote + ";";
                    }

                    int i = 0;
                    String word = null;
                    int countTokens = 0;
                    while (tokens.hasMoreElements() && countTokens++ < 100) {
                        word = tokens.nextToken();
                        sms_words_aux = sms_words_aux + "word | " +word.toLowerCase() + " ; ";
                        i = i + 1;
                        countTokens = countTokens +1;
                    }

                    sms_words = sms_words + i + " ; " + sms_words_aux;
                    sms_barrels = sms_barrels + sms_words;

                    //count number of links founded
                    Elements links = doc.select("a[href]");
                    for (Element link : links){
                        count_urls ++;
                    }

                    //add links to the message
                    sms_urls = sms_urls + count_urls + " ; ";
                    for (Element link : links){
                        sms_urls = sms_urls + "url_ap | " + link.attr("abs:href") + " ; ";
                    }
                    sms_barrels = sms_barrels + sms_urls;

                    System.out.println(sms_barrels);
                }catch (IOException e) {
                    //e.printStackTrace();
                }

                //Multicast Server connection -> send information to barrels
                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);

                //message with the size
                int UdpPort = sms_barrels.length();
                String sms_size = identifier + ";" + Integer.toString(sms_barrels.length());
                byte[] buffer_msg = sms_size.getBytes();
                DatagramPacket firstMessage = new DatagramPacket(buffer_msg, buffer_msg.length, group, PORT);
                multicast_socket.send(firstMessage);

                //send mensagem
                byte[] buffer = sms_barrels.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                multicast_socket.send(packet);
                Long time = System.currentTimeMillis();

                //socket UDP
                try (DatagramSocket aSocket = new DatagramSocket(UdpPort)) {
                    boolean flag = false;
                    while(!flag){
                        Long pastTime = System.currentTimeMillis();
                        if(pastTime - time > 60000){
                            multicast_socket.send(firstMessage);
                            multicast_socket.send(packet);
                            time = System.currentTimeMillis();
                        }
                        byte[] buffer_ack = new byte[1024];
                        DatagramPacket request = new DatagramPacket(buffer_ack, buffer_ack.length);
                        aSocket.receive(request);
                        String s = new String(request.getData(), 0, request.getLength());
                        if(s.equals(identifier)){
                            flag = true;
                        }
                    }

                }catch (SocketException e){
                    System.out.println("Socket: " + e.getMessage());
                }catch (IOException e){
                    System.out.println("IO: " + e.getMessage());
                }


                //TCP connection -> send urls to queue
                sms_urls = url + "--> " + sms_urls;
                out.writeUTF(sms_urls);
            }
        }catch (UnknownHostException e) {
            System.out.println("Sock:" );
            e.getStackTrace();
        } catch (EOFException e) {
            System.out.println("EOF:" );
            e.getStackTrace();
        } catch (IOException e) {
            System.out.println("Queue is not active!");
            System.exit(0);
        }
    }

    public static void main(String args[]) throws RemoteException, NotBoundException, ServerNotActiveException {

        try{
            String nome = "localhost";
            ServerInterface server = (ServerInterface) LocateRegistry.getRegistry(9000).lookup("DOWNLOADER");
            Downloader down = new Downloader();

            //return is id
            down.id = server.subscribe_downloader(nome, (DownloaderInterface) down);
            System.out.println("Downloader sent subscription to server");

            //Ctrl+C thread handler
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        server.ShareInfoToServer(down.id,"-1d");
                    } catch (RemoteException e) {
                    }
                    System.out.println("Downloader ending...");
                }
            });

            //starts
            down.run();

        }catch (Exception e) {
            System.out.println("SearchModule is not activated!");
        }
    }
}