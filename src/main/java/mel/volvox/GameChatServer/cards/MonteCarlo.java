package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Placement;
import mel.volvox.GameChatServer.comm.cards.Tableau;

import java.util.Objects;

public class MonteCarlo extends CardGame {
    private final DrawDeck drawDeck = new DrawDeck(52);
    private final Placement main = new Placement();
    private Card selection = null;
    private int selectionIndex = NO_SELECTION;
    private int selX = NO_SELECTION;
    private int selY = NO_SELECTION;

    @Override
    public void init() {
        super.init();
        main.setId(MAIN);
        main.setX(100);
        main.setY(75);
        main.setGridHeight(5);
        main.setGridWidth(5);
        drawDeck.deal(main.getDeck(), 25, true);
        drawDeck.getPlacement().setId(DRAW);
        drawDeck.getPlacement().setX(555);
        drawDeck.getPlacement().setY(215);
        drawDeck.setRedealAllowed(true);
        table.getPlacements().add(main);
        table.getPlacements().add(drawDeck.getPlacement());
    }

    @Override
    public Tableau select(String id, int gridX, int gridY) {
        if (DRAW.equals(id)) {
            clearSelection();
            main.getDeck().removeIf(Objects::isNull);
            drawDeck.deal(main.getDeck(), 25-main.getDeck().size(), true);
            checkResult();
        } else {
            int index = gridX + 5*gridY;
            if (index >= main.getDeck().size()) clearSelection();
            Card c = main.getDeck().get(index);
            if (c == null || c.isHighlight()) {
                clearSelection();
            } else if (selection != null && selection.rank() == c.rank() && adjacent(gridX, gridY, selX, selY)) {
                main.getDeck().set(selectionIndex, null);
                main.getDeck().set(index, null);
                clearSelection();
                checkResult();
            } else {
                clearSelection();
                selection = c;
                selectionIndex = index;
                selX = gridX;
                selY = gridY;
                c.setHighlight(true);
                checkResult();
            }
        }
        return table;
    }

    /**
     * note every space is adjacent to itself. includes diagonals
     */
    private boolean adjacent(int gx, int gy, int sx, int sy) {
        int dx = Math.abs(gx - sx);
        int dy = Math.abs(gy - sy);
        return dx<2 && dy<2;
    }

    private void clearSelection() {
        if (selection != null) selection.setHighlight(false);
        selection = null;
        selectionIndex = NO_SELECTION;
        selX = NO_SELECTION;
        selY = NO_SELECTION;
    }

    private void checkResult() {
        if(drawDeck.isEmpty() && main.isEmpty()) {
            table.setResult(Tableau.WIN);
            table.getPlacements().clear();
        } else {
            for (Card c: main.getDeck()) if (c == null) return;
            for (int i=0; i<5; i++) for(int j=0; j<5; j++) {
                if(hasMatch(i, j)) return;
            }
            table.setResult(Tableau.LOSE);
            drawDeck.setRedealAllowed(false);
        }
    }

    private Card cardAt(int col, int row) {
        if(col < 0 || row < 0 || col > 4 || row > 4) return null;
        int index = col + row*5;
        return main.getDeck().size() > index ? main.getDeck().get(index) : null;
    }

    private boolean matches(int col, int row, int rank) {
        Card c = cardAt(col, row);
        if (c == null) return false;
        return c.rank() == rank;
    }

    private boolean hasMatch(int col, int row) {
        Card c = cardAt(col, row);
        if (c == null) return false;
        int rank = c.rank();
        return  matches(col - 1, row, rank) ||
                matches(col + 1, row, rank) ||
                matches(col - 1, row + 1, rank) ||
                matches(col, row + 1, rank) ||
                matches(col + 1, row + 1, rank);
    }
}
