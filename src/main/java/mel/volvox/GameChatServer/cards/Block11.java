package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Placement;
import mel.volvox.GameChatServer.comm.cards.Tableau;

import java.util.List;

public class Block11 extends CardGame {
    List<Card> deck;
    Placement main = new Placement();
    boolean seeded = false;
    Card a = null;

    @Override
    public void init() {
        super.init();
        deck = Cards.shuffle(52);
        while(main.getDeck().size() < 12) {
            Card c = deck.remove(0);
            if(c.isFace()) deck.add(c);
            else main.getDeck().add(c);
        }
        main.setGridHeight(3);
        main.setGridWidth(4);
        main.setX(300);
        main.setY(140);
        main.setId(MAIN);
        table.getPlacements().add(main);
    }

    private void deselectAll() {
        if(a != null) { a.setHighlight(false); a = null; }
    }

    @Override
    public Tableau select(String id, int gridX, int gridY) {
        int index = gridX + gridY*main.getGridWidth();
        Card c = main.getDeck().get(index);
        if (c.isHighlight() || c.isFace()) {
            deselectAll();
        } else if (a==null) {
            a = c;
            c.setHighlight(true);
        } else if (a.rank()+c.rank() == 11) {
            Cards.dealOver(deck, main.getDeck(), a);
            Cards.dealOver(deck, main.getDeck(), c);
            a = null;
        } else {
            a.setHighlight(false);
            c.setHighlight(true);
            a = c;
        }
        return table;
    }
}
