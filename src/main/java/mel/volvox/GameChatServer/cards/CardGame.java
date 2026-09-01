package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Tableau;
import mel.volvox.GameChatServer.service.CheckoffService;
import mel.volvox.GameChatServer.comm.cards.Placement;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class CardGame {
    public static final String MAIN = "main";
    protected static final int DRAW_PILE = -1;
    protected static final int PLAY_PILE = -2;
    protected static final int NO_SELECTION = -3;
    protected static String DRAW = "draw";
    protected static String PLAY = "play";
    protected Tableau table;
    protected String user;
    protected String gameName;
    protected CheckoffService service;

    public void init(String user, String gameName, CheckoffService service) {
        String id = UUID.randomUUID().toString();
        table = new Tableau();
        table.setId(id);
        this.user = user;
        this.service = service;
        this.gameName = gameName;
        init();
    }

    abstract void init();

    protected void win() {
        if(!user.isEmpty()) service.check(user, gameName);
        table.setResult(Tableau.WIN);
    }

    protected void lose() {
        table.setResult(Tableau.LOSE);
    }

    protected void flipDeck(Placement p) {
        List<Card> out = new ArrayList<>();
        List<Card> in = p.getDeck();
        while(!in.isEmpty()) {
            Card c = in.get(0);
            c.setExposed(!c.isExposed());
            out.add(0, c);
            in.remove(0);
        }
        p.setDeck(out);
    }

    public Tableau getLayout() {return table; }
    public abstract Tableau select(String id, int gridX, int gridY);
}
