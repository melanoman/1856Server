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

    public void load(List<Move> moves) { undoMgr.load(moves); resetWealth(); }
    public Board undo() { undoMgr.undo(); resetWealth(); return board; }
    public Board redo() { undoMgr.redo(); resetWealth(); return board; }
    public Board redoAll() { undoMgr.redoAll(); resetWealth(); return board; }

    public Board addMove(String opcode, String player, String corp, int amount, String detail) {
        Move move = new Move(nextID(), opcode, player, corp, amount, detail, true);
        undoMgr.newTopMove(move);
        resetWealth();
        return board;
    }

    public void addSub(String opcode, String player, String corp, int amount, String detail) {
        Move move = new Move(nextID(), opcode, player, corp, amount, detail, false);
        undoMgr.newSubMove(move);
    }

    private MoveID nextID() { return new MoveID(board.name, undoMgr.calculateSerialNumber()); }
    private void registerActions() {
        Action.registerAll(undoMgr);
        GatherActions.registerAll(undoMgr);
        AuctionActions.registerAll(undoMgr);
        StockActions.registerAll(undoMgr);
        OpActions.registerAll(undoMgr);
    }

    public void resetWealth() {
        Map<String,Integer> corp2price = new HashMap<>();
        for(Corp c: board.corps) {
            corp2price.put(c.name, c.par < 65 ? 0 : c.price.getPrice() - 10*c.loans);
        }
        for(Player p: board.players) {
            int wealth = p.cash;
            for(String priv:p.privs) {
                wealth += Priv.findPriv(priv).price;
            }
            for(Stock s:p.shares) {
                wealth += corp2price.get(s.corpName)*s.amount;
            }
            p.wealth = wealth;
        }
    }

    public int floatLevel() {
        if(board.trains.size() < 3) return 6;
        return board.trains.get(0);
    }

    /**
     * Highest Corp with hasOperated false or null if none found
     */
    public Corp nextCorp() {
        for(Corp c: board.corps) if(!c.hasOperated && c.par > 0) return c;
        return null;
    }
}
