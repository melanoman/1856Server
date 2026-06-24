package mel.volvox.GameChatServer.controller;

import mel.volvox.GameChatServer.model.seating.Channel;
import mel.volvox.GameChatServer.repository.ChannelRepo;
import mel.volvox.GameChatServer.repository.xx1856Repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@Controller
@Component
public class xx1856Controller {
    public static final String xx1856_TYPE = "xx1856";

    @Autowired
    private xx1856Repo repo;
    @Autowired
    ChannelRepo channelRepo;

    @GetMapping("/18xx/list")
    @ResponseBody
    synchronized List<Channel> listGames() {
        return channelRepo.findByType(xx1856_TYPE);
    }

    @PutMapping("/18xx/create/{name}")
    @ResponseBody
    synchronized List<Channel> create1856(@PathVariable String name) {
        if(channelRepo.findByNameAndType(name, xx1856_TYPE).isEmpty()) {
            channelRepo.save(new Channel(name, xx1856_TYPE));
        }
        //TODO actually create the game
        //TODO return board instead
        return channelRepo.findByType(xx1856_TYPE);
    }
}
