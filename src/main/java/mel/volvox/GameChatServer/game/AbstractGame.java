package mel.volvox.GameChatServer.game;

import mel.volvox.GameChatServer.model.seating.Move;

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
    @Override public void abandonSeat(String user) { }
    @Override public String changeSeats(String seat, String user) { return null; }
}
