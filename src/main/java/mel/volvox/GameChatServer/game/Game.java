package mel.volvox.GameChatServer.game;

import mel.volvox.GameChatServer.comm.Board;
import mel.volvox.GameChatServer.model.seating.Move;
import mel.volvox.GameChatServer.repository.MoveRepo;

import java.util.List;

public interface Game {
    List<String> getSeatOptions();
    void initMove(Move move);
    Move processMove(Move move, MoveRepo repo);
    void abandonSeat(String user);
    String changeSeats(String user, String newSeat);
    String requestSeat(String seat, String user);
    int lastMoveNumber();
    int nextMoveNumber();
    int lastChatNumber();
    int nextChatNumber();
    void setChatNumber(int serialNumber);
}
