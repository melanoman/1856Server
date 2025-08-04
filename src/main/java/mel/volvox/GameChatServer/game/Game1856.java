package mel.volvox.GameChatServer.game;

import mel.volvox.GameChatServer.model.seating.Move;

public class Game1856 extends AbstractGame {
    enum Era { GATHER, AUCTION, STOCK, OP, DONE };
    Era era = Era.GATHER;
    @Override public void initMove(Move move) { }
}
