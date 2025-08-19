package mel.volvox.GameChatServer.seating;

import mel.volvox.GameChatServer.game.Game;
import mel.volvox.GameChatServer.game.GameRPS;
import mel.volvox.GameChatServer.model.seating.*;
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
    public String changeSeats(String newSeat, String user) { return game.changeSeats(newSeat, user); }
    public MessageID nextMessageId() { return new MessageID(name, game.nextChatNumber()); }
    public void setChatNumber(int serialNumber) { game.setChatNumber(serialNumber); }
    /**
     * called when loading seating after quit and restart of server
     */
    public void initSeats(List<Seat> seats) {
        for(Seat seat:seats) account2chair.put(seat.getAccount(), seat);
    }

    public synchronized int addMessage(MessageRepo messageRepo, String text, String author) {
        MessageID id = nextMessageId();
        messageRepo.save(new Message(id, text, 0, author)); // TODO remove move references
        return id.getSerialNumber();
    }

    public GameRPS getRPSGame() { return (GameRPS)game; }
}
