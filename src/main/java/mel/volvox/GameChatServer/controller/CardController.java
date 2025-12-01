package mel.volvox.GameChatServer.controller;

import mel.volvox.GameChatServer.cards.Addition13;
import mel.volvox.GameChatServer.cards.CardGame;
import mel.volvox.GameChatServer.comm.cards.Tableau;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@Controller
@Component
public class CardController {
    Map<String, CardGame> id2game = new HashMap<>();

    @GetMapping("cards/new/{category}/{game}")
    @ResponseBody
    public Tableau createGame(@PathVariable String category,
                             @PathVariable String game) {
        CardGame cg = new Addition13(); //TODO lookup specific game
        cg.init();
        id2game.put(game, cg);
        return cg.refresh();
    }

    @GetMapping("cards/list")
    @ResponseBody
    public List<String> list() {
        return id2game.keySet().stream().toList();
    }
}
