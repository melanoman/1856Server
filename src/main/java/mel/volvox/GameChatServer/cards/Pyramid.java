package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Placement;
import mel.volvox.GameChatServer.comm.cards.Tableau;

import java.util.List;

public class Pyramid extends CardGame {
    DrawDeck drawDeck = new DrawDeck(52);
    Placement playPile = new Placement();
    Card selection = null;
    int selectionIndex;
    private int redeals = 3;

    Placement[] row;

    private void makeRow(int index) {
        Placement p = new Placement();
        p.setId(""+index);
        row[index] = p;
        p.setX(350 - index*27);
        p.setY(50 + index*35);
        p.setGridWidth(index+1);
        drawDeck.deal(p.getDeck(), index+1, true);
        row[index] = p;
        table.getPlacements().add(p);
    }

    @Override public void init() {
        super.init();
        row = new Placement[7];
        for(int i=0; i<7; i++) makeRow(i);
        drawDeck.getPlacement().setX(300);
        drawDeck.getPlacement().setY(350);
        drawDeck.getPlacement().setId(DRAW);
        drawDeck.setRedealAllowed(true);
        table.getPlacements().add(drawDeck.getPlacement());
        playPile.setId(PLAY);
        playPile.setX(400);
        playPile.setY(350);
        table.getPlacements().add(playPile);
    }

    private boolean unavailable(int r, int c) {
        if (row[r].getDeck().get(c) == null) return true;
        if (r == 6) return false;
        return row[r+1].getDeck().get(c) != null || row[r+1].getDeck().get(c+1) != null;
    }

    @Override
    public Tableau select(String id, int gridX, int gridY) {
        if(DRAW.equals(id)) {
            if(!drawDeck.isEmpty()) {
                if (drawDeck.isTopExposed()) {
                    if(drawDeck.peek().rank() == 13) {
                        drawDeck.draw();
                        clearSelection();
                        checkResult();
                    } else {
                        drawDeck.dealOnto(playPile, true);
                        clearSelection();
                    }
                } else {
                    selection = drawDeck.exposeTop();
                    selectionIndex = DRAW_PILE;
                    selection.setHighlight(true);
                }
            } else {
                if (redeals > 0) {
                    redeals--;
                    drawDeck.redealFrom(playPile.getDeck(), false);
                    if(redeals == 0) drawDeck.setRedealAllowed(false);
                }
                checkResult();
            }
        } else if (PLAY.equals(id)) {
            if (drawDeck.isTopExposed()) {
                if(!playPile.isEmpty() && selection.rank() + playPile.getDeck().get(0).rank() == 13) {
                    playPile.getDeck().remove(0);
                    removeSelection(1);
                } else {
                    drawDeck.dealOnto(playPile, true);
                    clearSelection();
                }
            } else if (!playPile.isEmpty()) {
                if(selectionIndex == PLAY_PILE) {
                    clearSelection();
                } else if (selection != null && selection.rank() + playPile.getDeck().get(0).rank() == 13) {
                    playPile.getDeck().remove(0);
                    removeSelection(2);
                } else {
                    if(selection != null) selection.setHighlight(false);
                    selectionIndex = PLAY_PILE;
                    selection = playPile.getDeck().get(0);
                    selection.setHighlight(true);
                }
            }
        } else {
            int rowNum = Integer.parseInt(id);
            if(unavailable(rowNum, gridX)) { return table; }
            Placement p = row[rowNum];
            Card c = p.getDeck().get(gridX);
            if (c.isHighlight()) {
                c.setHighlight(false);
                selectionIndex = NO_SELECTION;
                selection = null;
            } else if(c.rank() == 13) {
                p.getDeck().set(gridX, null);
                checkResult();
            } else if (selection != null && selection.rank() + c.rank() == 13) {
                p.getDeck().set(gridX, null);
                removeSelection(3);
            } else {
                if(drawDeck.isTopExposed()) {
                    drawDeck.dealOnto(playPile, true);
                    clearSelection();
                }
                if(selection != null) selection.setHighlight(false);
                c.setHighlight(true);
                selection = c;
                selectionIndex = rowNum;
            }
        }
        return table;
    }

    private void clearSelection() {
        if(selection != null) selection.setHighlight(false);
        selectionIndex = NO_SELECTION;
        selection = null;
    }

    private void removeSelection(int debug) {
        switch(selectionIndex) {
            case DRAW_PILE -> drawDeck.draw();
            case PLAY_PILE -> playPile.getDeck().remove(0);
            case NO_SELECTION -> throw new IllegalStateException("Nothing Selected"+debug);
            default -> removeSelectionFromRow();
        }
        selectionIndex = NO_SELECTION;
        selection = null;
        checkResult();
    }

    private void removeSelectionFromRow() {
        List<Card> r = row[selectionIndex].getDeck();
        r.set(r.indexOf(selection), null);
    }

    private void checkResult() {
        if (drawDeck.isEmpty() && playPile.isEmpty() && row[0].getDeck().get(0) == null) {
            table.setResult(Tableau.WIN);
        } else if (redeals > 0 || !drawDeck.isEmpty()) {
            return;
        } else {
            boolean[] used = new boolean[14];
            //for each card in pyramid
            for(int i=6; i>=0; i--) {
                for(int j=i-1; j>=0; j--) {
                    if(unavailable(i, j)) continue;
                    //return if playable else mark used
                    Card c = row[i].getDeck().get(j);
                    if(used[13-c.rank()]) return;
                    used[c.rank()] = true;
                }
            }
            // return if top of playPile is playable
            if (!playPile.isEmpty() && used[13-playPile.getDeck().get(0).rank()]) return;
            // LOSER
            table.setResult(Tableau.LOSE);
        }
    }
}
