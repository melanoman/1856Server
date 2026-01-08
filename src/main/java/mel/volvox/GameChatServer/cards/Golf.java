package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Placement;
import mel.volvox.GameChatServer.comm.cards.Tableau;

public class Golf extends CardGame {
    Placement[] column = new Placement[7];
    DrawDeck drawDeck = new DrawDeck(52);
    Placement playPile = new Placement();

    @Override
    public void init() {
        super.init();
        for (int i=0; i<7; i++) {
            column[i] = makeColumn(i);
            for(int j=0; j<5; j++) {
                column[i].getDeck().add(drawDeck.draw(true));
            }
            table.getPlacements().add(column[i]);
        }
        drawDeck.getPlacement().setId(DRAW);
        drawDeck.getPlacement().setX(320);
        drawDeck.getPlacement().setY(275);
        table.getPlacements().add(drawDeck.getPlacement());

        playPile.setId(PLAY);
        playPile.setX(380);
        playPile.setY(275);
        table.getPlacements().add(playPile);
    }

    private Placement makeColumn(int index) {
        Placement out = new Placement();
        out.setX(180 + index*60);
        out.setY(175);
        out.setId(""+index);
        out.setSplay(Placement.SPLAY_DOWN);
        return out;
    }

    private Tableau tryPlayCard(Placement p) {
        if(p.isEmpty()) return table;
        int rank = p.getDeck().get(0).rank();
        int target = playPile.getDeck().get(0).rank();
        if (rank == target + 1 || (rank == target - 1 && rank < 12)) {
            playPile.getDeck().add(0, p.getDeck().remove(0));
            checkResult();
        }
        return table;
    }

    private void checkResult() {
        boolean found = false;
        for(Placement p: column) if(!p.isEmpty()) found = true;
        if(!found) table.setResult(Tableau.WIN);
        else {
            if(drawDeck.isEmpty()) {
                found = false;
                int target = playPile.getDeck().get(0).rank();
                for(Placement p:column) {
                    if(p.isEmpty()) continue;
                    int rank = p.getDeck().get(0).rank();
                    if(rank == target + 1 || (rank == target - 1 && rank < 12)) found = true;
                }
                if (!found) table.setResult(Tableau.LOSE);
            }
        }
    }

    @Override
    public Tableau select(String id, int gridX, int gridY) {
        if(id.equals(DRAW)) {
            if (!drawDeck.isEmpty()) playPile.getDeck().add(0, drawDeck.draw(true));
            checkResult();
        } else {
            for (Placement p : column) {
                if (p.getId().equals(id)) return tryPlayCard(p);
            }
        }
        return table;
    }

}
