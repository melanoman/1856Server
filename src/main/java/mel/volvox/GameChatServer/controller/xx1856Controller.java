package mel.volvox.GameChatServer.controller;

import mel.volvox.GameChatServer.model.seating.Channel;
import mel.volvox.GameChatServer.repository.ChannelRepo;
import mel.volvox.GameChatServer.repository.xx1856Repo;

import mel.volvox.GameChatServer.xx1856.xx1856Board;
import mel.volvox.GameChatServer.xx1856.xx1856Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@Controller
@Component
public class xx1856Controller {
    public static final String xx1856_TYPE = "xx1856";
    public Map<String, xx1856Game> name2game = new HashMap<>();

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
    synchronized xx1856Board create1856(@PathVariable String name) {
        if(channelRepo.findByNameAndType(name, xx1856_TYPE).isEmpty()) {
            try {
                channelRepo.save(new Channel(name, xx1856_TYPE));
                xx1856Game game = new xx1856Game(name, repo);
                name2game.put(name, game);
                return game.getBoard();
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage());
            }
        } else {
            throw new IllegalStateException("Game already exists");
        }
    }

    @GetMapping("18xx/board/{name}")
    @ResponseBody
    synchronized xx1856Board load1856(@PathVariable String name) {
        if(name2game.containsKey(name)) return name2game.get(name).getBoard();
        if(channelRepo.findByNameAndType(name, xx1856_TYPE).isEmpty()) {
            throw new IllegalStateException("Game not Found");
        } else {
            xx1856Game game = new xx1856Game(name, repo);
            name2game.put(name, game);
            return game.getBoard();
            //TODO load moves
        }
    }
}
