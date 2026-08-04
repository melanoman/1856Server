package mel.volvox.GameChatServer.xx1856;

import mel.volvox.GameChatServer.model.xx1856.Move;
import mel.volvox.undo.UndoManager;

import static mel.volvox.GameChatServer.xx1856.OpActions.OP_POST;
import static mel.volvox.GameChatServer.xx1856.Opcodes.*;

public class BankActions {
    public static String INTEREST = "interest";
    public static String TRAIN = "train";
    public static String FORCED_SALE_ACTIVITY = "forceSale";
    public static String CALL_LOAN_ACTIVITY = "callLoan";

    public static void registerAll(UndoManager<Move, Game, Action> undoMgr) {
        undoMgr.registerActionType(PREZ_PAYS, new PrezPays());
        undoMgr.registerActionType(TAKE_LOAN, new TakeLoanAction());
        undoMgr.registerActionType(REPAY_LOAN, new RepayLoanAction());
        undoMgr.registerActionType(BEGIN_FORCED_SALE, new BeginForcedSale());
        undoMgr.registerActionType(CALL_LOANS, new CallLoans());
    }

    static class PrezPays extends Action {
        @Override public void checkAllowed(Move move, Game game) { }

        @Override public void init(Move move, Game game) {
            Player p = findPlayer(move.getPlayer(), game);
            if(move.getDetail().equals(BankActions.TRAIN)) {
                game.addSub(END_OP_TURN, "", move.getCorp(), 0, "");
            } else if (move.getDetail().equals(BankActions.INTEREST)) {
                if(p.cash < 0) game.addSub(BEGIN_FORCED_SALE, "", move.getCorp(), -p.cash, game.getBoard().activity);
            }
        }

        @Override public void doAction(Move move, Game game) {
            Player p = findPlayer(move.getPlayer(), game);
            Corp c = findCorp(move.getCorp(), game);
            game.getBank().player2Corp(p, c, move.getAmount());
        }

        @Override public void undoAction(Move move, Game game) {
            Player p = findPlayer(move.getPlayer(), game);
            Corp c = findCorp(move.getCorp(), game);
            game.getBank().corp2Player(c, p, move.getAmount());
        }
    }

    static class TakeLoanAction extends Action {

        @Override public void checkAllowed(Move move, Game game) {
            assertPhase(game, Game.Era.OP, "TakeLoan");
            assertCorpTurn(game, move.getCorp(), "TakeLoan");
            Corp c = findCorp(move.getCorp(), game);
            if(heldShareCount(move.getCorp(), game) <= c.loans) throw new IllegalStateException("Too many loans");
            if(c.loanTaken) throw new IllegalStateException("Only one loan per turn");
        }

        @Override public void init(Move move, Game game) { }

        @Override public void doAction(Move move, Game game) {
            int amount = (game.getBoard().activity.equals(OP_POST)) ? 90 : 100;
            Corp c = findCorp(move.getCorp(), game);
            game.getBank().payCorp(c.name, amount);
            c.loanTaken = true;
            c.loans++;
        }

        @Override public void undoAction(Move move, Game game) {
            int amount = (game.getBoard().activity.equals(OP_POST)) ? 90 : 100;
            Corp c = findCorp(move.getCorp(), game);
            game.getBank().debitCorp(c.name, amount);
            c.loanTaken = false;
            c.loans--;
        }
    }

    static class RepayLoanAction extends Action {
        @Override public void checkAllowed(Move move, Game game) {
            assertPhase(game, Game.Era.OP, "RepayLoan");
            assertCorpTurn(game, move.getCorp(), "RepayLoan");
            assertActivity(game, OP_POST, "RepayLoan");
            assertCorpFunds(game, move.getCorp(), 100 * move.getAmount(), "RepayLoan");
        }

        @Override public void init(Move move, Game game) { }

        @Override public void doAction(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            c.loans -= move.getAmount();
            game.getBank().debitCorp(move.getCorp(), 100 * move.getAmount());
        }

        @Override public void undoAction(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            c.loans += move.getAmount();
            game.getBank().payCorp(move.getCorp(), 100 * move.getAmount());
        }
    }

    static class BeginForcedSale extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) { }

        @Override public void doAction(Move move, Game game) {
            game.getBoard().activity = FORCED_SALE_ACTIVITY;
        }

        @Override public void undoAction(Move move, Game game) {
            game.getBoard().activity = move.getDetail();
        }
    }

    static class CallLoans extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) {
            if (move.getCorp().equals(move.getPlayer())) {
                game.addSub(FORM_CGR, "", "", 0, "");
                return;
            }
            Player p = findPlayer(move.getPlayer(), game);
            for (Stock s: p.shares) {
                if(!s.isPrez) continue;
                Corp c = findCorp(s.corpName, game);
                if(c.loans == 0) continue;
                if(c.cash >= 100) {
                    if(c.cash >= 100*c.loans) {
                        game.addSub(REPAY_LOAN, "", s.corpName, c.loans, "");
                        continue;
                    } else {
                        game.addSub(REPAY_LOAN, "", s.corpName, c.cash / 100, "");
                    }
                }
                if (p.cash <= c.loans * 100) {
                    game.addSub(ABANDON_CORP, "", s.corpName, 0, "");
                    continue;
                }
                return; // at least one decision to make
            }
            String endPlayer = move.getCorp().isEmpty() ? move.getPlayer() : move.getCorp();
            game.addSub(CALL_LOANS, nextPlayer(p.name, game).name, endPlayer, 0, CALL_LOAN_ACTIVITY);
        }

        @Override public void doAction(Move move, Game game) {
            game.getBoard().activity = CALL_LOAN_ACTIVITY;
        }

        @Override public void undoAction(Move move, Game game) {
            game.getBoard().activity = move.getDetail();
        }

    }

    static int heldShareCount(String corpName, Game game) {
        int count = 0;
        for (Player p:game.getBoard().getPlayers()) {
            for (Stock s: p.shares) {
                if(s.corpName.equals(corpName)) count += s.getAmount();
            }
        }
        return count;
    }
}
