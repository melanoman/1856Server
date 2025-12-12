package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Placement;
import mel.volvox.GameChatServer.comm.cards.Tableau;

public class Baroness extends CardGame {
    Placement[] pile = new Placement[7];
    DrawDeck drawDeck = new DrawDeck(52);
    Card selection = null;
    int selectionIndex;

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

    private Tableau select(int index) {
        if(pile[index].getDeck().isEmpty()) return table;

        Card c = pile[index].getDeck().get(0);
        if(selection == null) {
            if(c.rank() == 13) {
                pile[index].getDeck().remove(0);
            }
            else {
                selection=c;
                selectionIndex=index;
                c.setHighlight(true);
            }
        } else if(selection.rank()+c.rank() == 13) {
            pile[selectionIndex].getDeck().remove(0);
            pile[index].getDeck().remove(0);
            selection = null;
        } else {
            selection.setHighlight(false);
            selection=c;
            selectionIndex=index;
            c.setHighlight(true);
        }
        return table;
    }

    @Override
    public Tableau select(String id, int gridX, int gridY) {
        if(id.equals("draw")) deal5();
        else {
            try {
                return select(Integer.parseInt(id));
            } catch (NumberFormatException e) {
                throw new IllegalStateException("unknown click target");
            }
        }
        return table;
    }
}
