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

/**
 * Class responsavel pela homepage, rediricionamento do URL raiz para a homepage e login.
 */
@Controller
public class GreetingController {

    /**
     * metodo que faz o mapeamento da pagina raiz e redireciona para a homepage
     * @return pagina homepage
     */
    @GetMapping("/")
    public String redirect() {
        return "redirect:/homepage";
    }

    /**
     * metodo que faz o mapeamento da pagina homepage
     * @param model
     * @return pagina homepage
     */
	@GetMapping("/homepage")
	public String greeting(Model model) {
		return "homepage";
	}
    /**
     * metodo que faz o mapeamento da pagina links
     * @param model
     * @return pagina links
     */
    @GetMapping("/links")
    public String links(Model model){
        return "links";
    }

    /***
     * metodo que faz o mapeamento da pagina login
     * @param model
     * @return pagina login
     */
    @GetMapping("/login")
    public String login(Model model){
        return "login";
    }
}