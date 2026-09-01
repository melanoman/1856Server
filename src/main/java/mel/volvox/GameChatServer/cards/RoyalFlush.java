package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Placement;
import mel.volvox.GameChatServer.comm.cards.Tableau;

public class RoyalFlush extends SingleSelectionGame {
    DrawDeck deck = new DrawDeck(52);
    Placement[] pile = new Placement[5];
    Placement[] to = new Placement[5];
    String currentPile = "0";
    int max = 4;
    int suit = -1;

    @Override void init() {
        for(int i=0; i<5; i++) {
            pile[i] = new Placement();
            pile[i].setId(""+i);
            pile[i].setX(200+i*80);
            pile[i].setY(180);
            table.getPlacements().add(pile[i]);
        }
        dealAll();
    }

    @Override public Tableau select(String id, int gridX, int gridY) {
        if (id.equals(currentPile)) discardOne();
        return table;
    }

    private void dealAll() {
        int stack = 0;
        while(!deck.isEmpty()) {
            deck.dealOnto(pile[stack].getDeck(), false);
            stack++;
            if(stack > max) stack = 0;
        }
        flipDeck(pile[0]);
        currentPile = "0";
    }

    private void discardOne() {
        int index = currentPile.charAt(0) - '0';
        Card c = pile[index].getDeck().get(0);
        if(suit == -1) {
            if(c.rankAH() > 9) {
                suit = c.suit();
                nextPile();
            } else {
                pile[index].getDeck().remove(0);
                if(pile[index].getDeck().isEmpty()) nextPile();
            }
        } else {
            if(c.suit() == suit && c.rankAH() > 9) {
                nextPile();
            } else {
                pile[index].getDeck().remove(0);
                if(pile[index].getDeck().isEmpty()) nextPile();
            }
        }
    }

    private void nextPile() {
        int index = currentPile.charAt(0) - '0';
        if(index == max)  {
            if(max == 0) {
                checkResult();
                pile[0].setSplay(Placement.SPLAY_DOWN);
                return; //GAME OVER
            }
            for(int i=max; i>=0; i--) {
                deck.redealFrom(pile[i].getDeck(), false);
            }
            max --;
            dealAll();
        } else {
            currentPile = pile[index+1].getId();
            flipDeck(pile[index+1]);
        }
    }

    private void checkResult() {
        if(pile[0].getDeck().size() == 5) {
            for(Card c:pile[0].getDeck()) {
                if(c.rankAH() < 10) { lose(); return; }
            }
            win();
        } else lose();
    }
}
