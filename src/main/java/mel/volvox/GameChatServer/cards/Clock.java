package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Placement;
import mel.volvox.GameChatServer.comm.cards.Tableau;

public class Clock extends SingleSelectionGame {
    DrawDeck deck = new DrawDeck(52);
    Placement[] pile = new Placement[13];
    Placement discard = new Placement();

    static final int XC = 375;
    static final int YC = 200;
    static final int[] dx = {
            50, 100, 115, 100, 50, 0,
            -50, -100, -115, -100, -50, 0, -30
    };
    static final int[] dy = {
            100, 65, 0, -65, -100, -115,
            -100, -65, 0, 65, 100, 115, 0
    };
    @Override void init() {
        for(int i=0; i<13; i++) {
            pile[i] = new Placement();
            pile[i].setId(""+i);
            pile[i].setX(XC + dx[i]);
            pile[i].setY(YC - dy[i]);
            pile[i].getDeck().add(deck.draw());
            pile[i].getDeck().add(deck.draw());
            pile[i].getDeck().add(deck.draw());
            pile[i].getDeck().add(deck.draw());
            table.getPlacements().add(pile[i]);

            discard.setId(DRAW);
            discard.setX(XC+30);
            discard.setY(YC);
            table.getPlacements().add(discard);
        }
        Card c = pile[12].getDeck().remove(0);
        c.setExposed(true);
        discard.getDeck().add(c);
        selection = c;
        selectedIndex = -1;
    }

    @Override public Tableau select(String id, int gridX, int gridY) {
        if (table.getResult() != Tableau.NONE) return table;
        if (DRAW.equals(id)) {
            if(selectedIndex < 0) {
                throw new IllegalStateException("Select card first");
            }
            selection = pile[selectedIndex].getDeck().remove(0);
            selectedIndex = -1;
            discard.getDeck().add(0, selection);
            if(pile[selection.rank()-1].getDeck().isEmpty()) {
                if(discard.getDeck().size()+pile[12].getDeck().size() == 52) win();
                else lose();
            }
        } else {
            int rank = selection.rank();
            int index = Integer.parseInt(id);
            if (selectedIndex == -1) {
                if (rank - 1 != index) {
                    if (rank < 12) throw new IllegalStateException("Choose " + rank + " o'clock instead");
                    else throw new IllegalStateException("Kings draw from the center deck");
                }
                Card c = pile[index].getDeck().get(0);
                c.setExposed(true);
                selectedIndex = index;
                selection = c;
            } else {
                throw new IllegalStateException("Discard selected card");
            }
        }
        return table;
    }
}
