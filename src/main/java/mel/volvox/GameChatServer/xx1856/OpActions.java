package mel.volvox.GameChatServer.xx1856;

import mel.volvox.GameChatServer.model.xx1856.Move;
import mel.volvox.undo.UndoManager;

import static mel.volvox.GameChatServer.xx1856.Opcodes.*;

public class OpActions {
    public static final String OP_PRE = "opPre";
    public static final String OP_POST = "opPost";

    public static void registerAll(UndoManager<Move, Game, Action> undoMgr) {
        undoMgr.registerActionType(START_OP_ROUND, new StartOpRound());
        undoMgr.registerActionType(END_OP_ROUND, new EndOpRound());
    }

    //detail == former phase amount = 0 for reset, 1 for continue
    static class StartOpRound extends Action {
        @Override public void checkAllowed(Move move, Game game) { }

        @Override public void init(Move move, Game game) {
            game.addSub(CHANGE_ACTIVITY, "OP_PRE", "", 0, game.getBoard().activity);
            game.addSub(CHANGE_CORP, "", game.getBoard().corps.get(0).name, 0, game.getBoard().currentCorp);
        }

        @Override public void doAction(Move move, Game game) {
            payPrivates(game);
            for(Corp c:game.getBoard().corps) c.hasOperated=false;
        }

        @Override public void undoAction(Move move, Game game) {
            refundPrivates(game);
            for(Corp c:game.getBoard().corps) c.hasOperated=true;
        }
    }

    static class EndOpRound extends Action {
        @Override public void checkAllowed(Move move, Game game) { }

        @Override public void init(Move move, Game game) {
            if(game.getBoard().thisOR > game.getBoard().maxOR) {
                game.addSub(START_OP_ROUND, "", "", 0, "");
            } else {
                game.addSub(START_STOCK_ROUND, game.getBoard().currentPlayer, "", 0, game.getBoard().activity);
            }
        }

        @Override public void doAction(Move move, Game game) {
            game.getBoard().thisOR++;
        }

        @Override public void undoAction(Move move, Game game) {
            game.getBoard().thisOR--;
        }
    }

    // detail = former currentCorp
    static class StartOpTurn extends Action {
        @Override public void checkAllowed(Move move, Game game) { }

        @Override public void init(Move move, Game game) {
            game.addSub(CHANGE_ACTIVITY, "OP_PRE", "", 0, game.getBoard().activity);

        }

        @Override public void doAction(Move move, Game game) {
            game.getBoard().currentCorp = move.getCorp();
        }

        @Override public void undoAction(Move move, Game game) {
            game.getBoard().currentCorp = move.getDetail();
        }
    }
}
