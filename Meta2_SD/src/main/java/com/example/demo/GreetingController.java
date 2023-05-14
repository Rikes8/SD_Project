package com.example.demo;

import com.example.demo.beans.WebServerRMI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.function.EntityResponse;

import java.rmi.RemoteException;

@Controller
public class GreetingController {

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_APPLICATION, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public WebServerRMI applicationRMI() throws RemoteException {
        return new WebServerRMI();
    }

    @GetMapping("/")
    public String redirect() {
        return "redirect:/homepage";
    }

	@GetMapping("/homepage")
	public String greeting(Model model) {
		return "homepage";
	}

    @GetMapping("/search")
    public String homepage(Model model){
        return redirect();
    }
    @GetMapping(value = "/search")
    public String search(Model model) {
        return "search";
    }
    @GetMapping("/login")
    public String login(Model model){
        return "login";
    }
    @GetMapping("/indexarUrl")
    public String indexarUrl(Model model){
        return "indexarUrl";
    }


}