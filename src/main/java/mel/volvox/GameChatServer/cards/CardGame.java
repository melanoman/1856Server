package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Placement;
import mel.volvox.GameChatServer.comm.cards.Tableau;

import java.util.UUID;

public abstract class CardGame {
    public static final String MAIN = "main";
    protected static final int DRAW_PILE = -1;
    protected static final int PLAY_PILE = -2;
    protected static final int NO_SELECTION = -3;
    protected static String DRAW = "draw";
    protected static String PLAY = "play";
    protected Tableau table;

    public void init() {
        String id = UUID.randomUUID().toString();
        table = new Tableau();
        table.setId(id);
    }
    public Tableau getLayout() {return table; }
    public abstract Tableau select(String id, int gridX, int gridY);
}
