package com.example.demo;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;

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

    @GetMapping("/search")
    public String homepage(Model model){
        return redirect();
    }
    @RequestMapping(value = "/search", method = RequestMethod.POST)
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