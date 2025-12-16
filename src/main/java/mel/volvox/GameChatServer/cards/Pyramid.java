package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Placement;
import mel.volvox.GameChatServer.comm.cards.Tableau;

public class Pyramid extends CardGame {
    DrawDeck drawDeck = new DrawDeck(52);
    Placement[] row;

    private void makeRow(int index) {
        Placement p = new Placement();
        p.setId(""+index);
        row[index] = p;
        p.setX(350 - index*27);
        p.setY(50 + index*35);
        p.setGridWidth(index+1);
        Cards.deal(drawDeck.getDeck(), p.getDeck(), index+1, true);
        row[index] = p;
        table.getPlacements().add(p);
    }

    @Override public void init() {
        super.init();
        row = new Placement[7];
        for(int i=0; i<7; i++) makeRow(i);
        drawDeck.getPlacement().setX(350);
        drawDeck.getPlacement().setY(350);
        drawDeck.getPlacement().setId("draw");
        table.getPlacements().add(drawDeck.getPlacement());
    }

    @Override
    public Tableau select(String id, int gridX, int gridY) {
        return table;
    }
}
