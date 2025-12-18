package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Placement;
import mel.volvox.GameChatServer.comm.cards.Tableau;

import java.util.ArrayList;
import java.util.List;

public class Nestor extends CardGame {
    private final Placement[] column = new Placement[12];
    private final DrawDeck drawDeck = new DrawDeck(52);
    private final Placement playPile = new Placement();

    Card selection = null;
    int selectionIndex = NO_SELECTION;

    private Placement makeColumn(int index) {
        Placement out = new Placement();
        out.setX(30 + index*60);
        out.setY(175);
        out.setId(""+index);
        out.setSplay(Placement.SPLAY_DOWN);
        return out;
    }

    private boolean hasRank(Placement p, int rank) {
        for(Card c: p.getDeck()) if (c.rank() == rank) return true;
        return false;
    }

    @Override
    public void init() {
        super.init();
        for(int i=0; i<12; i++) {
            column[i] = makeColumn(i);
            table.getPlacements().add(column[i]);
        }
        for(int i=0; i<4; i++) for(int j=0; j<12; j++) {
            Card c = drawDeck.draw();
            while(hasRank(column[j], c.rank())) {
                drawDeck.add(c);
                c = drawDeck.draw();
            }
            column[j].getDeck().add(0, c);
            c.setExposed(true);
        }
        drawDeck.getPlacement().setId(DRAW);
        drawDeck.getPlacement().setX(350);
        drawDeck.getPlacement().setY(275);
        table.getPlacements().add(drawDeck.getPlacement());
    }

    @Override
    public Tableau select(String id, int gridX, int gridY) {
        //TODO all the play rules
        return table;
    }
}
