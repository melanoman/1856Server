package mel.volvox.GameChatServer.game;

import mel.volvox.GameChatServer.model.seating.Move;

import java.util.List;

public abstract class AbstractGame implements Game {
    public static List<String> seatTypes = List.of("Player", "Observer");

    // default section
    @Override public List<String> getSeatOptions() { return seatTypes; }
    @Override public String requestSeat(String seat) { throw new IllegalStateException("Denied"); }
    @Override public int lastMove() { return 0; }
}
