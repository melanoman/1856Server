package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Placement;
import mel.volvox.GameChatServer.comm.cards.Tableau;

import java.util.List;

public class Addition10 extends CardGame {
    List<Card> deck;
    Placement main;
    Card a, b, c;
    int selSuit = Card.NO_SUIT;
    int selVal = 0;

    @Override
    public void init() {
        super.init();
        deck = Cards.shuffle(52);
        main = new Placement();
        Cards.deal(deck, main.getDeck(), 13, true);
        main.setId(MAIN);
        main.setX(280);
        main.setY(65);
        main.setGridWidth(5);
        main.setGridHeight(3);
        Card mover = main.getDeck().get(10);
        main.getDeck().add(mover);
        main.getDeck().add(null);
        main.getDeck().set(10,null);
        table.getPlacements().add(main);
        checkResult();
    }

    private void clearSelection() {
        if(a != null) { a.setHighlight(false); a = null; }
        if(b != null) { b.setHighlight(false); b = null; }
        if(c != null) { c.setHighlight(false); c = null; }
        selSuit = Card.NO_SUIT;
        selVal = 0;
    }

    private void checkResult() {
        if (deck.isEmpty() && main.isEmpty()) {
            table.setResult(Tableau.WIN);
        } else {
            boolean[] rank = new boolean[11];
            int[] suit = new int[4];
            for(Card d: main.getDeck()) {
                if(d==null) continue;
                if(d.rank() > 9) {
                    if(suit[d.suit()] > 2) return;
                    suit[d.suit()]++;
                } else {
                    if(rank[10-d.rank()]) return;
                    rank[d.rank()] = true;
                }
            }
            table.setResult(Tableau.LOSE);
        }
    }

    @Override
    public Tableau select(String id, int gridX, int gridY) {
        int index = gridX + gridY*main.getGridWidth();
        Card d = main.getDeck().get(index);
        if(d==null) return table;
        if(d.isHighlight()) {
            clearSelection();
            return table;
        }
        d.setHighlight(true);
        int val = d.rank();
        if (val > 9) {
            int suit = d.suit();
            if (selSuit != suit) {
                clearSelection();
                selSuit = suit;
                a = d;
            } else if(a==null) {
                a=d;
            } else if(b==null) {
                b=d;
            } else if(c==null) {
                c=d;
            } else {
                Cards.dealOver(deck, main.getDeck(), a);
                Cards.dealOver(deck, main.getDeck(), b);
                Cards.dealOver(deck, main.getDeck(), c);
                Cards.dealOver(deck, main.getDeck(), d);
                a=b=c=null;
                selSuit = Card.NO_SUIT;
                checkResult();
            }
        } else {
            if(selVal + val == 10) {
                Cards.dealOver(deck, main.getDeck(), a);
                Cards.dealOver(deck, main.getDeck(), d);
                a=b=null;
                selVal = 0;
                checkResult();
            } else {
                clearSelection();
                a=d;
                selVal = val;
            }
        }
        return table;
    }
}
