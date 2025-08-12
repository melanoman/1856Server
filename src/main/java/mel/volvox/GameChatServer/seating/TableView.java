package mel.volvox.GameChatServer.seating;

import mel.volvox.GameChatServer.comm.Board;
import mel.volvox.GameChatServer.game.Game;
import mel.volvox.GameChatServer.model.seating.Message;
import mel.volvox.GameChatServer.model.seating.MessageID;
import mel.volvox.GameChatServer.model.seating.Move;
import mel.volvox.GameChatServer.model.seating.Seat;
import mel.volvox.GameChatServer.repository.MessageRepo;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableView {
    final Map<String, Seat> account2chair = new HashMap<>();

    private String name;
    private Game game;

    public TableView(String name, Class<? extends Game> gc)
            throws NoSuchMethodException, InvocationTargetException,
                   InstantiationException, IllegalAccessException {
        this.name = name;
        this.game = gc.getConstructor().newInstance();
    }

    // passthru section
    public List<String> getSeatOptions() { return game.getSeatOptions(); }
    public String requestSeat(String seat, String user) { return game.requestSeat(seat, user); }
    public void abandonSeat(String user) { game.abandonSeat(user); }
    public String changeSeats(String user, String newSeat) { return game.changeSeats(user, newSeat); }
    public int currentMoveNumber() { return game.lastMoveNumber(); }
    public MessageID nextMessageId() { return new MessageID(name, game.nextChatNumber()); }
    public void setChatNumber(int serialNumber) { game.setChatNumber(serialNumber); }

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

    public synchronized int addMessage(MessageRepo messageRepo, String text, String author) {
        int move = currentMoveNumber();
        MessageID id = nextMessageId();
        messageRepo.save(new Message(id, text, move, author));
        return id.getSerialNumber();
    }

    public Board getBoard() {
        return game.getBoard();
    }
}
