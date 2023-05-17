package com.example.demo;

import com.example.demo.beans.WebServerRMI;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Class responsavel pela pagina de indexação de URL's
 */
@Controller
public class IndexController {

    private WebServerRMI server;
    /**
     * Construtor da classe
     * @param webserver Servidor RMI
     */
    public IndexController(WebServerRMI webserver){
        this.server = webserver;
    }

    /***
     * metodo que faz o mapeamento da pagina indexarUrl
     * @param model
     * @return pagina para indexar URL's
     * @throws RemoteException
     */
    @GetMapping("/indexarUrl")
    public String indexarUrl(Model model) throws RemoteException {

        model.addAttribute("input", "");

        return "indexarUrl";
    }

    /***
     * metodo responsavel por tratar da indexação do URL na queue do WebServer
     * @param input URL inserido
     * @param model
     * @return homepage indexarUrl com uma mensagem de aviso
     * @throws RemoteException
     */
    @PostMapping("/indexarUrl")
    public String indexarUrlPost(@RequestParam(name = "input") String input, Model model) throws RemoteException {


        if (!input.equals("")){
            //System.out.println("-------");
            //System.out.println(input);
            server.index(input);
            model.addAttribute("input", "");
            String mensagem = "Url Indexado: "+input;
            model.addAttribute("mensagem",mensagem);
        }
        else{
            String mensagem = "Insira um url a ser indexado!";
            model.addAttribute("mensagem",mensagem);
        }


        return "indexarUrl";
    }


}