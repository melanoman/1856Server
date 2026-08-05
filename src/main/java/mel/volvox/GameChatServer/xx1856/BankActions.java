package mel.volvox.GameChatServer.xx1856;

import jakarta.persistence.criteria.CriteriaBuilder;
import mel.volvox.GameChatServer.model.xx1856.Move;
import mel.volvox.undo.UndoManager;

import java.util.ArrayList;
import java.util.List;

import static mel.volvox.GameChatServer.xx1856.OpActions.OP_POST;
import static mel.volvox.GameChatServer.xx1856.Opcodes.*;

public class BankActions {
    public static String CGR = "CGR";
    public static String INTEREST = "interest";
    public static String TRAIN = "train";
    public static String FORCED_SALE_ACTIVITY = "forceSale";
    public static String CALL_LOAN_ACTIVITY = "callLoan";
    public static String FORM_CGR_ACTIVITY = "formCGR";

    public static void registerAll(UndoManager<Move, Game, Action> undoMgr) {
        undoMgr.registerActionType(PREZ_PAYS, new PrezPays());
        undoMgr.registerActionType(TAKE_LOAN, new TakeLoanAction());
        undoMgr.registerActionType(REPAY_LOAN, new RepayLoanAction());
        undoMgr.registerActionType(BEGIN_FORCED_SALE, new BeginForcedSale());
        undoMgr.registerActionType(CALL_LOANS, new CallLoans());
        undoMgr.registerActionType(SAVE_CORP, new SaveCorp());
        undoMgr.registerActionType(ABANDON_CORP, new AbandonCorp());
        undoMgr.registerActionType(FORM_CGR, new FormCGR());
        undoMgr.registerActionType(CLOSE_CORP, new CloseCorp());
        undoMgr.registerActionType(ASK_CGR_TOKENS, new AskTokens());
        undoMgr.registerActionType(ANSWER_CGR_TOKENS, new AnswerTokens());
        undoMgr.registerActionType(DONE_DROP, new DoneDrop());
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
            if (!game.getBoard().currentPlayer.equals(move.getPlayer())) {
                game.addSub(CHANGE_PLAYER, move.getPlayer(), "", 0, game.getBoard().currentPlayer);
            }
            Player p = findPlayer(move.getPlayer(), game);

            for (Stock s: p.shares) {
                if(!s.isPrez) continue;
                Corp c = findCorp(s.corpName, game);
                if (c.abandoned) continue;
                if(c.loans == 0) continue;
                if(c.cash >= 100) {
                    if(c.cash >= 100*c.loans) {
                        game.addSub(REPAY_LOAN, "", s.corpName, c.loans, "");
                        continue;
                    } else {
                        game.addSub(REPAY_LOAN, "", s.corpName, c.cash / 100, "");
                    }
                }
                if (p.cash < c.loans * 100) {
                    game.addSub(ABANDON_CORP, p.name, s.corpName, 0, move.getDetail());
                    continue;
                }
                return; // at least one decision to make
            }
            Player next = nextPlayer(p.name, game);
            while(noCGRWork(next, game)) {
                if(next.name.equals(move.getPlayer())) {
                    game.addSub(FORM_CGR, findPrez(game.getBoard().currentCorp, game).name, "", 0, game.getBoard().activity);
                    return;
                }
                next = nextPlayer(next.name, game);
            }
            game.addSub(CALL_LOANS, next.name, "", 0, CALL_LOAN_ACTIVITY);
        }

        static boolean noCGRWork(Player player, Game game) {
            for(Stock s: player.shares) {
                if(!s.isPrez) continue;
                Corp c = findCorp(s.corpName, game);
                if (c.abandoned || c.loans == 0) continue;
                return false;
            }
            return true;
        }

        @Override public void doAction(Move move, Game game) {
            game.getBoard().activity = CALL_LOAN_ACTIVITY;
        }

        @Override public void undoAction(Move move, Game game) {
            game.getBoard().activity = move.getDetail();
        }
    }

    static class AbandonCorp extends Action {
        @Override public void checkAllowed(Move move, Game game) {
            assertPhase(game, Game.Era.OP, "AbandonCorp");
            assertActivity(game, CALL_LOAN_ACTIVITY, "AbandonCorp");
            Corp c = findCorp(move.getCorp(), game);
            if(c.abandoned) throw new IllegalStateException(c.name+" already abandoned");
            if(c.cash >= c.loans * 100) throw new IllegalStateException(c.name+" is solvent");
            Player prez = findPrez(c.name, game);
            if (!prez.name.equals(move.getPlayer())) {
                throw new IllegalStateException(move.getPlayer()+" is not prez of "+c.name);
            }
        }

        @Override public void init(Move move, Game game) {
            game.addSub(CALL_LOANS, move.getPlayer(), "", 0, CALL_LOAN_ACTIVITY);
        }

        @Override public void doAction(Move move, Game game) {
            findCorp(move.getCorp(), game).abandoned = true;
        }

        @Override public void undoAction(Move move, Game game) {
            findCorp(move.getCorp(), game).abandoned = false;
        }
    }

    static class SaveCorp extends Action {
        @Override public void checkAllowed(Move move, Game game) {
            assertPhase(game, Game.Era.OP, "SaveCorp");
            assertActivity(game, CALL_LOAN_ACTIVITY, "SaveCorp");
            assertPlayerTurn(game, move.getPlayer(), "saveCorp");
            Corp c = findCorp(move.getCorp(), game);
            if(c.abandoned) throw new IllegalStateException("Corp already abandoned");
            if(c.loans == 0) throw new IllegalStateException("Nothing to redeem");
            Player prez = findPrez(c.name, game);
            if (!prez.name.equals(move.getPlayer())) {
                throw new IllegalStateException("Only Prez can contribute");
            }
            assertPlayerFunds(game, prez.name, c.loans * 100, "SaveCorp");
        }

        @Override public void init(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            int amount = c.loans;
            game.addSub(REPAY_LOAN, "", move.getCorp(), amount, "");
            game.addSub(PREZ_PAYS, move.getPlayer(), move.getCorp(), 100*amount, "");
            game.addSub(CALL_LOANS, move.getPlayer(), "", 0, CALL_LOAN_ACTIVITY);
        }

        @Override public void doAction(Move move, Game game) { }
        @Override public void undoAction(Move move, Game game) { }
    }

    static class FormCGR extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) {
            List<String> losers = new ArrayList<>();
            for(Corp c: game.getBoard().corps) if(c.abandoned) losers.add(c.name);
            if(losers.isEmpty()) {
                throw new IllegalStateException("TODO ABORT CGR FORMATION / NEXT OP TURN");
            } else  {
                int issueCount = 0;
                int poolShares = 0;
                int prezShares = 0;
                String prezName = "";
                Player startPlayer = findPlayer(game.getBoard().currentPlayer, game);
                Player currentPlayer = startPlayer;
                do {
                    List<Stock> purge = new ArrayList<>();
                    int tradeShares = 0;
                    for(Stock s: currentPlayer.shares) {
                        if(losers.contains(s.corpName)) {
                            tradeShares += s.amount;
                            purge.add(s);
                        }
                    }

                    if (tradeShares % 2 == 1) poolShares++;
                    tradeShares /= 2;
                    if (tradeShares + issueCount > 20) tradeShares = 20 - issueCount;
                    issueCount += tradeShares;
                    if (tradeShares > 0) {
                        game.addSub(ADD_SHARES, currentPlayer.getName(), CGR, tradeShares, "");
                    }
                    if (tradeShares > prezShares) {
                        prezShares = tradeShares;
                        prezName = currentPlayer.name;
                    }
                    currentPlayer = nextPlayer(currentPlayer.name, game);
                } while(currentPlayer != startPlayer);
                int looseCash = 0;
                boolean bridgeRights = false;
                boolean tunnelRights = false;
                boolean hasOperated = false;
                StringBuilder cgrTrains = new StringBuilder(); // each char is a train
                for(Corp c: game.getBoard().corps) if(losers.contains(c.name)) {
                    poolShares += c.poolShares;
                    looseCash += c.cash;
                    if (c.bridgeRights) bridgeRights = true;
                    if (c.tunnelRights) tunnelRights = true;
                    if (c.hasOperated) hasOperated = true;
                    for(Integer train: c.trains) cgrTrains.append(train);
                }

                int cgrPar = calculatePar(losers, game);
                for(String s: losers) {
                    game.addSub(CLOSE_CORP, "", s, 0, "");
                }
                poolShares /= 2;
                if (poolShares + issueCount > 20) poolShares = 20 - issueCount;
                if (poolShares < 0) poolShares = 0;
                boolean halfShares = (poolShares + issueCount > 10);
                int bankShares = (halfShares ? 20 : 10) - issueCount - poolShares;
                game.addSub(CGR_SHELL, prezName, ""+looseCash, poolShares, ""+bankShares);
                int rights = (hasOperated ? 4 : 0) + (bridgeRights ? 2: 0) + (tunnelRights ? 1 : 0);
                game.addSub(CGR_FILL, ""+cgrPar, ""+halfShares, rights, cgrTrains.toString());
                game.addSub(ASK_CGR_TOKENS, "", "", 0, game.getBoard().activity);
            }
        }

        @Override public void doAction(Move move, Game game) {
            game.getBoard().loansDone = true;
        }
        @Override public void undoAction(Move move, Game game) {
            game.getBoard().loansDone = false;
        }
    }

    static class AskTokens extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) { }
        @Override public void doAction(Move move, Game game) {
            game.getBoard().activity = ASK_CGR_TOKENS;
        }

        @Override public void undoAction(Move move, Game game) {
            game.getBoard().activity = move.getDetail();
        }
    }

    static class AnswerTokens extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) { }
        @Override public void doAction(Move move, Game game) {
            findCorp(CGR, game).tokensUsed = move.getAmount();
            game.getBoard().activity = ASK_CGR_TRAINS;
        }

        @Override public void undoAction(Move move, Game game) {
            game.getBoard().activity = ASK_CGR_TOKENS;
        }
    }

    static class CloseCorp extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) {
            List<Holding> purge = new ArrayList<>();
            for(Player p: game.getBoard().players) {
                for(Stock s: p.shares) {
                    if (s.corpName.equals(move.getCorp())) {
                        purge.add(new Holding(p.name, s));
                    }
                }
            }
            for (Holding h: purge) {
                int amount = h.share.isPrez ? -h.share.amount : h.share.amount;
                game.addSub(PURGE_SHARES, h.playerName, h.share.corpName, amount, "");
            }
            Corp c = findCorp(move.getCorp(), game);
            int refund = move.getAmount() > 0 ? c.cash : 0;
            if (refund != 0 || c.escrow != 0) game.addSub(REPO_CASH, "", c.name, refund, ""+c.escrow);
            int index = findCorpIndex(move.getCorp(), game);
            game.addSub(RESORT_CORP, "", move.getCorp(), index, "");
        }

        @Override public void doAction(Move move, Game game) {
            findCorp(move.getCorp(), game).closed = true;
        }

        @Override public void undoAction(Move move, Game game) {
            findCorp(move.getCorp(), game).closed = false;
        }
    }

    static class DoneDrop extends Action {
        @Override public void checkAllowed(Move move, Game game) {
            assertPhase(game, Game.Era.OP, "DoneDrop");
            assertActivity(game, ASK_CGR_TRAINS, "DoneDrop");
            Corp c = findCorp(CGR, game);
            if(c.trains.size() > 3) throw new IllegalStateException("Too many trains");
        }

        @Override public void init(Move move, Game game) {
            game.addSub(END_OP_TURN, "", game.getBoard().currentCorp, 0, "");
        }

        @Override public void doAction(Move move, Game game) {
            Corp c = findCorp(CGR, game);
            for(Integer train: c.trains) {
                if (train == 0 || train > 4) {
                    game.getBoard().loanerDiesel = false;
                }
            }
        }

        @Override public void undoAction(Move move, Game game) {
            game.getBoard().loanerDiesel = true;
        }
    }

    static int calculatePar(List<String> losers, Game game) {
        if(losers.size() > 2) {
            // TODO big calc
        } else {
            // TODO small calc
        }
        return 100;
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
