package src;

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



    public static void geturls() {
        String url = "http://www.uc.pt";
        String[] words = new String[100];
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
            for (Element link : links)
                System.out.println(link.text() + "\n" + link.attr("abs:href") + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String args[]) {
        geturls();

    }
}
