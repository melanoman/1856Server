package mel.volvox.GameChatServer.game;

import mel.volvox.GameChatServer.comm.Board;
import mel.volvox.GameChatServer.model.seating.Move;

public class Game1856 extends AbstractGame {
    enum Era { GATHER, AUCTION, STOCK, OP, DONE };
    Era era = Era.GATHER;
    @Override public void initMove(Move move) { }
    @Override public Move processMove(Move move) { return null; }
    @Override public void abandonSeat(String user) { }
    @Override public String changeSeats(String user, String newSeat) { return null; }
    @Override public Board getBoard() { return null; }
}
