package mel.volvox.GameChatServer.controller;

import mel.volvox.GameChatServer.cards.*;
import mel.volvox.GameChatServer.comm.cards.CardMenuItem;
import mel.volvox.GameChatServer.comm.cards.Tableau;
import mel.volvox.GameChatServer.model.cards.CardRules;
import mel.volvox.GameChatServer.repository.CardRulesRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin
@Controller
@Component
public class CardController {
    @Autowired
    CardRulesRepo cardRulesRepo;

    Map<String, CardGame> id2game = new HashMap<>();
    static List<CardMenuItem> mainMenu = new ArrayList<>();
    static Map<String, CardMenuItem> name2sub = new HashMap<>();

    static Map<String, Class<? extends CardGame>> name2class = new HashMap<>();
    static void addGame(String fullName, Class<? extends CardGame> clazz) {
        name2class.put(fullName, clazz);
        String[] p = fullName.split(" ==>");
        String top = p[0];
        if(p.length > 1) {
            CardMenuItem mi;
            if(name2sub.containsKey(top)) {
                mi = name2sub.get(top);
            } else {
                mi = new CardMenuItem(top, new ArrayList<>());
                mainMenu.add(mi);
                name2sub.put(top, mi);
            }
            mi.getSub().add(p[1]);
        } else {
            mainMenu.add(new CardMenuItem(top, null));
        }
    }
    static {
        addGame("simple addition ==>thirteens", Addition13.class);
        addGame("simple addition ==>elevens", Addition11.class);
        addGame("simple addition ==>tens", Addition10.class);
        addGame("simple addition ==>fifteens", Addition15.class);
        addGame("block solitaire ==>elevens", Block11.class);
        addGame("block solitaire ==>tens", Block10.class);
        addGame("baroness", Baroness.class);
        addGame("fourteen puzzle", FourteenPuzzle.class);
    }


    private CardGame findGame(String id) {
        CardGame out = id2game.get(id);
        if(out == null) throw new IllegalStateException("GameNotFound");
        return out;
    }

    @GetMapping("cards/rules/{game}")
    @ResponseBody
    public String getRules(@PathVariable String game) {
        Optional<CardRules> cr = cardRulesRepo.findById(game);
        if(cr.isPresent()) return cr.get().getRules();
        else throw new IllegalStateException("Rules not found");
    }

    @GetMapping("cards/menu")
    @ResponseBody
    public List<CardMenuItem> getMainMenu() {
        return mainMenu;
    }

    @PutMapping("cards/new/{game}")
    @ResponseBody
    public Tableau createGame(@PathVariable String game) {
        return makeGame(game);
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

    @PutMapping("cards/delete/{game}")
    @ResponseBody
    public Tableau deleteGame(@PathVariable String game) {
        CardGame t = id2game.remove(game);
        //TODO add resignation to stats
        if(t == null) throw new IllegalStateException("No such game");
        else return t.getLayout();
    }

    @PutMapping("cards/change/{before}/{after}")
    @ResponseBody
    public Tableau startAnother(@PathVariable String before,
                                @PathVariable String after) {
        CardGame cg = id2game.remove(before);
        //TODO add resignation to stats
        return makeGame(after);
    }

    private Tableau makeGame(String game) {
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
}
