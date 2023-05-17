package com.example.demo;


import com.example.demo.beans.WebServerRMI;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/***
 * Class responsavel pela gestão das paginas que lidam com a REST API HackerNews
 */
@Controller
public class ControllerHackerNews {

    private WebServerRMI server;

    /**
     * Construtor da classe
     * @param webserver Servidor RMI
     */
    public ControllerHackerNews(WebServerRMI webserver){
        this.server = webserver;
    }

    /***
     * metodo que faz o mapeamento da pagina HackerNews
     * @param model
     * @return pagina HackerNews
     */
    @GetMapping("/HackerNews")
    public String HackerNews(Model model){
        return "HackerNews";
    }

    /***
     * metodo que faz o mapeamento da pagina HackerNewsUser
     * @param model
     * @return pagina indexarHackerNewsUser
     */
    @GetMapping("/HackerNewsUser")
    public String HackerNewsUser(Model model){
        return "indexarHackerNewsUser";
    }

    /***
     * metodo que faz o mapeamento da pagina HackerNewsSearch
     * @param model
     * @return pagina indexHackerNews
     */
    @GetMapping("/HackerNewsSearch")
    public String HackerNewsSearch(Model model){
        return "indexHackerNews";
    }

    /***
     * metodo que faz o mapeamento da pagina indexarHackerNews.
     * Metodo responsavel pela ligação a REST API retira as historia com a palavra inserida
     * e mandar indexar na queue
     * @param string termos da pesquisa inseridos
     * @param model
     * @return pagina indexHackerNews
     */
    @GetMapping("/indexarHackerNews")
    public String getHackerNews(@RequestParam(name="string")String string ,Model model){
        try {
            ArrayList<String> results = new ArrayList<>();

            URL url = new URL("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-agent", "Internet Explorer");


            InputStream input = connection.getInputStream();
            InputStreamReader inputS = new InputStreamReader(input);

            BufferedReader read = new BufferedReader(inputS);

            StringBuilder txt = new StringBuilder();
            String str;
            while ((str = read.readLine())!= null){
                txt.append(str);
            }


            JSONArray json = new JSONArray(txt.toString());

            String id;
            String url_start = "https://hacker-news.firebaseio.com/v0/item/";
            String url_end = ".json?print=pretty";
            for (int i = 0; i< 101; i++){
                id = json.get(i).toString();

                String url_full = url_start + id + url_end;
                connection = (HttpURLConnection) new URL(url_full).openConnection();


                InputStream input2 = connection.getInputStream();
                InputStreamReader inputS2 = new InputStreamReader(input2);

                BufferedReader read2 = new BufferedReader(inputS2);

                StringBuilder txt2 = new StringBuilder();
                String str2;
                while ((str2 = read2.readLine())!= null){
                    txt2.append(str2);
                }
                read2.close();

                JSONObject json_obj = new JSONObject(txt2.toString());

                //System.out.println(json_obj);
                if (json_obj.has("url") && json_obj.has("text")){
                    //System.out.println(json_obj.get("url").toString());
                    //System.out.println(json_obj.get("text").toString());
                    String url_storie = json_obj.get("url").toString();
                    String url_text = json_obj.get("text").toString();

                    String[] splited= url_text.split(" ");

                    //System.out.println("-->" +string);
                    String[] spl_string = string.split(" ");
                    int count = 0;
                    for (int j = 0; j < splited.length; j++){
                        for (int k = 0; k < spl_string.length; k++){
                            if(splited[j].equals(spl_string[k])){
                                //System.out.println(url_storie);
                                server.index(url_storie);
                                count+=1;
                            }
                        }
                    }
                    if(count == 0){
                        model.addAttribute("except", "Não se encontram historias com essas palavras!");
                        return "indexHackerNews";
                    }
                }
            }

            read.close();


            connection.disconnect();

        } catch(IOException e) {
            e.printStackTrace();
        }
        model.addAttribute("except", "Operação bem sucedida!");
        return "indexHackerNews";
    }
    /***
     * metodo que faz o mapeamento da pagina indexarHackerNewsUser.
     * Metodo responsavel pela ligação a REST API retira as historia do utilizador com o nome inserida
     * e mandar indexar na queue
     * @param string nome do utilizador
     * @param model
     * @return pagina indexHackerNewsUser
     */
    @GetMapping("/indexarHackerNewsUser")
    public String getHackerNewsUser(@RequestParam(name="string")String string ,Model model){


        try {
            String first = "https://hacker-news.firebaseio.com/v0/user/";
            String last = ".json?print=pretty";
            String url_done = first + string + last;


            HttpURLConnection connection2 = (HttpURLConnection) new URL(url_done).openConnection();
            connection2.setRequestMethod("GET");
            connection2.setDoOutput(true);
            connection2.setInstanceFollowRedirects(false);
            connection2.setRequestProperty("Accept", "application/json");
            connection2.setRequestProperty("User-agent", "Internet Explorer");


            InputStream input3 = connection2.getInputStream();
            InputStreamReader inputS3 = new InputStreamReader(input3);

            BufferedReader read3 = new BufferedReader(inputS3);

            StringBuilder txt3 = new StringBuilder();
            String str3;
            while ((str3 = read3.readLine())!= null){
                txt3.append(str3);
            }

            //System.out.println(txt3);


            if (!txt3.toString().equals("null")){

                System.out.println(txt3);
                JSONObject json2 = new JSONObject(txt3.toString());

                String submit = json2.get("submitted").toString();
                submit =submit.replace("[","");
                submit = submit.replace("]","");

                String[] splited_sub= submit.split(",");
                System.out.println(splited_sub[0]);

                for (int i=0; i< splited_sub.length; i++){
                    String urlstart = "https://hacker-news.firebaseio.com/v0/item/";
                    String urlend = ".json?print=pretty";

                    String url_full = urlstart + splited_sub[i] + urlend;
                    connection2 = (HttpURLConnection) new URL(url_full).openConnection();

                    InputStream input4 = connection2.getInputStream();
                    InputStreamReader inputS4 = new InputStreamReader(input4);

                    BufferedReader read4 = new BufferedReader(inputS4);

                    StringBuilder txt4 = new StringBuilder();
                    String str4;
                    while ((str4 = read4.readLine())!= null){
                        txt4.append(str4);
                    }
                    read4.close();

                    JSONObject json_obj4 = new JSONObject(txt4.toString());

                    //System.out.println(json_obj);
                    if (json_obj4.has("url")){
                        String url_storie4 = json_obj4.get("url").toString();
                        if (url_storie4.length()>0){
                            server.index(url_storie4);
                        }

                    }

                }

            }else {

                model.addAttribute("except", "Sem utilizadores com esse nome!");
                return "indexarHackerNewsUser";
            }



            read3.close();


            connection2.disconnect();

        } catch(IOException e) {
            e.printStackTrace();
        }
        model.addAttribute("except", "Operação bem sucedida!");
        return "indexarHackerNewsUser";
    }


}




