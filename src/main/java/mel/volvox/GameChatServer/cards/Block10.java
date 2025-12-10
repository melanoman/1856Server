package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Placement;
import mel.volvox.GameChatServer.comm.cards.Tableau;

import java.util.List;

public class Block10 extends CardGame {
    List<Card> deck;
    Placement main = new Placement();
    Card a = null;

    @Override
    public void init() {
        super.init();
        deck = Cards.shuffle(52);
        Cards.deal(deck, main.getDeck(), 9, true);
        main.setGridHeight(3);
        main.setGridWidth(3);
        main.setX(320);
        main.setY(135);
        main.setId(MAIN);
        table.getPlacements().add(main);
        checkResult();
    }

    private void deselectAll() {
        if(a != null) { a.setHighlight(false); a = null; }
    }

    private void checkResult() {
        if(deck.isEmpty()) {
            table.setResult(Tableau.WIN);
        } else {
            boolean[] used = new boolean[11];
            boolean face = false;
            for(Card c:main.getDeck()) {
                if(c.rank() == 10) continue;
                if(c.isFace()) {
                    if(face) return;
                    face = true;
                    continue;
                }
                if(used[10-c.rank()]) return;
                used[c.rank()] = true;
            }
            table.setResult(Tableau.LOSE);
        }
    }

    @Override
    public Tableau select(String id, int gridX, int gridY) {
        int index = gridX + gridY*main.getGridWidth();
        Card c = main.getDeck().get(index);
        if (c.isHighlight() || c.rank() == 10) {
            deselectAll();
        } else if (a==null) {
            a = c;
            c.setHighlight(true);
        } else if (a.rank()+c.rank() == 10 || (a.isFace() && c.isFace())) {
            Cards.dealOver(deck, main.getDeck(), a);
            Cards.dealOver(deck, main.getDeck(), c);
            a = null;
            checkResult();
        } else {
            a.setHighlight(false);
            c.setHighlight(true);
            a = c;
        }
        return table;
    }
}
