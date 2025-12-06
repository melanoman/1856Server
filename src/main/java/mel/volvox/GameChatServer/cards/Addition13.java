package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Placement;
import mel.volvox.GameChatServer.comm.cards.Tableau;

import java.util.List;

public class Addition13 extends CardGame {
    List<Card> deck;
    Placement main = new Placement();
    Card selection = null;
    int selectedIndex = -1;

    @Override
    public void init() {
        super.init();
        deck = Cards.shuffle(52);
        Cards.deal(deck, main.getDeck(), 10, true);
        main.setGridHeight(2);
        main.setGridWidth(5);
        main.setX(265);
        main.setY(165);
        main.setId(MAIN);
        table.getPlacements().add(main);
    }

    @Override
    public Tableau select(String id, int gridX, int gridY) {
        if(!MAIN.equals(id)) return table;
        if(table.getResult() != Tableau.NONE) return table;
        if (gridX > 4 || gridY > 1) return table;

        int index = gridX + gridY* main.getGridWidth();
        Card c = main.getDeck().get(index);
        int value = cardValue(c);

        if(value == 13) {
            Cards.dealOver(deck, main.getDeck(), index);
            checkResult();
        } else if(selection == null) {
            selection = c;
            selectedIndex = index;
            c.setHighlight(true);
        } else if(cardValue(selection) + value == 13) {
            Cards.dealOver(deck, main.getDeck(), index);
            Cards.dealOver(deck, main.getDeck(), selectedIndex);
            selection = null;
            checkResult();
        } else {
            selection.setHighlight(false);
            c.setHighlight(true);
            selection = c;
            selectedIndex = index;
        }
        return table;
    }

    private void checkResult() {
        if(deck.isEmpty() && main.isEmpty()) {
            table.setResult(Tableau.WIN);
            return;
        }

        boolean[] used = new boolean[13];
        used[0] = true;
        for(int i=1; i<13; i++) used[i] = false;
        for(Card c: main.getDeck()) {
            if(c==null) continue;
            int val = cardValue(c);
            if(used[13-val]) return;
            else used[val] = true;
        }
        table.setResult(Tableau.LOSE);
    }

    private int cardValue(Card c) {
        return c.rank();
    }
}
