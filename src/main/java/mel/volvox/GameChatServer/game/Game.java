package mel.volvox.GameChatServer.game;

import java.util.List;

public interface Game {
    List<String> getSeatOptions();
    void abandonSeat(String user);
    String changeSeats(String user, String newSeat);
    String requestSeat(String seat, String user);
    int lastMoveNumber();
    int nextMoveNumber();
    int lastChatNumber();
    int nextChatNumber();
    void setChatNumber(int serialNumber);
}
