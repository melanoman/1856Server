package mel.volvox.undo;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UndoManager<
        MOVE,
        GAME extends UndoableGame<MOVE>,
        ACTION extends UndoableAction<MOVE, GAME>
> {
    final List<MOVE> moves;
    final GAME game;
    final HashMap<String, ACTION> name2action = new HashMap<>();

    @Getter
    int undoCount = 0;

    public UndoManager(GAME game) {
        this.game = game;
        moves = new ArrayList<>();
    }

    public UndoManager(GAME game, List<MOVE> moves) {
        this.game = game;
        this.moves = moves;
        for(MOVE move: moves) findAction(move).exec(move,game);
    }

    public void registerActionType(String actionName, ACTION action) {
        if(null != name2action.put(actionName, action)) {
            throw new IllegalStateException("Duplicate Action Registered");
        }
    }

    public void load(List<MOVE> moves) {
        for(MOVE move:moves) {
            ACTION action = findAction(move);
            moves.add(move);
            action.exec(move, game);
        }
    }

    public void newTopMove(MOVE move) {
        ACTION action = findAction(move);
        action.checkAllowed(move, game);
        if (undoCount > 0) {
            moves.subList(moves.size()-undoCount, moves.size()).clear();
            undoCount = 0;
        }
        game.storeMove(move);
        moves.add(move);
        action.init(move, game);
        action.exec(move, game);
    }

    public void newSubMove(MOVE move) {
        ACTION action = findAction(move);
        game.storeMove(move);
        moves.add(move);
        action.exec(move, game);
    }

    public ACTION findAction(MOVE move) {
        String type = game.getActionType(move);
        ACTION action = name2action.get(type);
        if (action == null) throw new IllegalStateException("Unknown Action Type " + type);
        return action;
    }

    public void undo() {
        if (undoCount == moves.size()) return;
        undoCount++;
        MOVE move = moves.get(moves.size() - undoCount);
        findAction(move).undo(move, game);
        if(!game.isMovePrimary(move)) undo();
    }

    public void redo() {
        if (undoCount < 1) return;
        MOVE move = moves.get(moves.size() - undoCount);
        findAction(move).exec(move, game);
        undoCount--;
        redoSubs();
    }

    public void redoSubs() {
        if (undoCount < 1) return;
        MOVE move = moves.get(moves.size() - undoCount);
        if (game.isMovePrimary(move)) return;
        undoCount--;
        findAction(move).exec(move,game);
        redoSubs();
    }

    public void redoAll() {
        while (undoCount > 0) {
            undoCount--;
            MOVE move = moves.get(moves.size() - undoCount);
            findAction(move).exec(move, game);
        }
    }
}

