package mel.volvox.GameChatServer.xx1856;

import lombok.Getter;

import mel.volvox.GameChatServer.model.xx1856.Move;
import mel.volvox.GameChatServer.model.xx1856.MoveID;
import mel.volvox.GameChatServer.repository.xx1856Repo;
import mel.volvox.undo.UndoManager;
import mel.volvox.undo.UndoableGame;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game implements UndoableGame<Move> {
    @Override public void storeMove(Move move) { repo.save(move); }
    @Override public void deleteMove(Move move) { repo.delete(move); }
    @Override public String getActionType(Move move) { return move.getAction(); }
    @Override public boolean isMovePrimary(Move move) { return move.isTop(); }

    public enum Era { GATHER, AUCTION, INITIAL, STOCK, OP, DONE }

    @Getter private final Board board = new Board();
    @Getter private final Bank bank = new Bank(board);
    private final xx1856Repo repo;
    private final Map<String, Game> name2game = new HashMap<>();
    @Getter private final UndoManager<Move, Game, Action> undoMgr = new UndoManager<>(this);

    public Game(String name, xx1856Repo repo) {
        this.repo = repo;
        board.name = name;
        registerActions();
    }

    public void load(List<Move> moves) { undoMgr.load(moves); }
    public Board undo() { undoMgr.undo(); return board; }
    public Board redo() { undoMgr.redo(); return board; }
    public Board redoAll() { undoMgr.redoAll(); return board; }

    public Board addMoveUsingPlayer(String opcode, String player) {
        Move move = new Move(nextID(), opcode, player, "", 0, "", true);
        undoMgr.newTopMove(move);
        return board;
    }

    public Board addMoveUsingPlayerDetail(String opcode, String player, String detail) {
        Move move = new Move(nextID(), opcode, player, "", 0, detail, true);
        undoMgr.newTopMove(move);
        return board;
    }

    public Board addMoveUsingAmount(String opcode, int amount) {
        Move move = new Move(nextID(), opcode, "", "", amount, "", true);
        undoMgr.newTopMove(move);
        return board;
    }

    public Board addMove(String opcode, String player, String corp, int amount, String detail) {
        Move move = new Move(nextID(), opcode, player, corp, amount, detail, true);
        undoMgr.newTopMove(move);
        return board;
    }

    public void addSub(String opcode, String player, String corp, int amount, String detail) {
        Move move = new Move(nextID(), opcode, player, corp, amount, detail, false);
        undoMgr.newSubMove(move);
    }

    public void addSubUsingDetail(String opcode, String detail) {
        Move move = new Move(nextID(), opcode, "", "", 0, detail, false);
        undoMgr.newSubMove(move);
    }

    public void addSubUsingPlayerDetail(String opcode, String player, String detail) {
        Move move = new Move(nextID(), opcode, player, "", 0, detail, false);
        undoMgr.newSubMove(move);
    }

    public void addSubUsingCorpDetail(String opcode, String corp, String detail) {
        Move move = new Move(nextID(), opcode, "", corp, 0, detail, false);
        undoMgr.newSubMove(move);
    }

    public void addSubUsingNothing(String opcode) {
        Move move = new Move(nextID(), opcode, "", "", 0, "", false);
        undoMgr.newSubMove(move);
    }

    private MoveID nextID() { return new MoveID(board.name, undoMgr.calculateSerialNumber()); }
    private void registerActions() {
        Action.registerAll(undoMgr);
        GatherActions.registerAll(undoMgr);
        AuctionActions.registerAll(undoMgr);
    }
}
