package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Placement;
import mel.volvox.GameChatServer.comm.cards.Tableau;

import java.util.List;

public class Baroness extends CardGame {
    Placement[] pile = new Placement[5];
    List<Card> deck;

    private Placement makePile(int index, Card c) {
        Placement out = new Placement();
        out.setId(""+index);
        out.setX(200+ index*75);
        out.setY(200);
        out.setGridWidth(1);
        out.setGridHeight(1);
        out.getDeck().add(c);
        c.setExposed(true);
        return out;
    }

    @Override
    public void init() {
        super.init();
        deck = Cards.shuffle(52);
        for (int i=0; i<5; i++) {
            pile[i] = makePile(i, deck.remove(0));
            table.getPlacements().add(pile[i]);
        }
    }
    @Override
    public Tableau select(String id, int gridX, int gridY) {
        return table;
    }
}
