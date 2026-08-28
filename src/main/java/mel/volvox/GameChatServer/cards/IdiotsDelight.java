package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Placement;
import mel.volvox.GameChatServer.comm.cards.Tableau;

public class IdiotsDelight extends SingleSelectionGame {

    DrawDeck deck = new DrawDeck(52);
    Placement[] play = new Placement[4];
    Card highlit = null;
    int highlitIndex = -1;

    @Override
    void init() {
        deck.getPlacement().setId(DRAW);
        deck.getPlacement().setX(380);
        deck.getPlacement().setY(375);
        table.getPlacements().add(deck.getPlacement());

        for(int i=0; i<4; i++) {
            play[i] = new Placement();
            play[i].setId("play"+i);
            play[i].setX(260 + i*80);
            play[i].setY(250);
            play[i].setSplay(Placement.SPLAY_DOWN);
            deck.dealOnto(play[i], true);
            table.getPlacements().add(play[i]);
        }
    }

    @Override
    public Tableau select(String id, int gridX, int gridY) {
        if(DRAW.equals(id)) {
            unsetHighlight();
            for(int i=0; i<4; i++) {
                if(!deck.isEmpty()) deck.dealOnto(play[i], true);
            }
        } else if(id.startsWith("play")) {
            int index = Integer.parseInt(id.substring(4));
            if(play[index].getDeck().isEmpty()) {
                moveHighlitCard(index);
            } else {
                if (killable(index)) {
                    killSelection(index);
                } else {
                    setHighlight(index);
                }
            }
        }
        checkResult();
        return table;
    }

    private boolean killable(int index) {
        if(play[index].getDeck().isEmpty()) return false;
        Card c = play[index].getDeck().get(0);
        for (int i = 0; i < 4; i++) {
            if (i == index || play[i].getDeck().isEmpty()) continue;
            Card cc = play[i].getDeck().get(0);
            if (cc.suit() == c.suit() && cc.rankAH() > c.rankAH()) {
                return true;
            }
        }
        return false;
    }

    private void setHighlight(int index) {
        unsetHighlight();
        highlit = play[index].getDeck().get(0);
        highlit.setHighlight(true);
        highlitIndex = highlit == null ? -1 : index;
    }

    private void unsetHighlight() {
        if (highlit != null) highlit.setHighlight(false);
        highlit = null;
        highlitIndex = -1;
    }

    private void killSelection(int index) {
        play[index].getDeck().remove(0);
        unsetHighlight();
    }

    private void moveHighlitCard(int target) {
        if(highlitIndex == -1) return;
        play[highlitIndex].getDeck().remove(highlit);
        play[target].getDeck().add(0, highlit);
        unsetHighlight();
    }

    private boolean loneAce(int index) {
        if(play[index].getDeck().size() != 1) return false;
        return play[index].getDeck().get(0).rank() == 1;
    }

    private void checkResult() {
        if (!deck.isEmpty()) return; // can't win or lose with cards to draw
        boolean loaded = false;
        for(int i=0; i<4; i++) {
            if(killable(i)) return;
            if(!loneAce(i)) loaded = true;
        }
        if(loaded) lose();
        else win();
    }
}
