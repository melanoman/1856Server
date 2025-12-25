package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Placement;
import mel.volvox.GameChatServer.comm.cards.Tableau;

import java.util.List;

public class Accordian extends SingleSelectionGame {
    Placement main = new Placement();
    List<Card> deck = Cards.shuffle(52);

    @Override
    public void init() {
        super.init();
        main.setId(MAIN);
        main.setDeck(deck);
        main.setY(120);
        main.setX(40);
        main.setGridWidth(13);
        main.setGridHeight(4);
        table.getPlacements().add(main);
        for(Card c:main.getDeck()) c.setExposed(true);
        checkResult();
    }

    @Override
    public Tableau select(String id, int gridX, int gridY) {
        int index = gridX + gridY*main.getGridWidth();
        if (index < deck.size()) {
            Card c = deck.get(index);
            if (index == selectedIndex) {
                clearSelection();
            } else if (
                    selection != null &&
                    (selectedIndex == index+1 || selectedIndex == index+3) &&
                    (c.rank() == selection.rank() || c.suit() == selection.suit())
            ) {
                deck.set(index, selection);
                deck.remove(selectedIndex);
                clearSelection();
                checkResult();
            } else shiftSelection(index, c);
        }
        return table;
    }

    private static boolean match(Card a, Card b) {
        return a.suit() == b.suit() || a.rank() == b.rank();
    }

    private void checkResult() {
        if (deck.size() == 1) {
            table.setResult(Tableau.WIN);
        } else {
            Card a = deck.get(0);
            Card b = deck.get(1);
            if (match (a, b)) return;
            if (deck.size() > 2) {
                Card c = deck.get(2);
                if (match(b, c)) return;

                for (int index = 3; index < deck.size(); index++) {
                    Card d = deck.get(index);
                    if (match(a, d) || match(c, d)) return;
                    a=b; b=c; c=d;
                }
                table.setResult(Tableau.LOSE);
            }
        }
    }
}
