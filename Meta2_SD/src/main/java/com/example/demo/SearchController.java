package com.example.demo;

import com.example.demo.beans.WebServerRMI;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.rmi.RemoteException;
import java.util.ArrayList;

@Controller
public class SearchController {

    private WebServerRMI server;

    /**
     * Construtor da classe
     * @param webserver Servidor RMI
     */
    public SearchController(WebServerRMI webserver){
        this.server = webserver;
    }


    /**
     * metodo que faz o mapeamento da pagina /search
     * @param input termos da pesquisa inserido
     * @param pag pagina requerida pelo utilizador
     * @param model
     * @return a pagina de pesquisa com o resultado indexado em pagina contendo cada 10 links
     * @throws RemoteException
     */
    @GetMapping  (value = "/search")
    public String search(@RequestParam(name = "input") String input,@RequestParam(name = "pag", defaultValue = "1") String pag, Model model) throws RemoteException {
        ArrayList<ArrayList<String>> help = new ArrayList<>();

        //System.out.println("+++ " + pag);

        int paginas = 0;
        if (!input.equals("")){
            //System.out.println("---" + pag);




            System.out.println("--:" + pag);

            //System.out.println("input:" + input);

            paginas = server.num_paginas(input);
            System.out.println(":::" + paginas);

            help = server.getlinks(input,pag);
            System.out.println(help);


        }

        //System.out.println("<<<<<" + pag);
        model.addAttribute("input", input);
        model.addAttribute("pag", pag);
        model.addAttribute("paginas", paginas);
        model.addAttribute("array", help);

        //System.out.println("POST");
        return "search";
    }



}



