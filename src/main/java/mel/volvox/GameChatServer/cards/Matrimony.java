package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Placement;
import mel.volvox.GameChatServer.comm.cards.Tableau;

import java.util.ArrayList;
import java.util.List;

public class Matrimony extends SingleSelectionGame {
    public static int QUEEN_HEARTS = 37;
    public static int KING_HEARTS = 38;
    private final List<Card> deck = Cards.shuffle(52);
    private final Placement main = new Placement();

    @Override
    public void init() {
        super.init();

        //move QH to head of deck
        deck.removeIf(x -> x.getId() == 37);
        Card qh = new Card(QUEEN_HEARTS, false, false, false);
        deck.add(0, qh);

        //move KH to end of deck
        deck.removeIf(x -> x.getId() == 38);
        Card kh = new Card(KING_HEARTS, false, false, false);
        deck.add(kh);

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
        int index = gridX + 13*gridY;
        if (index < deck.size()) {
            Card c = deck.get(index);
            int dx = Math.abs(selectedIndex - index);
            if (selection == null || dx == 0) {
                shiftSelection(index, c);
            } else if ((dx == 2 || dx == 3) &&
                       (selection.suit() == c.suit() || selection.rank() == c.rank())
            ) {
                    int start = Math.min(selectedIndex, index);
                    for (int i = 1; i < dx; i++) deck.remove(start + 1);
                    clearSelection();
                    checkResult();
            } else shiftSelection(index, c);
        } else clearSelection();
        return table;
    }

    private void checkResult() {
        if (deck.size() == 2) table.setResult(Tableau.WIN);
        else if (deck.size() > 4) {
            List<Card> recent = new ArrayList<>();
            for(Card c:deck) if (matchShift(c, recent)) return;
            table.setResult(Tableau.LOSE);
        }
    }

    private boolean matchShift(Card c, List<Card> recent) {
        if(recent.size() >= 2) {
            Card a = recent.get(0);
            if(a.suit() == c.suit() || a.rank() == c.rank()) return true;
        }
        if(recent.size() > 2) {
            recent.remove(0);
            Card a = recent.get(0);
            if(a.suit() == c.suit() || a.rank() == c.rank()) return true;
        }
        recent.add(c);
        return false;
    }
}
