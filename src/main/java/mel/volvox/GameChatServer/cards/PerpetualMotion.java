package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Placement;
import mel.volvox.GameChatServer.comm.cards.Tableau;

import java.util.HashSet;
import java.util.Set;

public class PerpetualMotion extends SingleSelectionGame {
    DrawDeck deck = new DrawDeck(52);
    Placement[] play = new Placement[4];
    boolean set = false;
    Set<String> used = new HashSet<>();

    @Override void init() {
        deck.getPlacement().setId(DRAW);
        deck.getPlacement().setX(380);
        deck.getPlacement().setY(375);
        deck.setRedealAllowed(true);
        table.getPlacements().add(deck.getPlacement());

        for(int i=0; i<4; i++) {
            play[i] = new Placement();
            play[i].setId(""+i);
            play[i].setX(260 + i*80);
            play[i].setY(250);
            deck.dealOnto(play[i], true);
            table.getPlacements().add(play[i]);
        }
    }

    @Override public Tableau select(String id, int gridX, int gridY) {
        if(table.getResult() != Tableau.NONE) throw new IllegalStateException("Game is over");
        if (id.equals(DRAW)) {
            if(set) for(int i=0; i<4; i++) {
                play[i].getDeck().get(0).setHighlight(false);
                set = false;
            } else clearSelection();
            if(deck.isEmpty()) { //reset deck
                for(int i=3; i>=0; i--) deck.redealFrom(play[i].getDeck(), false);
                String checksum = deck.getDeckString();
                if(used.contains(checksum)) lose();
                used.add(checksum);
            } else { //deal4
                for (int i=0; i<4; i++) deck.dealOnto(play[i], true);
                int rank0 = play[0].getDeck().get(0).rank();
                int rank1 = play[1].getDeck().get(0).rank();
                int rank2 = play[2].getDeck().get(0).rank();
                int rank3 = play[3].getDeck().get(0).rank();
                if(rank0==rank1 && rank0==rank2 && rank0==rank3) {
                    set = true;
                    play[0].getDeck().get(0).setHighlight(true);
                    play[1].getDeck().get(0).setHighlight(true);
                    play[2].getDeck().get(0).setHighlight(true);
                    play[3].getDeck().get(0).setHighlight(true);
                }
            }
        } else {
            int index = id.charAt(0) - '0';
            if(set) {
                set = false;
                for(int i=0; i<4; i++) play[i].getDeck().remove(0);
                checkWin();
            } else if(index < selectedIndex && selection.rank() == play[index].getDeck().get(0).rank()) {
                play[selectedIndex].getDeck().remove(0);
                play[index].getDeck().add(0, selection);
                shiftSelection(index, selection);
            } else {
                shiftSelection(index, play[index].getDeck().get(0));
            }
        }
        return table;
    }

    private void checkWin() {
        if(!deck.isEmpty()) return;
        for(int i=0; i<4; i++) if(!play[i].getDeck().isEmpty()) return;
        win();
    }
}
