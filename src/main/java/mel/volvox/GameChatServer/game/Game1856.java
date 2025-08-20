package mel.volvox.GameChatServer.game;

import lombok.Getter;
import lombok.Setter;
import mel.volvox.GameChatServer.comm.Board1856;
import mel.volvox.GameChatServer.model.train.TrainMove;
import mel.volvox.GameChatServer.model.train.TrainMoveID;
import mel.volvox.GameChatServer.model.train.TrainWallet;
import mel.volvox.GameChatServer.repository.TrainRepo;
import mel.volvox.GameChatServer.service.DiceService;

import java.util.ArrayList;
import java.util.List;

public class Game1856 extends AbstractGame {
    @Setter TrainRepo repo; // set by controller
    @Setter String name; // set by controller
    @Getter private final Board1856 board = new Board1856();
    public static final int[] START_CASH = { 0, 0, 0, 500, 375, 300, 250 };

    // action constants
    public static final String ADD_PLAYER = "addPlayer";
    public static final String RENAME_PLAYER = "renamePlayer";
    public static final String START_GAME = "startGame";

    // sub-action constants
    public static final String SHUFFLE = "shuffle";
    public static final String NO_SHUFFLE = "noShuffle";

    private int nextMove = 1;
    private List<TrainMove> history;

    public enum Era { GATHER, AUCTION, STOCK, OP, DONE }

    public void loadMoves(List<TrainMove> moves) {
        history = moves;
        for(TrainMove move: moves) {
            loadMove(move);
        }
    }

    public void loadMove(TrainMove move) {
        doMove(move);
        nextMove++;
    }

    private void doStart(boolean shuffle) {
        if (shuffle) doShuffle();
        board.setPhase(Era.AUCTION.name());
        for (int i=0; i<board.getPlayers().size(); i++) {
            TrainWallet wallet= new TrainWallet();
            wallet.setCash(START_CASH[i]);
            board.getWallets().add(wallet);
        }
    }

    private void doMove(TrainMove move) {
        switch (move.getAction()) {
            case ADD_PLAYER:
                board.getPlayers().add(move.getPlayer());
                break;
            case RENAME_PLAYER:
                board.getPlayers().set(move.getAmount(), move.getPlayer());
                break;
            case START_GAME:
                doStart(SHUFFLE.equals(move.getPlayer()));
                break;
            default:
                throw new IllegalStateException("unknown move action: "+move.getAction());
        }
    }

    private void makeMove(String action, String player, String corp, int amount) {
        TrainMoveID id = new TrainMoveID(name, nextMove);
        TrainMove out = new TrainMove(id, action, player, corp, amount);
        repo.save(out);
        doMove(out);
        history.add(out);
        nextMove++;
    }

    public boolean addPlayer(String name) {
        if (!Era.GATHER.name().equals(board.getPhase()) ||
                board.getPlayers().size() >= 6 ||
                board.getPlayers().contains(name)) {
            return false;
        }
        makeMove(ADD_PLAYER, name, "", 0);
        return true;
    }

    public boolean renamePlayer(int seat, String name) {
        if(seat < 0 || seat >= board.getPlayers().size() || board.getPlayers().contains(name)) return false;
        makeMove(RENAME_PLAYER, name, "", seat);
        return true;
    }

    public void doShuffle() {
        List<String> newOrder = new ArrayList<>();
        for(int i=board.getPlayers().size(); i>0; i--) {
            String name = board.getPlayers().get(DiceService.Roll(i)-1);
            newOrder.add(name);
            board.getPlayers().remove(name);
        }
        board.setPlayers(newOrder);
    }

    synchronized public Board1856 startGame(boolean shuffle) {
        if (!Era.GATHER.name().equals(board.getPhase()) ||
                board.getPlayers().size() < 3 ||
                board.getPlayers().size() > 6) {
            throw new IllegalStateException("Game is not startable");
        }
        makeMove("start", shuffle ? SHUFFLE : NO_SHUFFLE, "", 0);
        return board;
    }
}
