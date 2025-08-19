package mel.volvox.GameChatServer.controller;

import mel.volvox.GameChatServer.repository.TrainRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@Controller
@Component
public class TrainController {
    @Autowired
    TrainRepo trainRepo;

    @PutMapping ("/1856/create/{table}")
    @ResponseBody
    String create1856(@PathVariable String table) {

        throw new Error("Not implemented");

    }
}
