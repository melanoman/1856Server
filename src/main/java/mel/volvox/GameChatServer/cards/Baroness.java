package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Placement;
import mel.volvox.GameChatServer.comm.cards.Tableau;

public class Baroness extends CardGame {
    Placement[] pile = new Placement[7];
    DrawDeck drawDeck = new DrawDeck(52);

    private Placement makePile(int index, Card c) {
        Placement out = new Placement();
        out.setId(""+index);
        out.setX(220+ index*75);
        out.setY(175);
        out.setGridWidth(1);
        out.setGridHeight(1);
        if (c != null) {
            out.getDeck().add(c);
            c.setExposed(true);
        }
        return out;
    }

    @Override
    public void init() {
        super.init();
        for (int i=0; i<5; i++) {
            pile[i] = makePile(i, drawDeck.draw());
            table.getPlacements().add(pile[i]);
        }
        pile[6] = makePile(-1, null);
        pile[6].setId("6");
        pile[5] = makePile(5, null);
        table.getPlacements().add(pile[5]);
        table.getPlacements().add(pile[6]);
        drawDeck.getPlacement().setX(370);
        drawDeck.getPlacement().setY(250);
        drawDeck.getPlacement().setId("draw");
        drawDeck.getPlacement().getDeck().get(0).setExposed(false);
        table.getPlacements().add(drawDeck.getPlacement());
    }

    private void deal5() {
        for(int i=0; i<5; i++) drawDeck.dealOnto(pile[i].getDeck(), true);
        if(drawDeck.size() == 2) {
            drawDeck.dealOnto(pile[5].getDeck(), true);
            drawDeck.dealOnto(pile[6].getDeck(), true);
        }
    }

    @Override
    public Tableau select(String id, int gridX, int gridY) {
        if(id.equals("draw")) deal5();
        return table;
    }
}
