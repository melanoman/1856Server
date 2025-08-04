package mel.volvox.GameChatServer.seating;

import mel.volvox.GameChatServer.game.Game;
import mel.volvox.GameChatServer.model.seating.MessageID;
import mel.volvox.GameChatServer.model.seating.Move;
import mel.volvox.GameChatServer.model.seating.Seat;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableView {
    final Map<String, Seat> account2chair = new HashMap<>();

    private String name;
    private Game game;
    private int moveCount = 0;

    public TableView(String name, Class<? extends Game> gc)
            throws NoSuchMethodException, InvocationTargetException,
                   InstantiationException, IllegalAccessException {
        this.name = name;
        this.game = gc.getConstructor().newInstance();
    }

    /**
     * called when loading seating after quit and restart of server
     */
    public void initSeats(List<Seat> seats) {
        for(Seat seat:seats) account2chair.put(seat.getAccount(), seat);
    }

    //Input list is all the moves sorted by serial number
    public void initMoves(List<Move> moves) {
        for(Move move: moves) {
            game.initMove(move);
        }
    }

    private int calculateNextMessageNumber() {
        //TODO really calculate
        return 23;
    }

    public MessageID nextMessageId() {
        return new MessageID(name, calculateNextMessageNumber());
    }

    // passthru section
    public List<String> getSeatOptions() { return game.getSeatOptions(); }
    public String requestSeat(String seat) { return game.requestSeat(seat); }
}
