package mel.volvox.GameChatServer.xx1856;

import lombok.Getter;

import mel.volvox.GameChatServer.model.xx1856.xxMove;
import mel.volvox.GameChatServer.model.xx1856.xxMoveID;
import mel.volvox.GameChatServer.repository.xx1856Repo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static mel.volvox.GameChatServer.xx1856.xxOpcodes.*;

public class xx1856Game {
    public enum Era { GATHER, AUCTION, INITIAL, STOCK, OP, DONE }

    @Getter private final xx1856Board board = new xx1856Board();
    @Getter private final xxBank bank = new xxBank(board);
    private final xx1856Repo repo;
    private List<xxMove> moves = new ArrayList<>();
    private final Map<String, xx1856Game> name2game = new HashMap<>();

    public xx1856Game(xx1856Repo repo) { this.repo = repo; }

    public void undo() {
        if (board.undoCount < moves.size()) {
            board.undoCount++;
            undoMove(moves.get(moves.size() - board.undoCount));
        }
    }

    public void redo() {
        if (board.undoCount > 0) {
            board.undoCount--;
            doMove(moves.get(moves.size() - board.undoCount), false);
        }
    }

    public void load(String gameName) {
        moves = repo.findByIdGameNameOrderByIdSerialNumberAsc(gameName);
        for(xxMove move:moves) doMove(move, false);
    }

    private void assertPhase(Era intended, String caller) {
        if(!intended.name().equals(board.phase)) {
            throw new IllegalStateException("Edited during wrong phase -- " + caller);
        }
    }

    private void makeMove(String opcode, String player, String corp,
                          int amount, String detail, boolean isTop) {
        xxMoveID id = new xxMoveID(board.getName(), moves.size() + 1);
        xxMove move = new xxMove(id, board.getName(), player, corp, amount, detail, isTop);
        repo.save(move);
        moves.add(move);
        doMove(move, true);
    }

    private void makePrimaryMove(String opcode, String player, String corp, int amount, String detail) {
        // delete all the undone history so the new move will appear in the correct place
        while(board.undoCount > 0) {
            int last = moves.size() - 1;
            xxMove out = moves.get(last);
            repo.delete(out);
            moves.remove(last);
            board.undoCount--;
        }
        makeMove(opcode, player, corp, amount, detail, true);
    }

    private void makeSecondaryMove(String opcode, String player, String corp, int amount, String detail) {
        makeMove(opcode, player, corp, amount, detail, false);
    }

    private void doMove(xxMove move, boolean isNew) {
        switch(move.getAction()) {
            case ADD_PLAYER -> doAddPlayer(move);
            case RENAME_PLAYER -> doRenamePlayer(move);
        }
    }

    private void undoMove(xxMove move) {
        switch(move.getAction()) {
            case ADD_PLAYER -> undoAddPlayer(move);
            case RENAME_PLAYER -> undoRenamePlayer(move);
        }
    }

    /**
     * ADD_PLAYER player = playerName
     */
    public void tryAddPlayer(String name) {
        assertPhase(Era.GATHER, "Add Player");
        if (board.getPlayers().size() == 6) throw new IllegalStateException("Game is full");
        if (isPlayer(name)) throw new IllegalStateException("Duplicate player name");
        makePrimaryMove(ADD_PLAYER, name, "", 0, "");
    }

    private void doAddPlayer(xxMove move) {
        xxPlayer player = new xxPlayer();
        player.name = move.getPlayer();
        board.getPlayers().add(player);
    }

    private void undoAddPlayer(xxMove move) {
        board.getPlayers().removeIf(x -> move.getPlayer().equals(x.name));
    }

    /**
     * RENAME_PLAYER player = oldName // detail = newName
     */
    public void tryRenamePlayer(xxMove move) {
        assertPhase(Era.GATHER, "Rename Player");
        if (!isPlayer(move.getPlayer())) throw new IllegalStateException("Renamable not found");
        if (isPlayer(move.getDetail())) throw new IllegalStateException("Duplicate player name");
        makePrimaryMove(RENAME_PLAYER, move.getPlayer(), "", 0, move.getDetail());
    }

    private void doRenamePlayer(xxMove move) {
        findPlayer(move.getPlayer()).name = move.getDetail();
    }

    private void undoRenamePlayer(xxMove move) {
        findPlayer(move.getDetail()).name = move.getPlayer();
    }

    // player utilities
    private boolean isPlayer(String name) {
        for(xxPlayer p:board.getPlayers()) {
            if(p.name.equals(name)) return true;
        }
        return false;
    }

    private xxPlayer findPlayer(String name) {
        for(xxPlayer p: board.getPlayers()) {
            if(p.name.equals(name)) return p;
        }
        throw new IllegalStateException("Player not found");
    }
}
