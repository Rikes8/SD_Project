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

/***
 * Class responsavel pela gestão das paginas linksapontados e linksapt
 */
@Controller
public class ApontadosController {

    private WebServerRMI server;

    /**
     * Construtor da classe
     * @param webserver Servidor RMI
     */
    public ApontadosController(WebServerRMI webserver){
        this.server = webserver;
    }

    /**
     * metodo que faz o mapeamento da pagina linksapontados
     * @param model
     * @return pagina linkapontados
     * @throws RemoteException
     */
    @GetMapping("/linksapontados")
    public String linksApont(Model model) throws RemoteException {

        model.addAttribute("input", "");

        return "linksapontados";
    }

    /***
     * metodo que retorna a pagina para visualizar os links que apontam para o URL inserido
     * @param input URL inserido
     * @param model
     * @return lista com os link que apontam para o URL inserido
     * @throws RemoteException
     */
    @GetMapping("/linksapt")
    public String getApont(@RequestParam(name = "input") String input, Model model) throws RemoteException {

        ArrayList<ArrayList<String>> help = new ArrayList<>();

        if (!input.equals("")){
            //System.out.println("-------");
            //System.out.println(input);
            help = server.getlinksAp(input);
            model.addAttribute("input", "");
            model.addAttribute("array", help);
            String mensagem = "Url pesquisado: "+input;
            model.addAttribute("mensagem",mensagem);
        }
        else{
            String mensagem = "Insira um url a ser pesquiado!";
            model.addAttribute("mensagem",mensagem);
        }


        return "linksapontados";
    }


}