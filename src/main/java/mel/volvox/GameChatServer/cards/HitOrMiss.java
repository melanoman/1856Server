package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Placement;
import mel.volvox.GameChatServer.comm.cards.Tableau;

public class HitOrMiss extends SingleSelectionGame {
    DrawDeck drawDeck = new DrawDeck(52);
    Placement playPile = new Placement();
    int count = 0;
    int strike = 0;
    boolean gotHit = false;
    boolean lastChance = false;
    boolean justHit = true;

    public static String HIT = "HIT";
    public static String MISS = "MISS";

    @Override void init() {
        drawDeck.getPlacement().setId(DRAW);
        drawDeck.getPlacement().setX(320);
        drawDeck.getPlacement().setY(150);
        drawDeck.setRedealAllowed(true);
        table.getPlacements().add(drawDeck.getPlacement());

        playPile.setId(PLAY);
        playPile.setX(380);
        playPile.setY(150);
        table.getPlacements().add(playPile);
    }

    @Override public Tableau select(String id, int gridX, int gridY) {
        if(DRAW.equals(id)) {
            justHit = false;
            if (!drawDeck.isEmpty()) {
                count++;
                if(count > 13) count = 1;
                drawDeck.dealOnto(playPile, true);
            } else if (gotHit) {
                lastChance = false;
                gotHit = false;
                drawDeck.setRedealAllowed(true);
                drawDeck.redealFrom(playPile.getDeck(), false);
            } else if (lastChance) {
                lose();
            } else {
                lastChance = true;
                drawDeck.setRedealAllowed(false);
                drawDeck.redealFrom(playPile.getDeck(), false);
            }
        }
        if(PLAY.equals(id)) {
            if(justHit) throw new IllegalStateException("draw a new card first");
            justHit = true;
            if(count == playPile.getDeck().get(0).rank()) {
                gotHit = true;
                drawDeck.setRedealAllowed(true);
                playPile.getDeck().remove(0);
                if(playPile.isEmpty() && drawDeck.isEmpty()) win();
            } else {
                strike++;
                if (strike < 3) {
                    throw new IllegalStateException("Strike " + strike + ". Count is " + count);
                }
                else lose();
            }
        }
        return table;
    }
}
