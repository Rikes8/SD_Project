package com.example.demo.beans;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;

import src.SearchModule.ServerInterface;

/***
 * Class responsavel pela conexão entre o Servidor RMI e a pagina Web
 */
public class WebServerRMI{
    private ServerInterface w;

    /***
     * Construtor da classe WebServerRMI que invoca o metodo connection()
     * @throws RemoteException
     */
    public WebServerRMI() throws RemoteException {
        connection();
    }

    /***
     * Metodo que atribui ao atributo w a referencia ao objeto remoto que está registado na porta 4000 e tem nome "WEBSERVER"
     * @throws RemoteException
     */
    public void connection() throws RemoteException {
        try {
            this.w = (ServerInterface) LocateRegistry.getRegistry(4000).lookup("WEBSERVER");
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    /***
     * Metodo que retorna a uma lista com os links que apontam para o link inserido.
     * Usa o metodo ShareInfoToServer para obter a informação
     * @param link link inserido pelo User
     * @return lista com os links que apontam para o "link"
     * @throws RemoteException
     */
    public ArrayList<ArrayList<String>> getlinksAp(String link) throws RemoteException {
        ArrayList<String> aux;
        ArrayList<ArrayList<String>> div = new ArrayList<>();
        ArrayList<String> aux2 = new ArrayList<>();

        String str = "";

        str = str + "conn " + link;


        //System.out.println(":: "+ str);
        aux = w.ShareInfoToServer(0,str);
        System.out.println(aux);

        int count = 0;
        for (int i = 0; i < aux.size(); i++) {
            aux2.add(aux.get(i));
            div.add(aux2);
            aux2 = new ArrayList<>();
        }
        System.out.println(div);
        //System.out.println("end getlinks()");
        return div;
    }

    /***
     * Metodo que retorna a pagina que o utilizador requisita.
     * Usa o metodo ShareInfoToServer para obter a informação.
     * @param link URL inserido
     * @param pag pagina que o utilizador clica
     * @return pagina que o utilizador requisita
     * @throws RemoteException
     */
    public ArrayList<ArrayList<String>> getlinks(String link, String pag) throws RemoteException {
        ArrayList<String> aux;
        ArrayList<ArrayList<String>> div = new ArrayList<>();
        ArrayList<String> aux2 = new ArrayList<>();

        String str = "";
        if(pag.equals("1")){
            str = str + "search " + link;
        } else{
            str = str + "pag " + pag;
        }


        //System.out.println(":: "+ str);
        aux = w.ShareInfoToServer(0,str);
        System.out.println(aux);

        int count = 0;
        for (int i = 0; i < aux.size(); i++) {
            if(i == aux.size()-2 ){
                //System.out.println(aux.get(i));
                break;
            }

            if (count == 0) {
                aux2.add(aux.get(i));
                //System.out.println("1:" +aux2);
                count++;
            } else if (count == 1) {
                aux2.add(aux.get(i));
                //System.out.println("2:" +aux2);
                count++;
            } else if (count == 2) {
                aux2.add(aux.get(i));
                //System.out.println("3:" +aux2);
                count = 0;
                div.add(aux2);
                aux2 = new ArrayList<>();
                //System.out.println("dev:" +div);
            }
        }
        System.out.println(div);
        //System.out.println("end getlinks()");
        return div;
    }

    /***
     * metodo que retorna o numero de pagina total da pesquisa.
     * Usa o metodo ShareInfoToServer para obter a informação.
     * @param link URL inserido
     * @return numero de paginas da pesquisa
     * @throws RemoteException
     */
    public Integer num_paginas(String link) throws RemoteException {
        ArrayList<String> aux;

        String str = "";
        str = str + "search " + link;

        System.out.println("str: " +str);

        aux = w.ShareInfoToServer(0,str);
        //System.out.println(aux);

        //System.out.println(aux.size());
        int count = 0;
        for (int i = 0; i < aux.size(); i++) {
            if(i == aux.size()-1){
                count = Integer.parseInt(aux.get(i));
            }

        }

        System.out.println("end getnumpag():" +count);
        return count;
    }

    /***
     * Metodo que recebe um URL e indexa-o na Queue do WebServer.
     * Usa o metodo ShareInfoToServer para obter a informação.
     * @param string URL inserido
     * @throws RemoteException
     */
    public void index(String string) throws RemoteException {
        String aux = "";
        aux = aux + "index " + string;

        w.ShareInfoToServer(0,aux);

    }


}
