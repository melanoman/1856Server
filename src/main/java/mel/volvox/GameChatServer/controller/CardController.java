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
        try {
            return id2game.get(id).getLayout();
        } catch(Exception e) {
            throw new IllegalStateException("Game not found");
        }
    }

    @PutMapping("cards/select/{game}/{place}/{x}/{y}")
    @ResponseBody
    public Tableau selectCard(@PathVariable String game,
                              @PathVariable String place,
                              @PathVariable int x,
                              @PathVariable int y) {
        CardGame cg = id2game.get(game);
        cg.select(place, x, y);
        return cg.getLayout();
    }
}
