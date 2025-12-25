package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Placement;
import mel.volvox.GameChatServer.comm.cards.Tableau;

import java.util.List;

public class Block11 extends SingleSelectionGame {
    List<Card> deck;
    Placement main = new Placement();
    boolean seeded = false;

    @Override
    public void init() {
        super.init();
        deck = Cards.shuffle(52);
        while(main.getDeck().size() < 12) {
            Card c = deck.remove(0);
            c.setExposed(true);
            if(c.isFace()) deck.add(c);
            else main.getDeck().add(c);
        }
        main.setGridHeight(3);
        main.setGridWidth(4);
        main.setX(300);
        main.setY(140);
        main.setId(MAIN);
        table.getPlacements().add(main);
        checkResult();
    }


    private void checkResult() {
        if(deck.isEmpty()) table.setResult(Tableau.WIN);
        else {
            boolean[] used = new boolean[11];
            for(Card c:main.getDeck()) {
                if(c.isFace()) continue;
                if(used[11-c.rank()]) return;
                used[c.rank()] = true;
            }
            table.setResult(Tableau.LOSE);
        }
    }

    @Override
    public Tableau select(String id, int gridX, int gridY) {
        int index = gridX + gridY*main.getGridWidth();
        Card c = main.getDeck().get(index);
        if (c == null) return table;
        if (c.isHighlight() || c.isFace()) {
            clearSelection();
        } else if (selection==null) {
            shiftSelection(index, c);
        } else if (selection.rank()+c.rank() == 11) {
            Cards.dealOver(deck, main.getDeck(), selection);
            Cards.dealOver(deck, main.getDeck(), c);
            clearSelection();
            checkResult();
        } else {
            shiftSelection(index, c);
        }
        return table;
    }
}
