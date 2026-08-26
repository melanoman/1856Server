package mel.volvox.GameChatServer.controller;

import mel.volvox.GameChatServer.cards.*;
import mel.volvox.GameChatServer.comm.cards.CardMenuItem;
import mel.volvox.GameChatServer.comm.cards.Tableau;
import mel.volvox.GameChatServer.model.cards.CardRules;
import mel.volvox.GameChatServer.model.stat.Checkoff;
import mel.volvox.GameChatServer.repository.CardRulesRepo;
import mel.volvox.GameChatServer.repository.CheckoffRepo;
import mel.volvox.GameChatServer.service.CheckoffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin
@Controller
@Component
public class CardController {
    @Autowired CardRulesRepo cardRulesRepo;
    @Autowired CheckoffRepo checkoffRepo;

    CheckoffService checkoffService = null;
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
        addGame("pyramid", Pyramid.class);
        addGame("nestor", Nestor.class);
        addGame("monte carlo", MonteCarlo.class);
        addGame("decade", Decade.class);
        addGame("matrimony", Matrimony.class);
        addGame("accordian", Accordian.class);
        addGame("golf", Golf.class);
        addGame("hit or miss", HitOrMiss.class);
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

    @PutMapping("cards/new/{game}/{user}")
    @ResponseBody
    public Tableau createGame(@PathVariable String game,
                              @PathVariable String user) {
        return makeGame(game, user);
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

    @PutMapping("cards/change/{before}/{after}/{user}")
    @ResponseBody
    public Tableau startAnother(@PathVariable String before,
                                @PathVariable String after,
                                @PathVariable String user) {
        CardGame cg = id2game.remove(before);
        //TODO add resignation to stats
        return makeGame(after, user);
    }

    @GetMapping("cards/defeated/{name}")
    @ResponseBody
    public List<String> defeated(@PathVariable String name) {
        List<Checkoff> wins = checkoffRepo.findByName(name);
        Set<String> set = new HashSet<>();
        for(Checkoff c: wins) set.add(c.getTask());
        return set.stream().toList();
    }

    private Tableau makeGame(String game, String user) {
        Class<? extends CardGame> clazz = name2class.get(game);
        if(checkoffService == null) checkoffService = new CheckoffService(checkoffRepo);
        if(clazz==null) throw new IllegalStateException("Unknown game type "+game);
        try {
            if(user == null || user.equals("undefined") || user.equals("=-=")) user = "";
            CardGame cg = clazz.getConstructor().newInstance();
            cg.init(user, game, checkoffService);
            id2game.put(cg.getLayout().getId(), cg);
            return cg.getLayout();
        } catch (Exception e) {
            throw new IllegalStateException("Game construction failed: "+e.getClass().getName());
        }
    }
}
