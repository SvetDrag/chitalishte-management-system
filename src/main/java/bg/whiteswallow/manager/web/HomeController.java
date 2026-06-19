package bg.whiteswallow.manager.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index(HttpSession session) {
        //If user is logged - redirect to the Dashboard
        if (session.getAttribute("user_id") != null){
            return "redirect:/home";
        }
        return "index"; //if is still guest -> home page
    }

    @GetMapping("/home")
    public String home(HttpSession session){
        if(session.getAttribute("user_id") == null){
            return "redirect:/user/login";
        }
        return "home"; //Dashboard for logged users
    }



}
