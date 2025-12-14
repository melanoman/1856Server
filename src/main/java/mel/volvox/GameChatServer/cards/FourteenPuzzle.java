package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Placement;
import mel.volvox.GameChatServer.comm.cards.Tableau;

import java.util.List;

public class FourteenPuzzle extends CardGame {
    Placement[] pile = new Placement[12];
    int selectedIndex = -1;
    Card selection = null;

    private Placement makePile(int index, List<Card> deck) {
        Placement out = new Placement();
        out.setX((index < 4 ? 35 : 30) + index*60);
        out.setY(index < 4 ? 205 : 175);
        out.setSplay(Placement.SPLAY_DOWN);
        out.setId(""+index);
        Cards.deal(deck, out.getDeck(), (index < 4) ? 5 : 4, true);
        table.getPlacements().add(out);
        return out;
    }

    @Override
    public void init() {
        super.init();
        List<Card> deck = Cards.shuffle(52);
        for(int i=0; i<12; i++) pile[i] = makePile(i, deck);
        checkResult();
    }

    private void checkResult() {
        boolean found = false;
        boolean[] used = new boolean[14];
        for(int i = 0; i < 12; i++) {
            if(pile[i].isEmpty()) continue;
            found = true;
            int rank = pile[i].getDeck().get(0).rank();
            if(used[14-rank]) return;
            used[rank] = true;
        }
        table.setResult(found ? Tableau.LOSE : Tableau.WIN);
    }

    private void clearSelection() {
        if (selection != null) {
            selection.setHighlight(false);
            selectedIndex = -1;
            selection = null;
        }
    }

    private void setSelection(Card c, int index) {
        selectedIndex = index;
        selection = c;
        c.setHighlight(true);
    }

    private void removeCard(int index) {
        Placement p = pile[index];
        p.getDeck().remove(0);
        p.setX(p.getX() - 5);
        p.setY(p.getY() - 30);
    }

    @Override
    public Tableau select(String id, int gridX, int gridY) {
        int index = Integer.parseInt(id);
        if(pile[index].isEmpty()) return table;
        Card c = pile[index].getDeck().get(0);
        if(c.isHighlight()) clearSelection();
        else if(selection == null) setSelection(c, index);
        else if(selection.rank() + c.rank() == 14) {
            removeCard(selectedIndex);
            removeCard(index);
            clearSelection();
            checkResult();
        } else {
            clearSelection();
            setSelection(c, index);
        }
        return table;
    }
}
