package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Placement;
import mel.volvox.GameChatServer.comm.cards.Tableau;

import static mel.volvox.GameChatServer.cards.Clock.XC;
import static mel.volvox.GameChatServer.cards.Clock.YC;
import static mel.volvox.GameChatServer.cards.Clock.dx;
import static mel.volvox.GameChatServer.cards.Clock.dy;

public class EightDayClock extends SingleSelectionGame {
    DrawDeck deck = new DrawDeck(52);
    Placement[] pile = new Placement[13];
    boolean set = false;
    int targetIndex;
    Card targetCard;
    int score = 0;
    int life = 52;

    @Override void init() {
        for (int i=0; i<13; i++) {
            pile[i] = new Placement();
            pile[i].setId(""+i);
            pile[i].setX(XC + dx[i]);
            pile[i].setY(YC - dy[i]);
            pile[i].getDeck().add(deck.draw(true));
            pile[i].getDeck().add(deck.draw(true));
            pile[i].getDeck().add(deck.draw(true));
            pile[i].getDeck().add(deck.draw(true));
            table.getPlacements().add(pile[i]);
        }
        pile[12].setX(XC);
        setSelection(0);
        life = calculateLife();
    }

    @Override public Tableau select(String id, int gridX, int gridY) {
        if (table.getResult() != Tableau.NONE) throw new IllegalStateException("Game is over");
        if (set) {
            discardSet();
        } else {
            int index = Integer.parseInt(id);
            if(index != targetIndex) throw new IllegalStateException("Wrong pile. Check the rules");
            pile[selectedIndex].getDeck().remove(0);
            pile[targetIndex].getDeck().add(selection);
            if(pile[selectedIndex].getDeck().get(0).rank() - 1 == selectedIndex) life = calculateLife();
            setSelection(targetIndex);
            life--;
            if(!set && life < 0 && score < 3) lose();
        }
        return table;
    }

    private void discardSet() {
        for (int i=0; i<13; i++) pile[i].getDeck().remove(0);
        set = false;
        score++;
        if (score == 3) win();
        setSelection(0);
        life = calculateLife();
    }

    private int calculateLife() {
        int out = 0;
        for(int i=0; i<13; i++) {
            if(pile[i].getDeck().get(0).rank() - 1 != i) out ++;
        }
        return out * out * (4-score);
    }

    private void setSelection(int start) {
        int current = start;
        Card c = pile[current].getDeck().get(0);
        if(c.rank()-1 == start) {
            life = calculateLife();
            if(life == 0) {
                markSet();
                return;
            }
        }
        while(true) {
            if(c.rank() - 1 == current) {
                current = next(current);
                c = pile[current].getDeck().get(0);
                if (current == start) return;
            } else {
                shiftSelection(current, c);
                setTarget();
                return;
            }
        }
    }

    private void setTarget() {
        int current = next(selectedIndex);
        while(true) {
            Card c = pile[current].getDeck().get(0);
            if(c.rank() - 1 == current) {
                current = next(current);
            } else {
                targetIndex = current;
                return;
            }
        }
    }

    private int next(int x) {
        return x == 12 ? 0 : x + 1;
    }

    private void markSet() {
        set = true;
        for (int i=0; i<13; i++) pile[i].getDeck().get(0).setHighlight(true);
    }
}
