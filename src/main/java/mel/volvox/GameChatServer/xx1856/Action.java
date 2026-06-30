package mel.volvox.GameChatServer.xx1856;

import mel.volvox.GameChatServer.model.xx1856.xxMove;
import mel.volvox.undo.UndoableAction;

public abstract class Action implements UndoableAction<xxMove, Game> {
    @Override
    public void exec(xxMove move, Game game) {
        resetBoardCounts(game);
        doAction(move, game);
    }

    @Override
    public void undo(xxMove move, Game game) {
        resetBoardCounts(game);
        undoAction(move, game);
    }

    public abstract void doAction(xxMove move, Game game);
    public abstract void undoAction(xxMove move, Game game);

    private void resetBoardCounts(Game game) {
        game.getBoard().moveNumber = game.getUndoMgr().size();
        game.getBoard().undoCount = game.getUndoMgr().getUndoCount();
    }
}
