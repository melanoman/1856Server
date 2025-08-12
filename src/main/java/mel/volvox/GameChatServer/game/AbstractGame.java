package mel.volvox.GameChatServer.game;

import mel.volvox.GameChatServer.comm.Board;
import mel.volvox.GameChatServer.model.seating.Move;
import mel.volvox.GameChatServer.repository.MoveRepo;

import java.util.List;

public abstract class AbstractGame implements Game {
    public static final String PLAYER = "player";
    public static final String OBSERVER = "observer";
    public static List<String> seatTypes = List.of(PLAYER, OBSERVER);
    protected int currentMoveNumber = 0;
    protected int currentChatNumber = 0;

    // default section
    @Override public List<String> getSeatOptions() { return seatTypes; }
    @Override public String requestSeat(String seat, String user) { throw new IllegalStateException("Denied"); }
    @Override public int lastMoveNumber() { return currentMoveNumber; }
    @Override public int nextMoveNumber() { return 1+currentMoveNumber; }
    @Override public int lastChatNumber() { return currentChatNumber; }
    @Override public int nextChatNumber() { currentChatNumber++; return currentChatNumber; }
    @Override public void setChatNumber(int serialNumber) { currentChatNumber = serialNumber; }
    @Override public Move processMove(Move move, MoveRepo repo) { return null; }
    @Override public void initMove(Move move) { }
    @Override public void abandonSeat(String user) { }
    @Override public String changeSeats(String user, String newSeat) { return null; }
}
