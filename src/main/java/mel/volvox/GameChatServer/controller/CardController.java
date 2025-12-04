package mel.volvox.GameChatServer.controller;

import mel.volvox.GameChatServer.cards.Addition11;
import mel.volvox.GameChatServer.cards.Addition13;
import mel.volvox.GameChatServer.cards.CardGame;
import mel.volvox.GameChatServer.comm.cards.Tableau;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@Controller
@Component
public class CardController {
    Map<String, CardGame> id2game = new HashMap<>();

    static Map<String, Class<? extends CardGame>> name2class = new HashMap<>();
    static {
        name2class.put("simple addition ==>thirteens", Addition13.class);
        name2class.put("simple addition ==>elevens", Addition11.class);
    }

    private CardGame findGame(String id) {
        CardGame out = id2game.get(id);
        if(out == null) throw new IllegalStateException("GameNotFound");
        return out;
    }

    @PutMapping("cards/new/{game}")
    @ResponseBody
    public Tableau createGame(@PathVariable String game) {
        Class<? extends CardGame> clazz = name2class.get(game);
        if(clazz==null) throw new IllegalStateException("Unknown game type "+game);
        try {
            CardGame cg = clazz.getConstructor().newInstance();
            cg.init();
            id2game.put(cg.getLayout().getId(), cg);
            return cg.getLayout();
        } catch (Exception e) {
            throw new IllegalStateException("Game construction failed: "+e.getClass().getName());
        }
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
