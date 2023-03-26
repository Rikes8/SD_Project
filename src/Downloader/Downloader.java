package src.Downloader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.net.*;
import java.util.Scanner;
import java.io.*;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;

public class Downloader extends Thread {

    //FIXME: TCP
    private static int serversocket = 6000;

    //FIXME: Multicast
    private static String MULTICAST_ADDRESS = "224.3.2.1";
    private static int PORT = 4321;
    private static long SLEEP_TIME = 5000;


    public void run() {
        while (true) {
            int count_urls = 0;

            //FIXME: Input inicial para começar indexação
            String url = "https://www.uc.pt";


            String sms_urls = "type | url_list ; item_count | ";
            String sms_barrels = "type | url ; ";
            String s_quote = " qoute | ";
            String s_title = "title | ";
            String sms_words = "word_list ; item_count | ";
            String sms_words_aux = "";
            List<String> repeated = new ArrayList<>();

            try{
                //FIXME: JSOUP
                Document doc = Jsoup.connect(url).get();
                StringTokenizer tokens = new StringTokenizer(doc.text());

                sms_barrels = sms_barrels + url + " ; ";
                sms_barrels = sms_barrels + s_title;

                String title = doc.title();
                sms_barrels = sms_barrels + title + ";";

                //FIXME: Adiciona as words à mensagem
                int i = 0;
                String word = null;
                int countTokens = 0;
                while (tokens.hasMoreElements() && countTokens++ < 100) {
                    word = tokens.nextToken();
                    //FIXME: É preciso melhorar a parte da citação para mandar apenas depois do titulo
                    if(countTokens < 20){
                        s_quote = s_quote + word + " ";
                    }
                    sms_words_aux = sms_words_aux + "word | " +word.toLowerCase() + " ; ";
                    i = i + 1;
                    countTokens = countTokens +1;
                }
                sms_barrels = sms_barrels + s_quote;
                sms_words = sms_words + i + " ; " + sms_words_aux;
                sms_barrels = sms_barrels + sms_words;

                //FIXME: Conta o numero de links encontrados
                Elements links = doc.select("a[href]");
                for (Element link : links){
                    count_urls ++;
                }

                //FIXME: Adiciona os links à mensagem
                sms_urls = sms_urls + count_urls + " ; ";
                for (Element link : links){
                    sms_urls = sms_urls + "url_ap | " + link.attr("abs:href") + " ; ";
                }
                sms_barrels = sms_barrels + sms_urls;
            }catch (IOException e) {
                e.printStackTrace();
            }


            MulticastSocket multicast_socket = null;
            try {
                //FIXME: Multicast "server" connection ==> Enviar as words para os barrels
                multicast_socket = new MulticastSocket();
                //while(true){
                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);

                //handshake with the size
                String sms_size = Integer.toString(sms_barrels.length());
                byte[] buffer_handshake = sms_size.getBytes();
                DatagramPacket handshake = new DatagramPacket(buffer_handshake, buffer_handshake.length, group, PORT);
                multicast_socket.send(handshake);
                System.out.println("enviei handshake");

                //send mensagem
                byte[] buffer = sms_barrels.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                multicast_socket.send(packet);
                System.out.println("enviei mensagem");

                try { sleep((long) (Math.random() * SLEEP_TIME)); } catch (InterruptedException e) { }
                /*
                //ACK
                byte[] ack = new byte[1];
                DatagramPacket ack_packet = new DatagramPacket(ack, ack.length, group, PORT);
                System.out.println("ainda n recebi");
                multicast_socket.receive(ack_packet);
                System.out.println("recebi ack");
                if(ack[0] != 1){
                    System.out.println("Not all barrels recieved the message: only: " + ack[0]);
                }*/

                //===============================================================================
                //==========================   TCP   ==================================
                //===============================================================================

                //FIXME: TCP connection ==> Envia urls para a Queue
                try (Socket socket = new Socket("localhost", serversocket)) {
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    out.writeUTF(sms_urls);
                } catch (UnknownHostException e) {
                    System.out.println("Sock:" + e.getMessage());
                } catch (EOFException e) {
                    System.out.println("EOF:" + e.getMessage());
                } catch (IOException e) {
                    System.out.println("IO:" + e.getMessage());
                }



                // wait for some time before starting a new crawl
                sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }catch (IOException e) {
                throw new RuntimeException(e);
            }finally {
                multicast_socket.close();
            }
        }
    }

    public static void main(String args[]) {
        Downloader downloader = new Downloader();
        downloader.start();
    }
}