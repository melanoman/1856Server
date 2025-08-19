package mel.volvox.GameChatServer.controller;

import mel.volvox.GameChatServer.comm.RPSBoard;
import mel.volvox.GameChatServer.game.GameRPS;
import mel.volvox.GameChatServer.model.seating.Channel;
import mel.volvox.GameChatServer.repository.ChannelRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@CrossOrigin
@Controller
@Component
public class RPSController {
    @Autowired
    ChannelRepo channelRepo;

    static public final String RPS_TYPE = "rps";

    Map<String, GameRPS> name2game = new HashMap<>();
    
    synchronized private GameRPS loadGame(String name) { // change to RPSGame
        GameRPS game = name2game.get(name);
        if(game != null) return game;

        Optional<Channel> table = channelRepo.findByNameAndType(name, RPS_TYPE);
        if(table.isEmpty()) throw new IllegalStateException("Channel Not Found");

        GameRPS out = new GameRPS();
        name2game.put(name, out);
        return out;
    }

    //TODO move this to a utility file
    @GetMapping("rps/status/{table}")
    @ResponseBody
    public RPSBoard getRPSstatus(@PathVariable String table) {

        GameRPS tv = loadGame(table);
        if (tv == null) throw new IllegalStateException("Table not found");
        return tv.getStatus();
    }

    @PutMapping("rps/pause/{table}")
    @ResponseBody
    public RPSBoard requestPause(@PathVariable String table) {
        GameRPS tv = loadGame(table);
        if (tv == null) throw new IllegalStateException("Table not found");

        return tv.pause();
    }

    @PutMapping("rps/resume/{table}")
    @ResponseBody
    public RPSBoard requestResume(@PathVariable String table) {
        GameRPS tv = loadGame(table);
        if (tv == null) throw new IllegalStateException("Table not found");

        return tv.resume();
    }

    @PutMapping("rps/move/{table}/{user}/{move}")
    @ResponseBody
    public String submitMove(@PathVariable String table,
                             @PathVariable String user,
                             @PathVariable String move) {
        GameRPS tv = loadGame(table);
        if (tv == null) throw new IllegalStateException("Table not found");

        return tv.chooseThrow(user, move);
    }

    @PutMapping("rps/start/{table}/{time}")
    @ResponseBody
    synchronized public RPSBoard startRPS(@PathVariable String table,
                                          @PathVariable int time) {
        GameRPS tv = loadGame(table);
        if (tv == null) throw new IllegalStateException("Table not found");

        return tv.startGame(time);
    }

    @PutMapping("rps/join/{table}/{user}")
    @ResponseBody
    synchronized public boolean join(@PathVariable String table,
                                     @PathVariable String user) {
        GameRPS tv = loadGame(table);
        if (tv == null) throw new IllegalStateException("Table not found");

        return tv.join(user);
    }

    @PutMapping("rps/quit/{table}/{user}")
    @ResponseBody
    synchronized public boolean leave(@PathVariable String table,
                                     @PathVariable String user) {
        GameRPS tv = loadGame(table);
        if (tv == null) throw new IllegalStateException("Table not found");

        return tv.leave(user);
    }
}
