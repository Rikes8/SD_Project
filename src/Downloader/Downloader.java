package src.Downloader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.StringTokenizer;
import java.net.*;
import java.util.Scanner;
import java.io.*;

public class Downloader {

    private static int serversocket = 6000; //porto tem de ser diff

    public static void geturls() {
        String mensagens = "type | url_list ; item_count | ";
        int count_urls = 0;
        String url = "http://www.uc.pt";
        String[] words = new String[101];
        int i = 0;
        try {
            Document doc = Jsoup.connect(url).get();
            words[i] = url; //FIXME: url of the words, first place in the array
            i = i +1;
            StringTokenizer tokens = new StringTokenizer(doc.text());
            int countTokens = 0;
            while (tokens.hasMoreElements() && countTokens++ < 100) {
                words[i] = tokens.nextToken().toLowerCase(); //FIXME: add the first 100 words to array
                //System.out.println(words[i]);
                i = i + 1;
            }
            Elements links = doc.select("a[href]");
            for (Element link : links){
                count_urls ++;
                //System.out.println(link.text() + "\n" + link.attr("abs:href") + "\n");
            }
            mensagens = mensagens + count_urls + " ; ";

            for (Element link : links){
                mensagens = mensagens + "url | " + link.attr("abs:href") + " ; ";
            }
            System.out.println(mensagens);


                try (Socket socket = new Socket("localhost", serversocket)) {
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    out.writeUTF(mensagens);
                } catch (UnknownHostException e) {
                    System.out.println("Sock:" + e.getMessage());
                } catch (EOFException e) {
                    System.out.println("EOF:" + e.getMessage());
                } catch (IOException e) {
                    System.out.println("IO:" + e.getMessage());
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String args[]) {
        geturls();

    }
}
