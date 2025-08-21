package mel.volvox.GameChatServer.game;

import lombok.Getter;
import lombok.Setter;
import mel.volvox.GameChatServer.comm.Board1856;
import mel.volvox.GameChatServer.model.train.TrainMove;
import mel.volvox.GameChatServer.model.train.TrainMoveID;
import mel.volvox.GameChatServer.model.train.TrainWallet;
import mel.volvox.GameChatServer.repository.TrainRepo;
import mel.volvox.GameChatServer.service.DiceService;

import javax.print.DocFlavor;
import java.util.ArrayList;
import java.util.List;

public class Game1856 extends AbstractGame {
    @Setter TrainRepo repo; // set by controller
    @Getter private final Board1856 board = new Board1856();
    public static final int[] START_CASH = { 0, 0, 0, 500, 375, 300, 250 };

    // action constants
    public static final String ADD_PLAYER = "addPlayer";
    public static final String RENAME_PLAYER = "renamePlayer";
    public static final String START_GAME = "startGame";

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
        board.setMoveNumber(board.getMoveNumber()+1);
    }

    final private static String SHUFFLE_STOCK = "123456";

    static String makeShuffle(boolean shuffle, int size) {
        StringBuffer buf = new StringBuffer(SHUFFLE_STOCK.substring(0, size));
        if(shuffle) {
            for (int i = size; i > 0; i--) {
                int j = DiceService.Roll(i);
                char tmp = buf.charAt(i - 1);
                buf.setCharAt(i - 1, buf.charAt(j - 1));
                buf.setCharAt(j - 1, tmp);
            }
        }
        return buf.toString();
    }

    private void undoStart(String order) {
        List<String> newPlayers = new ArrayList<>();
        for(char c='1'; c-'1'<order.length(); c++) {
            newPlayers.add(board.getPlayers().get(order.indexOf(c)));
        }
        board.setPlayers(newPlayers);
        board.setPhase(Era.GATHER.name());
    }

    private void doStart(String order) {
        List<String> newPlayers = new ArrayList<>();
        for(int i=0; i<order.length(); i++) {
            newPlayers.add(board.getPlayers().get(order.charAt(i)-'1'));
        }
        board.setPlayers(newPlayers);
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
                doStart(move.getCorp());
                break;
            default:
                throw new IllegalStateException("unknown move action: "+move.getAction());
        }
    }

    public boolean undoMove(TrainMove move) {
        switch (move.getAction()) {
            case ADD_PLAYER:
                board.getPlayers().remove(board.getPlayers().size()-1);
                return true;
            case RENAME_PLAYER:
                board.getPlayers().set(move.getAmount(), move.getCorp());
                return true;
            case START_GAME:
                undoStart(move.getCorp());
                return true;
            default:
                return false;
        }
    }

    private void lockUndo() {
        while(board.getUndoCount() > 0) {
            TrainMove out = history.get(board.getMoveNumber()-1);
            repo.delete(out);
            history.remove(out);
            board.setUndoCount(board.getUndoCount()-1);
            board.setMoveNumber(board.getMoveNumber()-1);
        }
    }

    private void makeMove(String action, String player, String corp, int amount) {
        if(board.getUndoCount() > 0) {
            lockUndo();
        }
        TrainMoveID id = new TrainMoveID(board.getName(), board.getMoveNumber()+1);
        TrainMove out = new TrainMove(id, action, player, corp, amount);
        repo.save(out);
        doMove(out);
        history.add(out);
        board.setMoveNumber(id.getSerialNumber());
    }

    public Board1856 undo() {
        if (board.getUndoCount() == board.getMoveNumber()) return board;
        if (undoMove(history.get(board.getMoveNumber()-board.getUndoCount()-1))) {
            board.setUndoCount(board.getUndoCount()+1);
        }
        return board;
    }

    public Board1856 redo() {
        if (board.getUndoCount() < 1) return board;
        doMove(history.get(board.getMoveNumber()-board.getUndoCount()));
        board.setUndoCount(board.getUndoCount()-1);
        return board;
    }

    public Board1856 redoAll() {
        while (board.getUndoCount() > 0) {
            redo();
        }
        return board;
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

    public Board1856 renamePlayer(String oldName, String newName) {
        int seat = board.getPlayers().indexOf(oldName);
        if(seat < 0 || board.getPlayers().contains(newName)) return board;
        makeMove(RENAME_PLAYER, newName, oldName, seat);
        return board;
    }

    synchronized public Board1856 startGame(boolean shuffle) {
        if (!Era.GATHER.name().equals(board.getPhase()) ||
                board.getPlayers().size() < 3 ||
                board.getPlayers().size() > 6) {
            throw new IllegalStateException("Game is not startable");
        }
        makeMove(START_GAME, "", makeShuffle(shuffle, board.getPlayers().size()), 0);
        return board;
    }
}
