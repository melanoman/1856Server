package mel.volvox.GameChatServer.xx1856;

import mel.volvox.GameChatServer.model.xx1856.xxMove;
import mel.volvox.undo.UndoableAction;

public abstract class xxAction implements UndoableAction<xxMove, xx1856Game> {
    @Override
    public void exec(xxMove move, xx1856Game game) {
        resetBoardCounts(game);
        doAction(move, game);
    }

    @Override
    public void undo(xxMove move, xx1856Game game) {
        resetBoardCounts(game);
        undoAction(move, game);
    }

    public abstract void doAction(xxMove move, xx1856Game game);
    public abstract void undoAction(xxMove move, xx1856Game game);

    private void resetBoardCounts(xx1856Game game) {
        game.getBoard().moveNumber = game.getUndoMgr().size();
        game.getBoard().undoCount = game.getUndoMgr().getUndoCount();
    }
}
