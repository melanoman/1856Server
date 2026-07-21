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
        undoMgr.registerActionType(WITHHOLD, new WithholdAction());
        undoMgr.registerActionType(CHANGE_RUN, new ChangeRunAction());
        undoMgr.registerActionType(PAYDIV, new PayDivAction());
        undoMgr.registerActionType(PAY_INTEREST, new PayInterestAction());
        undoMgr.registerActionType(DISBURSE, new DisburseAction());
        undoMgr.registerActionType(RESET_LOAN, new ResetTokenAction());
        undoMgr.registerActionType(RESET_TOKEN, new ResetLoanAction());
        undoMgr.registerActionType(FLOAT, new FloatAction());
        undoMgr.registerActionType(DESTINATION_REACHED, new DestinationAction());
        undoMgr.registerActionType(RELEASE_ESCROW, new ReleaseEscrow());
        undoMgr.registerActionType(BUY_BANK_TRAIN, new BuyBankTrainAction());
    }

    //detail == former phase amount = 0 for reset, 1 for continue
    static class StartOpRound extends Action {
        @Override public void checkAllowed(Move move, Game game) { }

        @Override public void init(Move move, Game game) {
            game.addSub(CHANGE_ACTIVITY, OP_PRE, "", 0, game.getBoard().activity);
            Corp c = game.getBoard().corps.get(0);
            if (c.par < 65) {
                // TODO NULL OP ROUND
                throw new IllegalStateException("TODO OP ROUND WITH NO COMPANIES");
            } else {
                game.addSub(CHANGE_CORP, "", game.getBoard().corps.get(0).name, 0, game.getBoard().currentCorp);
                game.addSub(START_OP_TURN, "", game.getBoard().corps.get(0).name, 0, "");
            }
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

    static class WithholdAction extends Action {
        @Override public void checkAllowed(Move move, Game game) {
            assertPhase(game, Game.Era.OP, "Withhold");
            assertActivity(game, OP_PRE, "Withhold");
            assertCorpTurn(game, move.getCorp(), "Withhold");
            if(move.getAmount() % 10 > 0) {
                throw new IllegalStateException("Revenue must be a multiple of 10");
            }
        }

        @Override public void init(Move move, Game game) {
            //TODO stock move (left)
            Corp c = findCorp(move.getCorp(), game);
            game.addSub(CHANGE_RUN, "", c.name, move.getAmount(), ""+c.lastRun);
        }

        @Override public void doAction(Move move, Game game) {
            game.getBank().payCorp(move.getCorp(), move.getAmount());
            game.getBoard().activity = OP_POST;
        }

        @Override public void undoAction(Move move, Game game) {
            game.getBank().debitCorp(move.getCorp(), move.getAmount());
            game.getBoard().activity = OP_PRE;
        }
    }

    static class PayDivAction extends Action {
        @Override public void checkAllowed(Move move, Game game) {
            assertPhase(game, Game.Era.OP, "PayDiv");
            assertActivity(game, OP_PRE, "PayDiv");
            assertCorpTurn(game, move.getCorp(), "PayDiv");
            if(move.getAmount() % 10 > 0) {
                throw new IllegalStateException("Revenue must be a multiple of 10");
            }
            Corp c = findCorp(move.getCorp(), game);
            int overhead = c.loans*10 - c.cash;
            if (overhead < 0) overhead = 0;
            if (move.getAmount() < 10 - overhead) {
                throw new IllegalStateException("Minimum payout is $10");
            }
        }

        @Override public void init(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            int paid = 0;
            if (c.cash < c.loans/10) {
                int available = (c.cash / 10) * 10; //must be multiple of 10
                paid = (available > c.loans*10) ? paid : available;
                if (paid > 0) game.addSub(PAY_INTEREST, "", move.getCorp(), paid, "");
            }
            int due = c.loans*10 - paid;
            game.addSub(DISBURSE, "", move.getCorp(), (move.getAmount() - paid)/10, "");
            //TODO move stock(right)
            game.addSub(CHANGE_RUN, "", c.name, move.getAmount(), ""+c.lastRun);
        }

        @Override public void doAction(Move move, Game game) {
            game.getBoard().activity = OP_POST;
        }
        @Override public void undoAction(Move move, Game game) {
            game.getBoard().activity = OP_PRE;
        }
    }

    static class DisburseAction extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) { }

        @Override public void doAction(Move move, Game game) {
            for(Player p:game.getBoard().getPlayers()) {
                for(Stock s:p.shares) {
                    if(s.corpName.equals(move.getCorp())) {
                        game.getBank().payPlayer(p.name, s.amount * move.getAmount());
                    }
                }
            }
            game.getBank().payCorp(move.getCorp(), findCorp(move.getCorp(), game).poolShares * move.getAmount());
        }

        @Override public void undoAction(Move move, Game game) {
            for(Player p:game.getBoard().getPlayers()) {
                for(Stock s:p.shares) {
                    if(s.corpName.equals(move.getCorp())) {
                        game.getBank().debitPlayer(p.name, s.amount * move.getAmount());
                    }
                }
            }
            game.getBank().debitCorp(move.getCorp(), findCorp(move.getCorp(), game).poolShares * move.getAmount());

        }
    }

    static class PayInterestAction extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) { }

        @Override public void doAction(Move move, Game game) {
            game.getBank().debitCorp(move.getCorp(), move.getAmount());
        }

        @Override public void undoAction(Move move, Game game) {
            game.getBank().payCorp(move.getCorp(), move.getAmount());

        }
    }

    static class DestinationAction extends Action {
        @Override public void checkAllowed(Move move, Game game) {
            assertPhase(game, Game.Era.OP, "DestReached");
            assertCorpTurn(game, move.getCorp(), "DestReached");
            Corp c = findCorp(move.getCorp(), game);
            if(c.destinationSatisfied) throw new IllegalStateException("Dest Already Reached");
        }

        @Override public void init(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            if(c.escrow > 0) game.addSub(RELEASE_ESCROW, "", move.getCorp(), c.escrow, "");
        }

        @Override
        public void doAction(Move move, Game game) {
            findCorp(move.getCorp(), game).destinationSatisfied = true;
        }

        @Override
        public void undoAction(Move move, Game game) {
            findCorp(move.getCorp(), game).destinationSatisfied = false;
        }
    }

    static class ReleaseEscrow extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) { }

        @Override
        public void doAction(Move move, Game game) {
            findCorp(move.getCorp(), game).cash += move.getAmount();
            findCorp(move.getCorp(), game).escrow -= move.getAmount();
        }

        @Override
        public void undoAction(Move move, Game game) {
            findCorp(move.getCorp(), game).cash -= move.getAmount();
            findCorp(move.getCorp(), game).escrow += move.getAmount();
        }
    }

    static class ChangeRunAction extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) { }

        @Override
        public void doAction(Move move, Game game) {
            findCorp(move.getCorp(), game).lastRun = move.getAmount();
        }

        @Override
        public void undoAction(Move move, Game game) {
            findCorp(move.getCorp(), game).lastRun = Integer.parseInt(move.getDetail());
        }
    }

    static class BuyBankTrainAction extends Action {
        @Override public void checkAllowed(Move move, Game game) {
            assertPhase(game, Game.Era.OP, "BuyBankTrain");
            assertCorpTurn(game, move.getCorp(), "BuyBankTrain");
            assertActivity(game, OP_POST, "BuyBankTrain");
            Board b = game.getBoard();
            if(b.getTrains().isEmpty()) {
                throw new IllegalStateException("Bank sold out of numbered trains");
            }
            if(b.trains.get(0) != move.getAmount()) {
                throw new IllegalStateException("Current bank train is "+b.trains.get(0)+" not "+move.getAmount());
            }
            assertCorpFunds(game, move.getCorp(), TRAIN_PRICE[move.getAmount()], "BuyBankTrain");
            int limit = move.getCorp().equals("CGR") ? 3 : TRAIN_LIMIT[b.trains.size()];
            if(findCorp(move.getCorp(), game).trains.size() >= limit) {
                throw new IllegalStateException("Too many trains");
            }
        }

        @Override public void init(Move move, Game game) {
            //TODO enforce train limit change
        }

        @Override public void doAction(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            c.trains.add(move.getAmount());
            game.getBoard().trains.remove(0);
            game.getBank().debitCorp(c.name, TRAIN_PRICE[move.getAmount()]);
        }

        @Override public void undoAction(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            game.getBoard().trains.add(0, move.getAmount());
            c.trains.remove((Integer) move.getAmount());
            game.getBank().payCorp(c.name, TRAIN_PRICE[move.getAmount()]);
        }
    }

    public static int[] TRAIN_PRICE = { 0, 0, 100, 225, 350, 550, 700 };
    final static int[] TRAIN_LIMIT = {
            2,
            2, 2,
            2, 2, 3,
            3, 3, 3, 4,
            4, 4, 4, 4, 4,
            4, 4, 4, 4, 4, 4
    };
}
