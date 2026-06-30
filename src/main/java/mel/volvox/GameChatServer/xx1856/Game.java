package mel.volvox.GameChatServer.xx1856;

import lombok.Getter;

import mel.volvox.GameChatServer.model.xx1856.xxMove;
import mel.volvox.GameChatServer.model.xx1856.xxMoveID;
import mel.volvox.GameChatServer.repository.xx1856Repo;
import mel.volvox.undo.UndoManager;
import mel.volvox.undo.UndoableGame;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game implements UndoableGame<xxMove> {
    @Override public void storeMove(xxMove move) { repo.save(move); }
    @Override public void deleteMove(xxMove move) { repo.delete(move); }
    @Override public String getActionType(xxMove move) { return move.getAction(); }
    @Override public boolean isMovePrimary(xxMove move) { return move.isTop(); }

    public enum Era { GATHER, AUCTION, INITIAL, STOCK, OP, DONE }

    @Getter private final Board board = new Board();
    @Getter private final Bank bank = new Bank(board);
    private final xx1856Repo repo;
    private final Map<String, Game> name2game = new HashMap<>();
    @Getter private final UndoManager<xxMove, Game, Action> undoMgr = new UndoManager<>(this);

    public Game(String name, xx1856Repo repo) {
        this.repo = repo;
        board.name = name;
        registerActions();
    }

    public void load(List<xxMove> moves) { undoMgr.load(moves); }
    public Board undo() { undoMgr.undo(); return board; }
    public Board redo() { undoMgr.redo(); return board; }
    public Board redoAll() { undoMgr.redoAll(); return board; }

    void assertPhase(Era intended, String caller) {
        if(!intended.name().equals(board.phase)) {
            throw new IllegalStateException("Edited during wrong phase -- " + caller);
        }
    }

    public Board addMoveUsingPlayer(String opcode, String player) {
        xxMove move = new xxMove(nextID(), opcode, player, "", 0, "", true);
        undoMgr.newTopMove(move);
        return board;
    }

    public Board addMoveUsingPlayerDetail(String opcode, String player, String detail) {
        xxMove move = new xxMove(nextID(), opcode, player, "", 0, detail, true);
        undoMgr.newTopMove(move);
        return board;
    }

    private xxMoveID nextID() { return new xxMoveID(board.name, undoMgr.calculateSerialNumber()); }
    private void registerActions() {
        PlayerActions.registerAll(undoMgr);
    }
}
