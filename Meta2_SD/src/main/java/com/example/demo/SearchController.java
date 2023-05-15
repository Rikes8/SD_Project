package com.example.demo;

import com.example.demo.beans.WebServerRMI;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.rmi.RemoteException;
import java.util.ArrayList;

@Controller
public class SearchController {

    private WebServerRMI server;

    public SearchController(WebServerRMI webserver){
        this.server = webserver;
    }

    @GetMapping(value = "/search")
    public String search(Model model) throws RemoteException {
        ArrayList<String> help = new ArrayList<>();

        help = server.getlinks();
        model.addAttribute("array", help);

        return "array";
    }
}
