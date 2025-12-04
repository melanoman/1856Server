package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Placement;
import mel.volvox.GameChatServer.comm.cards.Tableau;

import java.util.List;

public class Addition11 extends CardGame {
    Card a, b;
    int saveSuit = Card.NO_SUIT;
    int saveSum = 0;
    List<Card> deck;
    Placement main;

    @Override
    public void init() {
        super.init();
        deck = Cards.shuffle(52);
        main = new Placement();
        Cards.deal(deck, main.getDeck(), 9, true);
        main.setId(MAIN);
        main.setX(300);
        main.setY(100);
        main.setGridWidth(3);
        main.setGridHeight(3);
        table.getPlacements().add(main);
    }

    private void clearHighlight() {
        if(a != null) a.setHighlight(false);
        if(b != null) b.setHighlight(false);
    }

    @Override
    public Tableau select(String id, int gridX, int gridY) {
        if (!MAIN.equals(id)) return table;
        if (gridX > 2 || gridY > 2) return table;
        if(table.getResult() != Tableau.NONE) return table;
        int index = gridX + gridY*main.getGridWidth();

        Card c = main.getDeck().get(index);
        c.setHighlight(true);
        if(c.isFace()) {
            int suit = c.suit();
            if(saveSuit==suit) {
                if(b==null) {
                    b=c;
                } else {
                    //TODO dealOver trio
                    Cards.dealOver(deck, main.getDeck(), a);
                    Cards.dealOver(deck, main.getDeck(), b);
                    Cards.dealOver(deck, main.getDeck(), c);
                }
            } else {
                clearHighlight();
                if (saveSum > 0) {
                    saveSum = 0;
                    a=c;
                    saveSuit = suit;
                } else { // switch suits
                    saveSuit = suit;
                    a=c;
                }
            }
        } else { //not face
            if (c.cv1to13() + saveSum == 11) {
                Cards.dealOver(deck, main.getDeck(), a);
                Cards.dealOver(deck, main.getDeck(), c);
                checkResult();
                saveSum = 0;
            } else {
                clearHighlight();
                saveSuit = Card.NO_SUIT;
                saveSum = c.cv1to13();
                a=c;
            }
        }
        return table;
    }

    private void checkResult() {
        if (deck.isEmpty()) {
            if(main.getDeck().isEmpty()) table.setResult(Tableau.WIN);
        } else {
            boolean[] sum = new boolean[11];
            int[] suit = new int[4];
            for(Card c:main.getDeck()) {
                if(c.isFace()) {
                    int s = c.suit();
                    if(suit[s] == 2) return;
                    suit[s]++;
                } else {
                    int v = c.cv1to13();
                    if(sum[11-v]) return;
                    sum[v] = true;
                }
            }
            table.setResult(Tableau.LOSE);
        }
    }
}
