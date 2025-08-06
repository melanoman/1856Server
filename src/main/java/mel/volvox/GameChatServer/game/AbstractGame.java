package mel.volvox.GameChatServer.game;

import mel.volvox.GameChatServer.model.seating.Move;

import java.util.List;

public abstract class AbstractGame implements Game {
    public static List<String> seatTypes = List.of("Player", "Observer");
    protected int currentMoveNumber = 0;
    protected int currentChatNumber = 0;

    // default section
    @Override public List<String> getSeatOptions() { return seatTypes; }
    @Override public String requestSeat(String seat) { throw new IllegalStateException("Denied"); }
    @Override public int lastMoveNumber() { return currentMoveNumber; }
    @Override public int lastChatNumber() { return currentChatNumber; }
    @Override public int nextChatNumber() { currentChatNumber++; return currentChatNumber; }
    @Override public void setChatNumber(int serialNumber) { currentChatNumber = serialNumber; }
}
