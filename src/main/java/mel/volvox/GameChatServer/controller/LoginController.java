package mel.volvox.GameChatServer.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import mel.volvox.GameChatServer.comm.HumanComm;
import mel.volvox.GameChatServer.model.Human;
import mel.volvox.GameChatServer.repository.HumanRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin
@RestController
@Component
public class LoginController {
    @Autowired
    private HumanRepo humanRepo;

    @GetMapping("/login")
    @ResponseBody
    public HumanComm login(@RequestParam(name="user")String userName,
                           @RequestParam(name="pass")String password,
                           HttpServletResponse out) {
        Optional<Human> h = humanRepo.findById(userName);
        if(h.isPresent()  && h.get().getPass().equals(password)) {
            out.addCookie(new Cookie("user", userName));
            return new HumanComm(h.get());
        }
        return HumanComm.NOBODY;
    }

    @GetMapping("/logout")
    @ResponseBody
    public String logout(HttpServletResponse out) {
        Cookie cookie = new Cookie("user", null);
        cookie.setMaxAge(0);
        out.addCookie(cookie);
        return "Bye";
    }

    @GetMapping("/createAccount")
    @ResponseBody
    public HumanComm createAccount(@RequestParam(name="user")String userName,
                                   @RequestParam(name="pass")String password,
                                   HttpServletResponse out) {
        Optional<Human> h = humanRepo.findById(userName);
        if(h.isPresent()) {
            return HumanComm.NOBODY;
        } else {
            Human human = new Human(userName, userName, password, "");
            humanRepo.save(human);
            out.addCookie(new Cookie("user", userName));
            return new HumanComm(human);
        }
    }
}
