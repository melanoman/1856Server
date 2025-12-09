package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Placement;
import mel.volvox.GameChatServer.comm.cards.Tableau;

import java.util.ArrayList;
import java.util.List;

public class Addition15 extends CardGame {
    private List<Card> deck;
    Placement main = new Placement();
    List<Card> selection = new ArrayList<>();
    int cardSum = 0;
    int saveSuit = Card.NO_SUIT;

    public void init() {
        super.init();
        deck = Cards.shuffle(52);
        Cards.deal(deck, main.getDeck(), 16, true);
        main.setGridHeight(4);
        main.setGridWidth(4);
        main.setX(275);
        main.setY(120);
        main.setId(MAIN);
        table.getPlacements().add(main);
        checkResult();
    }

    private boolean findFifteen(int[] used, int sum, int max, int block) {
        if(sum > 15) return false;
        if(sum == 15) return true;
        if(used[max] > block && findFifteen(used, sum+max, max, block+1)) return true;
        for(int i=max-1; i>0; i--) {
            if(used[i] == 0) continue;
            if(findFifteen(used, sum+i, i, 1)) return true;
        }
        return false;
    }

    private void checkResult() {
        if(deck.isEmpty()) {
            table.setResult(Tableau.WIN);
        } else {
            int[] used = new int[14];
            for(Card c: main.getDeck()) {
                if(c == null) continue;
                used[c.rank()]++;
            }
            for(int i=9; i>3; i--) {
                if(used[i]>0 && findFifteen(used, i, i, 1)) return;
            }
        }
        table.setResult(Tableau.LOSE);
    }

    private void deselect() {
        for(Card c: selection) {
            c.setHighlight(false);
        }
        cardSum = 0;
        saveSuit = Card.NO_SUIT;
        selection = new ArrayList<>();
    }

    @Override
    public Tableau select(String id, int gridX, int gridY) {
        if(!MAIN.equals(id)) return table;
        if(table.getResult() != Tableau.NONE) return table;
        if (gridX > 3 || gridY > 3) return table;

        int index = gridX + gridY* main.getGridWidth();
        Card c = main.getDeck().get(index);
        if(c.isHighlight()) {
            deselect();
            return table;
        }
        c.setHighlight(true);
        int rank = c.rank();
        if(rank > 9) {
            if(c.suit() != saveSuit) {
                deselect();
                saveSuit = c.suit();
            }
            selection.add(c);
            if(selection.size() == 4) {
                dealOver();
            }
        } else {
            if(cardSum + rank > 15 || saveSuit != Card.NO_SUIT) {
                deselect();
            }
            cardSum += rank;
            selection.add(c);
            if(cardSum == 15) dealOver();
        }
        return table;
    }

    private void dealOver() {
        for(Card c: selection) Cards.dealOver(deck, main.getDeck(), c);
        selection = new ArrayList<>();
        cardSum = 0;
        saveSuit = Card.NO_SUIT;
        checkResult();
    }
}
