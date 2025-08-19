package mel.volvox.GameChatServer.controller;

import mel.volvox.GameChatServer.comm.RPSBoard;
import mel.volvox.GameChatServer.view.ChatView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseBody;

public class RPSController {
    private ChatView loadView(String name) { // change to RPSGame
        return null; //TODO actually load;
    }

    //TODO move this to a utility file
    @GetMapping("rps/status/{table}")
    @ResponseBody
    public RPSBoard getRPSstatus(@PathVariable String table) {

        ChatView tv = loadView(table);
        if (tv == null) throw new IllegalStateException("Table not found");
        //return tv.getStatus();
        return null;
    }

    @PutMapping("rps/pause/{table}")
    @ResponseBody
    public RPSBoard requestPause(@PathVariable String table) {
        ChatView tv = loadView(table);
        if (tv == null) throw new IllegalStateException("Table not found");

        //return tv.getRPSGame().pause();
        return null;
    }

    @PutMapping("rps/resume/{table}")
    @ResponseBody
    public RPSBoard requestResume(@PathVariable String table) {
        ChatView tv = loadView(table);
        if (tv == null) throw new IllegalStateException("Table not found");

        //return tv.getRPSGame().resume();
        return null;
    }

    @PutMapping("rps/move/{table}/{user}/{move}")
    @ResponseBody
    public String submitMove(@PathVariable String table,
                             @PathVariable String user,
                             @PathVariable String move) {
        ChatView tv = loadView(table);
        if (tv == null) throw new IllegalStateException("Table not found");

        //return tv.getRPSGame().chooseThrow(user, move);
        return null;
    }

    @PutMapping("rps/start/{table}/{time}")
    @ResponseBody
    synchronized public RPSBoard startRPS(@PathVariable String table,
                                          @PathVariable int time) {
        ChatView tv = loadView(table);
        if (tv == null) throw new IllegalStateException("Table not found");

        //return tv.getRPSGame().startGame(time);
        return null;
    }
}
