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

    private CardGame findGame(String id) {
        CardGame out = id2game.get(id);
        if(out == null) throw new IllegalStateException("GameNotFound");
        return out;
    }

    @PutMapping("cards/new/{game}")
    @ResponseBody
    public Tableau createGame(@PathVariable String game) {
        CardGame cg = new Addition13(); //TODO lookup specific game
        cg.init();
        id2game.put(cg.getLayout().getId(), cg);
        return cg.getLayout();
    }

    @GetMapping("cards/list")
    @ResponseBody
    public List<String> list() {
        return id2game.keySet().stream().toList();
    }

    @GetMapping("cards/show/{id}")
    @ResponseBody
    public Tableau showTableau(@PathVariable String id) {
        return findGame(id).getLayout();
    }

    @PutMapping("cards/select/{game}/{place}/{x}/{y}")
    @ResponseBody
    public Tableau selectCard(@PathVariable String game,
                              @PathVariable String place,
                              @PathVariable int x,
                              @PathVariable int y) {
        return findGame(game).select(place, x, y);
    }
}
