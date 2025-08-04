package mel.volvox.GameChatServer.game;

import mel.volvox.GameChatServer.model.seating.Move;

import java.util.List;

public interface Game {
    List<String> getSeatOptions();
    void initMove(Move move);
    String requestSeat(String seat);
    int lastMove();
}
