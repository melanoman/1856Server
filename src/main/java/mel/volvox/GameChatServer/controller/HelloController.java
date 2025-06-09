package mel.volvox.GameChatServer.controller;


import jakarta.annotation.PostConstruct;
import mel.volvox.GameChatServer.model.Human;
import mel.volvox.GameChatServer.repository.HumanRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@Component
public class HelloController {

    @Autowired
    private HumanRepo humanRepo;

    @CrossOrigin(origins = "http://localhost:32109")
    @GetMapping("/")
    @ResponseBody
    public String index(@CookieValue(value = "user", defaultValue = "")String userName) {
        //TODO session token validation
        if(userName.length()<1) {
            return "You need to login";
        } else {
            return "Hello " + userName;
        }
    }
}
