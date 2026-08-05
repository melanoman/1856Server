package mel.volvox.GameChatServer.xx1856;

import mel.volvox.GameChatServer.comm.train.StockPrice;
import mel.volvox.GameChatServer.model.xx1856.Move;
import mel.volvox.undo.UndoManager;

import java.util.ArrayList;
import java.util.List;

import static mel.volvox.GameChatServer.xx1856.OpActions.OP_POST;
import static mel.volvox.GameChatServer.xx1856.Opcodes.*;

public class TrainActions {
    public static String POOL = "POOL";
    public static String BANK = "BANK";

    public static void registerAll(UndoManager<Move, Game, Action> undoMgr) {
        undoMgr.registerActionType(BUY_BANK_TRAIN, new BuyBankTrain());
        undoMgr.registerActionType(BUY_CORP_TRAIN, new BuyCorpTrain());
        undoMgr.registerActionType(FORCED_TRAIN, new ForcedTrain());
        undoMgr.registerActionType(RUST, new RustAction());
        undoMgr.registerActionType(BUY_PRIV, new BuyPriv());
        undoMgr.registerActionType(RUST_PRIV, new RustPriv());
        undoMgr.registerActionType(RUST_PORT, new RustPort());
        undoMgr.registerActionType(PLACE_PORT, new PlacePort());
        undoMgr.registerActionType(BUY_BRIDGE, new BuyBridge());
        undoMgr.registerActionType(BUY_TUNNEL, new BuyTunnel());
        undoMgr.registerActionType(CGR_SHELL, new CgrPhaseI());
        undoMgr.registerActionType(CGR_FILL, new CgrPhaseII());
    }

    static int getRustSize(int bankTrainCount) {
        if(bankTrainCount == 8) return 2;
        if(bankTrainCount == 1) return 3;
        return -1;
    }

    static final int PRIV_RUST_SIZE = 4;
    static final int PORT_RUST_SIZE = 1;

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

    static void makeRustSubs(Move move, Game game) {
        int trainCount = game.getBoard().trains.size();
        List<Corp> rustList = new ArrayList<>();
        int rustSize = getRustSize(game.getBoard().trains.size());
        if (rustSize > 0) for(Corp c:game.getBoard().corps) for(Integer t:c.trains) {
            if(t == rustSize) rustList.add(c);
        }
        for(Corp c: rustList) game.addSub(RUST, "", c.name, rustSize, "");

        if (trainCount == PRIV_RUST_SIZE) {
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
        if (trainCount == PORT_RUST_SIZE) {
            for(Corp c: game.getBoard().corps) {
                if(c.portRights) {
                    game.addSub(RUST_PORT, "", c.name, 0, "");
                }
            }
        }
        for(Corp c: game.getBoard().corps) {
            if(c.trains.size() > TRAIN_LIMIT[trainCount]) {
                throw new IllegalStateException("TODO DROP TRAIN OVER LIMIT");
                //game.addSub("START_TRAIN_LIMIT_DROP")
                //return board
            }
        }
    }

    static class RustPort extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) { }
        @Override public void doAction(Move move, Game game) {
            findCorp(move.getCorp(), game).portRights = false;
        }
        @Override public void undoAction(Move move, Game game) {
            findCorp(move.getCorp(), game).portRights = true;
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
            int limit = move.getCorp().equals("CGR") ? 3 : TRAIN_LIMIT[b.trains.size()]; //TODO put CGR in a constant
            if(findCorp(move.getCorp(), game).trains.size() >= limit) {
                throw new IllegalStateException("Too many trains");
            }
        }

        @Override public void init(Move move, Game game) {
            makeRustSubs(move, game);
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

    static int cheapestTrain(Board board) {
        int out = board.trains.isEmpty() ? 99 : board.trains.get(0);
        for(int train: board.pool) {
            if(train > 0 && train < out) out = train;
        }
        return out==99 ? 0 : out;
    }

    static class ForcedTrain extends Action {
        @Override public void checkAllowed(Move move, Game game) {
            assertPhase(game, Game.Era.OP, "ForcedTrain");
            assertActivity(game, OP_POST, "ForcedTrain");
            assertCorpTurn(game, move.getCorp(), "ForcedTrain");
            Corp c = findCorp(move.getCorp(), game);
            if(!c.trains.isEmpty()) {
                throw new IllegalStateException("No Prez contributions to train purchase unless zero trains");
            }
            Board board = game.getBoard();
            int train = cheapestTrain(board);
            if(move.getAmount() != train) {
                throw new IllegalStateException("Must buy cheapest train when prez contributes");
            }
            if(c.cash >= TRAIN_PRICE[train]) {
                throw new IllegalStateException("May not contribute when corp has enough funds.");
            }
            if("POOL".equals(move.getDetail())) {
                if(!board.pool.contains(train)) throw new IllegalStateException("Train not in pool");
            } else if (train > 0 && !board.trains.contains(train)) {
                throw new IllegalStateException("Train not in bank");
            }
        }

        @Override public void init(Move move, Game game) {
            if (move.getDetail().equals(BANK)) {
                makeRustSubs(move, game);
            }
            Corp c = findCorp(move.getCorp(), game);
            Player p = findPrez(c.name, game);
            game.addSub(PREZ_PAYS, p.name, move.getCorp(), -c.cash, BankActions.TRAIN);
        }

        @Override public void doAction(Move move, Game game) {
            if (POOL.equals(move.getDetail())) {
                game.getBoard().pool.remove(Integer.valueOf(move.getAmount()));
            } else if (move.getAmount() > 0) {
                game.getBoard().trains.remove(0);
            }
            Corp c = findCorp(move.getCorp(), game);
            c.trains.add(move.getAmount());
            game.getBank().debitCorp(move.getCorp(), TRAIN_PRICE[move.getAmount()]);
        }

        @Override public void undoAction(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            c.trains.remove(Integer.valueOf(move.getAmount()));
            game.getBank().payCorp(move.getCorp(), TRAIN_PRICE[move.getAmount()]);
            if (POOL.equals(move.getDetail())) {
                game.getBoard().pool.add(move.getAmount());
            } else if (move.getAmount() > 0) {
                game.getBoard().trains.add(0, move.getAmount());
            }
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
            switch(move.getDetail()) {
                case Priv.NIAG -> c.bridgeRights = true;
                case Priv.STC -> c.tunnelRights = true;
            }
            updatePort(game, p);
        }

        @Override public void undoAction(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            Player p = findPlayer(move.getPlayer(), game);
            p.privs.add(move.getDetail());
            c.privs.remove(move.getDetail());
            game.getBank().player2Corp(p, c, move.getAmount());
            switch(move.getDetail()) {
                case Priv.NIAG -> c.bridgeRights = false;
                case Priv.STC -> c.tunnelRights = false;
            }
            updatePort(game, p);
        }
    }

    static class CgrPhaseI extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) { }

        @Override public void doAction(Move move, Game game) {
            Corp cgr = new Corp("CGR", 10);
            Player p = findPlayer(move.getPlayer(), game);
            for (Stock s: p.shares) if("CGR".equals(s.corpName)) s.isPrez = true;
            cgr.cash = Integer.parseInt(move.getCorp());
            cgr.poolShares = move.getAmount();
            cgr.bankShares = Integer.parseInt(move.getDetail());
            game.getBoard().corps.add(cgr);
        }

        @Override public void undoAction(Move move, Game game) {
            game.getBoard().corps.removeIf(x->x.name.equals("CGR"));
        }
    }

    static class CgrPhaseII extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) {
            game.addSub(RESORT_CORP, "", "CGR", 0, "");
        }

        @Override public void doAction(Move move, Game game) {
            Corp cgr = findCorp("CGR", game);
            cgr.par = Integer.parseInt(move.getPlayer());
            cgr.price = makeCGRPrice(cgr.par);
            // TODO extract halfShare info from Corp field
            cgr.hasOperated = move.getAmount()/4 % 2 == 1;
            cgr.bridgeRights = move.getAmount()/2 % 2 == 1;
            cgr.tunnelRights = move.getAmount() % 2 == 1;
            // TODO extract trains
            for (int i=0; i< move.getDetail().length(); i++) {
                cgr.trains.add(parseTrain(move.getDetail().charAt(i)));
            }
        }

        @Override public void undoAction(Move move, Game game) {
        }
    }

    private static StockPrice makeCGRPrice(int par) {
        return new StockPrice(par, 5, 0); //TODO figure out correct location
    }

    private static Integer parseTrain(char c) {
        return c - '0';
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

    static class PlacePort extends Action {
        @Override public void checkAllowed(Move move, Game game) {
            assertPhase(game, Game.Era.OP, "PlacePort");
            assertCorpTurn(game, move.getCorp(), "PlacePort");
            if(!findCorp(move.getCorp(), game).privs.contains(Priv.GLS)) {
                throw new IllegalStateException(move.getCorp()+" does not own GLS");
            }
        }
        @Override public void init(Move move, Game game) { }

        @Override public void doAction(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            c.portRights = true;
            c.privs.remove(Priv.GLS);
        }

        @Override public void undoAction(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            c.portRights = false;
            c.privs.add(move.getAmount(), Priv.GLS);
        }
    }

    static class BuyBridge extends Action {
        @Override public void checkAllowed(Move move, Game game) {
            assertPhase(game, Game.Era.OP, "BuyBridge");
            assertCorpTurn(game, move.getCorp(), "BuyBridge");
            assertCorpFunds(game, move.getCorp(), 50, "BuyBridge");
            if(game.getBoard().bridgeTokens < 1) throw new IllegalStateException("No bridge tokens remain");
        }

        @Override public void init(Move move, Game game) { }

        @Override public void doAction(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            game.getBank().debitCorp(move.getCorp(), 50);
            game.getBoard().bridgeTokens--;
            c.bridgeRights = true;
            for (Corp cc: game.getBoard().corps) {
                if (cc.privs.contains(Priv.NIAG)) game.getBank().payCorp(cc.name, 50);
            }
        }

        @Override public void undoAction(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            game.getBank().payCorp(move.getCorp(), 50);
            game.getBoard().bridgeTokens++;
            c.bridgeRights = false;
            for (Corp cc: game.getBoard().corps) {
                if (cc.privs.contains(Priv.NIAG)) game.getBank().debitCorp(cc.name, 50);
            }
        }
    }

    static class BuyTunnel extends Action {
        @Override public void checkAllowed(Move move, Game game) {
            assertPhase(game, Game.Era.OP, "BuyTunnel");
            assertCorpTurn(game, move.getCorp(), "BuyTunnel");
            assertCorpFunds(game, move.getCorp(), 50, "BuyTunnel");
            if(game.getBoard().tunnelTokens < 1) throw new IllegalStateException("No tunnel tokens remain");
        }

        @Override public void init(Move move, Game game) { }

        @Override public void doAction(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            game.getBank().debitCorp(move.getCorp(), 50);
            game.getBoard().tunnelTokens--;
            c.tunnelRights = true;
            for (Corp cc: game.getBoard().corps) {
                if (cc.privs.contains(Priv.STC)) game.getBank().payCorp(cc.name, 50);
            }
        }

        @Override public void undoAction(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            game.getBank().payCorp(move.getCorp(), 50);
            game.getBoard().tunnelTokens++;
            c.tunnelRights = false;
            for (Corp cc: game.getBoard().corps) {
                if (cc.privs.contains(Priv.STC)) game.getBank().debitCorp(cc.name, 50);
            }
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
