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
        undoMgr.registerActionType(START_OP_TURN, new StartOpTurn());
        undoMgr.registerActionType(TAKE_LOAN, new TakeLoanAction());
        undoMgr.registerActionType(LAY_TOKEN, new LayTokenAction());
        undoMgr.registerActionType(DRILL_TILE, new DrillTileAction());
        undoMgr.registerActionType(RESET_LOAN, new ResetTokenAction());
        undoMgr.registerActionType(RESET_TOKEN, new ResetLoanAction());
        undoMgr.registerActionType(FLOAT, new FloatAction());
    }

    //detail == former phase amount = 0 for reset, 1 for continue
    static class StartOpRound extends Action {
        @Override public void checkAllowed(Move move, Game game) { }

        @Override public void init(Move move, Game game) {
            game.addSub(CHANGE_ACTIVITY, OP_PRE, "", 0, game.getBoard().activity);
            game.addSub(CHANGE_CORP, "", game.getBoard().corps.get(0).name, 0, game.getBoard().currentCorp);
            game.addSub(START_OP_TURN, "", game.getBoard().corps.get(0).name, 0, "");
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
            if(game.getBoard().thisOR <= game.getBoard().maxOR) {
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
            game.addSub(CHANGE_ACTIVITY, OP_PRE, "", 0, game.getBoard().activity);
            Corp c = findCorp(move.getCorp(), game);
            if(c.tokensUsed == 0) {
                //TODO if (CHECK FLOAT)
                game.addSub(FLOAT, "", move.getCorp(), 0, "");
                // else game.addSub(FAIL_FLOAT, "", move.getCorp(), 0, "");
            }
            game.addSub(RESET_TOKEN, "", move.getCorp(), c.tokenLaid?1:0, "");
            game.addSub(RESET_LOAN, "", move.getCorp(), c.loanTaken?1:0, "");
        }

        @Override public void doAction(Move move, Game game) {
            game.getBoard().currentCorp = move.getCorp();
            Corp c = findCorp(move.getCorp(), game);
        }

        @Override public void undoAction(Move move, Game game) {
            game.getBoard().currentCorp = move.getDetail();
            Corp c = findCorp(move.getCorp(), game);
        }
    }

    static class TakeLoanAction extends Action {

        @Override public void checkAllowed(Move move, Game game) {
            assertPhase(game, Game.Era.OP, "TakeLoan");
            assertCorpTurn(game, move.getCorp(), "TakeLoan");
            Corp c = findCorp(move.getCorp(), game);
            if(c.loanTaken) throw new IllegalStateException("Only one loan per turn");
            // TODO compare holdings to number of loans out
        }

        @Override public void init(Move move, Game game) { }

        @Override public void doAction(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            game.getBank().payCorp(c.name, 100);
            c.loanTaken = true;
            c.loans++;
        }

        @Override public void undoAction(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            game.getBank().debitCorp(c.name, 100);
            c.loanTaken = false;
            c.loans--;
        }
    }

    static class LayTokenAction extends Action {

        @Override public void checkAllowed(Move move, Game game) {
            assertPhase(game, Game.Era.OP, "LayToken");
            assertCorpTurn(game, move.getCorp(), "LayToken");
            assertActivity(game, OP_PRE, "LayToken");
            Corp c = findCorp(move.getCorp(), game);
            if(c.tokensUsed >= c.tokensMax) {
                throw new IllegalStateException("No tokens available");
            }
            if(c.tokenLaid) {
                throw new IllegalStateException("One paid token per turn");
            }
        }

        @Override public void init(Move move, Game game) { }

        @Override public void doAction(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            int price = (c.tokensUsed < 2) ? 40 : 100;
            game.getBank().debitCorp(c.name, price);
            c.tokensUsed++;
            c.tokenLaid = true;
        }

        @Override public void undoAction(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            c.tokensUsed--;
            int price = (c.tokensUsed < 2) ? 40 : 100;
            game.getBank().payCorp(c.name, price);
            c.tokenLaid = false;
        }
    }

    static class DrillTileAction extends Action {

        @Override public void checkAllowed(Move move, Game game) {
            assertPhase(game, Game.Era.OP, "DrillTile");
            assertCorpTurn(game, move.getCorp(), "DrillTile");
            assertActivity(game, OP_PRE, "DrillTile");
            assertCorpFunds(game, move.getCorp(), 40, "DrillTile");
            //TODO protect against 2x tile charges same turn
        }

        @Override public void init(Move move, Game game) { }

        @Override public void doAction(Move move, Game game) {
            game.getBank().debitCorp(move.getCorp(), 40);
        }

        @Override public void undoAction(Move move, Game game) {
            game.getBank().payCorp(move.getCorp(), 40);
        }
    }

    static class ResetLoanAction extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) { }

        @Override public void doAction(Move move, Game game) {
            findCorp(move.getCorp(), game).loanTaken = false;
        }

        @Override public void undoAction(Move move, Game game) {
            findCorp(move.getCorp(), game).loanTaken = move.getAmount() == 1;
        }
    }

    static class ResetTokenAction extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) { }

        @Override public void doAction(Move move, Game game) {
            findCorp(move.getCorp(), game).tokenLaid = false;
        }

        @Override public void undoAction(Move move, Game game) {
            findCorp(move.getCorp(), game).tokenLaid = move.getAmount() == 1;
        }
    }

    static class FloatAction extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) { }

        @Override public void doAction(Move move, Game game) {
            findCorp(move.getCorp(), game).tokensUsed = 1;
        }

        @Override public void undoAction(Move move, Game game) {
            findCorp(move.getCorp(), game).tokensUsed = 0;
        }
    }

    static class FailFloatAction extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) { }

        @Override public void doAction(Move move, Game game) {
            findCorp(move.getCorp(), game).hasOperated = true;
        }

        @Override public void undoAction(Move move, Game game) {
            findCorp(move.getCorp(), game).hasOperated = false;
        }
    }
}
