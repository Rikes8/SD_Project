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

@Controller
public class IndexController {

    private WebServerRMI server;

    public IndexController(WebServerRMI webserver){
        this.server = webserver;
    }

    @GetMapping("/indexarUrl")
    public String indexarUrl(Model model) throws RemoteException {

        model.addAttribute("input", "");

        return "indexarUrl";
    }

    @PostMapping("/indexarUrl")
    public String indexarUrlPost(@RequestParam(name = "input") String input, Model model) throws RemoteException {


        if (!input.equals("")){
            //System.out.println("-------");
            System.out.println(input);
            server.index(input);
        }

        model.addAttribute("input", input);

        return "indexarUrl";
    }

}