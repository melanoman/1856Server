package mel.volvox.GameChatServer.xx1856;

import mel.volvox.GameChatServer.model.xx1856.Move;
import mel.volvox.undo.UndoManager;

import java.util.ArrayList;
import java.util.List;

import static mel.volvox.GameChatServer.xx1856.OpActions.OP_POST;
import static mel.volvox.GameChatServer.xx1856.Opcodes.*;

public class TrainActions {
    public static void registerAll(UndoManager<Move, Game, Action> undoMgr) {
        undoMgr.registerActionType(BUY_BANK_TRAIN, new BuyBankTrain());
        undoMgr.registerActionType(BUY_CORP_TRAIN, new BuyCorpTrain());
        undoMgr.registerActionType(RUST, new RustAction());
        undoMgr.registerActionType(BUY_PRIV, new BuyPriv());
        undoMgr.registerActionType(RUST_PRIV, new RustPriv());
    }

    static int getRustSize(int bankTrainCount) {
        if(bankTrainCount == 8) return 2;
        if(bankTrainCount == 1) return 3;
        return -1;
    }

    static final int PRIV_RUST_SIZE = 4;

    //player=train corp=buyer detail=seller amount=price
    static class BuyCorpTrain extends Action {
        @Override public void checkAllowed(Move move, Game game) {
            assertPhase(game, Game.Era.OP, "BuyCorpTrain");
            assertCorpTurn(game, move.getCorp(), "BuyCorpTrain");
            assertActivity(game, OP_POST, "BuyCorpTrain");
            if(move.getAmount() < 1) {
                throw new IllegalStateException("Minimum Price is $1");
            }
            assertCorpFunds(game, move.getCorp(), move.getAmount(), "BuyCorpTrain");
            int train = trainValue(move.getPlayer());
            // TODO enforce CGR at face value
            Corp seller = findCorp(move.getDetail(), game);
            if (!seller.trains.contains(train)) {
                throw new IllegalStateException("Seller " + seller.name + " does not have train " + move.getPlayer());
            }
            int limit = move.getCorp().equals("CGR") ? 3 : TRAIN_LIMIT[game.getBoard().trains.size()];
            if(findCorp(move.getCorp(), game).trains.size() >= limit) {
                throw new IllegalStateException("Too many trains");
            }
        }

        @Override public void init(Move move, Game game) { }

        @Override public void doAction(Move move, Game game) {
            Corp buyer = findCorp(move.getCorp(), game);
            Corp seller = findCorp(move.getDetail(), game);
            Integer train = trainValue(move.getPlayer());
            int price = move.getAmount();
            buyer.cash -= price;
            seller.cash += price;
            seller.trains.remove(train);
            buyer.trains.add(train);
        }

        @Override public void undoAction(Move move, Game game) {
            Corp buyer = findCorp(move.getCorp(), game);
            Corp seller = findCorp(move.getDetail(), game);
            Integer train = trainValue(move.getPlayer());
            int price = move.getAmount();
            seller.cash -= price;
            buyer.cash += price;
            buyer.trains.remove(train);
            seller.trains.add(train);
        }
    }

    static class BuyBankTrain extends Action {
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
            List<Corp> rustList = new ArrayList<>();
            int rustSize = getRustSize(game.getBoard().trains.size());
            if (rustSize > 0) for(Corp c:game.getBoard().corps) for(Integer t:c.trains) {
                if(t == rustSize) rustList.add(c);
            }
            for(Corp c: rustList) game.addSub(RUST, "", c.name, rustSize, "");


            if (game.getBoard().trains.size() == PRIV_RUST_SIZE) {
                List<String> nuke = new ArrayList<>();
                for(Player p:game.getBoard().players) for(String pp:p.privs) nuke.add(pp+":"+p.name);
                for(String s:nuke) {
                    String[] ss = s.split(":", 2);
                    game.addSub(RUST_PRIV, ss[1], "", 0, ss[0]);
                }
                nuke = new ArrayList<>();
                for(Corp c:game.getBoard().corps) for(String pp:c.privs) nuke.add(pp+":"+c.name);
                for(String s:nuke) {
                    String[] ss = s.split(":", 2);
                    game.addSub(RUST_PRIV, "", ss[1], 0, ss[0]);
                }
            }
            //TODO trigger CGR formation
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

    static class BuyPriv extends Action {
        @Override public void checkAllowed(Move move, Game game) {
            assertPhase(game, Game.Era.OP, "buyPriv");
            assertCorpTurn(game, move.getCorp(), "buyPriv");
            assertCorpFunds(game, move.getCorp(), move.getAmount(), "buyPriv");
            Player p = findPlayer(move.getPlayer(), game);
            if(!p.privs.contains(move.getDetail())) throw new IllegalStateException("Player does not own priv");
            int faceValue = findPriv(move.getDetail()).price;
            if(move.getAmount()>2*faceValue || move.getAmount()<faceValue/2) {
                throw new IllegalStateException("Price must be between 2x and half printed value");
            }
        }

        @Override public void init(Move move, Game game) { }

        @Override public void doAction(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            Player p = findPlayer(move.getPlayer(), game);
            p.privs.remove(move.getDetail());
            c.privs.add(move.getDetail());
            game.getBank().corp2Player(c, p, move.getAmount());
            updatePort(game, p);
        }

        @Override public void undoAction(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            Player p = findPlayer(move.getPlayer(), game);
            p.privs.add(move.getDetail());
            c.privs.remove(move.getDetail());
            game.getBank().player2Corp(p, c, move.getAmount());
            updatePort(game, p);
        }
    }

    private static int trainValue(String s) {
        if("D".equals(s)) return 0;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Unknown train type "+s);
        }
    }

    static class RustAction extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) { }

        @Override public void doAction(Move move, Game game) {
            findCorp(move.getCorp(), game).trains.remove(Integer.valueOf(move.getAmount()));
        }

        @Override public void undoAction(Move move, Game game) {
            findCorp((move.getCorp()), game).trains.add(0, move.getAmount());
        }
    }

    static class RustPriv extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) { }

        @Override public void doAction(Move move, Game game) {
            if(move.getPlayer().isEmpty()) {
                findCorp(move.getCorp(), game).privs.remove(move.getDetail());
            } else {
                findPlayer(move.getPlayer(), game).privs.remove(move.getDetail());
            }
        }

        @Override public void undoAction(Move move, Game game) {
            if (move.getPlayer().isEmpty()) {
                findCorp(move.getCorp(), game).privs.add(0, move.getDetail());
            } else {
                findPlayer(move.getPlayer(), game).privs.add(0, move.getDetail());
            }
        }
    }

    public static int[] TRAIN_PRICE = { 1100, 0, 100, 225, 350, 550, 700 };
    final static int[] TRAIN_LIMIT = {
            2,
            2, 2,
            2, 2, 3,
            3, 3, 3, 4,
            4, 4, 4, 4, 4,
            4, 4, 4, 4, 4, 4
    };
}
