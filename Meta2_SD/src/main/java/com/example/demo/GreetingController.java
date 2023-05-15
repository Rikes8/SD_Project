package com.example.demo;

import com.example.demo.beans.WebServerRMI;
import org.apache.coyote.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.function.EntityResponse;

import java.rmi.RemoteException;

@Controller
public class GreetingController {


    @GetMapping("/")
    public String redirect() {
        return "redirect:/homepage";
    }

	@GetMapping("/homepage")
	public String greeting(Model model) {
		return "homepage";
	}

    @GetMapping("/search2")
    public String homepage(Model model){
        return redirect();
    }









}