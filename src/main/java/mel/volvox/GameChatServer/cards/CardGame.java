package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Placement;
import mel.volvox.GameChatServer.comm.cards.Tableau;

import java.util.UUID;

public abstract class CardGame {
    public static final String MAIN = "main";
    protected Tableau table;

    public void init() {
        String id = UUID.randomUUID().toString();
        table = new Tableau();
        table.setId(id);
    }
    public Tableau getLayout() {return table; }
    public abstract Tableau select(String id, int gridX, int gridY);
}
