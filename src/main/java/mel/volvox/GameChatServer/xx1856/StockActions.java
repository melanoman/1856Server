package mel.volvox.GameChatServer.xx1856;

import mel.volvox.GameChatServer.comm.train.StockPrice;
import mel.volvox.GameChatServer.model.xx1856.Move;
import mel.volvox.undo.UndoManager;

import java.util.List;

import static mel.volvox.GameChatServer.xx1856.Action.*;
import static mel.volvox.GameChatServer.xx1856.Opcodes.*;

public class StockActions {
    static final List<Game.Era> STOCK_OR_INITIAL = List.of(Game.Era.STOCK, Game.Era.INITIAL);
    static final List<Integer> VALID_PARS = List.of(65, 70, 75, 80, 90, 100);

    public static void registerAll(UndoManager<Move, Game, Action> undoMgr) {
        undoMgr.registerActionType(STOCK_PASS, new PassAction());
        undoMgr.registerActionType(STOCK_TURN, new StockTurnAction());
        undoMgr.registerActionType(END_STOCK_TURN, new EndStockTurn());
        undoMgr.registerActionType(SET_PAR, new SetParAction());
        undoMgr.registerActionType(BANK_BUY, new BuyBankAction());
        undoMgr.registerActionType(POOL_BUY, new BuyPoolAction());
        undoMgr.registerActionType(RESORT_CORP, new ResortCorpAction());
        undoMgr.registerActionType(END_STOCK_ROUND, new EndStockRoundAction());
    }

    static class PassAction extends Action {

        @Override public void checkAllowed(Move move, Game game) {
            assertPhases(game, STOCK_OR_INITIAL, "stockPass");
            if(!move.getPlayer().equals(game.getBoard().currentPlayer)) {
                throw new IllegalStateException("Wrong player: "+move.getPlayer());
            }
        }

        @Override public void init(Move move, Game game) {
            makePlayerAdvance(game);
            if (game.getBoard().priorityPlayer.equals(game.getBoard().currentPlayer)) {
                game.addSub(END_STOCK_ROUND, "", "", game.getBoard().maxOR, game.getBoard().phase);
            }
        }

        @Override public void doAction(Move move, Game game) { }
        @Override public void undoAction(Move move, Game game) { }
    }

    static class SetParAction extends Action {
        @Override public void checkAllowed(Move move, Game game) { }

        @Override public void init(Move move, Game game) {
            int oldIndex = findCorpIndex(move.getCorp(), game);
            game.addSub(RESORT_CORP, "", move.getCorp(), oldIndex, "");
        }

        @Override public void doAction(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            Player p = findPlayer(move.getPlayer(), game);
            c.par = move.getAmount();
            c.bankShares = 8;
            c.poolShares = 0;
            //TODO record float type
            if (c.incrementallyFunded) game.getBank().player2Corp(p, c, 2 * move.getAmount());
            c.price = StockPrice.makePar(move.getAmount());
            p.shares.add(new Stock(c.name, 2, true));
        }

        @Override public void undoAction(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            Player p = findPlayer(move.getPlayer(), game);
            c.par = 0;
            c.bankShares = 0;
            c.poolShares = 0;
            if (c.incrementallyFunded) game.getBank().corp2Player(c, p, 2 * move.getAmount());
            c.price = null;
            p.shares.removeIf(x -> x.corpName.equals(move.getCorp()));
        }
    }

    static class BuyBankAction extends Action {
        @Override public void checkAllowed(Move move, Game game) { }

        @Override
        public void init(Move move, Game game) {
            makePrezIf(move, game);
        }

        @Override
        public void doAction(Move move, Game game) {
            Player p = findPlayer(move.getPlayer(), game);
            Corp c = findCorp(move.getCorp(), game);
            c.bankShares--;
            addShareToPlayer(p, move.getCorp());
            if (c.incrementallyFunded) {
                if (c.bankShares >= 5) game.getBank().player2Corp(p, c, move.getAmount());
                else game.getBank().player2Escrow(p, c, move.getAmount());
            }
        }

        @Override
        public void undoAction(Move move, Game game) {
            Player p = findPlayer(move.getPlayer(), game);
            Corp c = findCorp(move.getCorp(), game);
            c.bankShares++;
            subtractShareFromPlayer(p, move.getCorp());
            if (c.incrementallyFunded) {
                if (c.bankShares > 5) game.getBank().corp2Player(c, p, move.getAmount());
                else game.getBank().escrow2Player(c, p, move.getAmount());
            }
        }
    }

    static class BuyPoolAction extends Action {
        @Override public void checkAllowed(Move move, Game game) { }

        @Override
        public void init(Move move, Game game) {
            makePrezIf(move, game);
        }

        @Override
        public void doAction(Move move, Game game) {
            Player p = findPlayer(move.getPlayer(), game);
            Corp c = findCorp(move.getCorp(), game);
            c.poolShares--;
            addShareToPlayer(p, move.getCorp());
            game.getBank().debitPlayer(move.getPlayer(), move.getAmount());
        }

        @Override
        public void undoAction(Move move, Game game) {
            Player p = findPlayer(move.getPlayer(), game);
            Corp c = findCorp(move.getCorp(), game);
            c.poolShares++;
            subtractShareFromPlayer(p, move.getCorp());
            game.getBank().payPlayer(move.getPlayer(), move.getAmount());
        }
    }

    static class ResortCorpAction extends Action {

        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) { }

        @Override public void doAction(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            game.getBoard().corps.remove(c);
            for(int i=game.getBoard().corps.size() - 1; i >= 0; i--) {
                Corp old = game.getBoard().corps.get(i);
                if (compareCorpOrder(c, old) > 0) continue;
                game.getBoard().corps.add(i+1, c);
                return;
            }
            game.getBoard().corps.add(0, c);
        }

        @Override
        public void undoAction(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            game.getBoard().corps.remove(c);
            game.getBoard().corps.add(move.getAmount(), c);
        }
    }

    static int compareCorpOrder(Corp c, Corp old) {
        if (c.par > 0 && old.par <= 0) return 1;
        if (c.par <=0 && old.par > 0) return -1;
        if (c.par <=0) return 0;
        if (c.price.getPrice() > old.price.getPrice()) return 1;
        if (c.price.getPrice() < old.price.getPrice()) return -1;
        return (c.price.getX() - old.price.getX());
    }

    static class EndStockRoundAction extends Action {

        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) {
            final Board board = game.getBoard(); //for line length only
            //TODO check sellouts
            //TODO check max stock -> end game
            game.addSub(START_OP_ROUND, "", "", 0, "");
        }

        @Override public void doAction(Move move, Game game) {
            game.getBoard().phase = Game.Era.OP.name();
            game.getBoard().thisOR = 1;
            game.getBoard().maxOR = calculateMaxOR(game);
        }

        @Override public void undoAction(Move move, Game game) {
            game.getBoard().phase = move.getDetail();
            game.getBoard().thisOR = move.getAmount() + 1;
            game.getBoard().maxOR = move.getAmount();
        }
    }

    static class StartStockRoundAction extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) { }

        @Override
        public void doAction(Move move, Game game) {
            game.getBoard().phase = Game.Era.STOCK.name();
            game.getBoard().activity = "";
            game.getBoard().currentPlayer = game.getBoard().priorityPlayer;
        }

        @Override
        public void undoAction(Move move, Game game) {
            game.getBoard().phase = Game.Era.OP.name();
            game.getBoard().activity = move.getDetail();
            game.getBoard().currentPlayer = move.getPlayer();
        }
    }

    // empty umbrella for processStockAction
    public static class StockTurnAction extends Action {
        @Override public void checkAllowed(Move move, Game game) { /* see processStockTurn */ }
        @Override public void init(Move move, Game game) { /* see processStockTurn */ }
        @Override public void doAction(Move move, Game game) { }
        @Override public void undoAction(Move move, Game game) { updatePort(game, move.getPlayer()); }
    }

    public static class EndStockTurn extends Action {
        @Override public void checkAllowed(Move move, Game game) { /* see processStockTurn */ }
        @Override public void init(Move move, Game game) { /* see processStockTurn */ }
        @Override public void doAction(Move move, Game game) { updatePort(game, move.getPlayer()); }
        @Override public void undoAction(Move move, Game game) { }
    }

    public static Board processStockTurn(StockTurn turn, String playerName, Game game) {
        //TODO checkAllowed
        assertPhases(game, STOCK_OR_INITIAL, "stockTurn");
        assertPlayerTurn(game, playerName, "stockTurn");
        if(game.getBoard().phase.equals(Game.Era.INITIAL.name()) && !turn.salesList.isEmpty()) {
            throw new IllegalStateException("No sales in first stock round.");
        }
        Player player = findPlayer(playerName, game);

        int cost = 0;
        Corp corp = null;
        if (turn.buyType != null) {
            corp = findCorp(turn.buyCorp, game);
            switch(turn.buyType) {
                case "par" -> checkPar(corp, turn.buyPar);
                case "bank" -> checkBank(corp);
                case "pool" -> checkPool(corp);
            }
            cost = calculateCost(corp, turn.buyType, turn.buyPar);
        }
        //TODO are sales sellable?

        if (!turn.buyFirst) cost -= calculateSalesValue(turn.salesList, game);
        if (cost > 0) assertPlayerFunds(game, playerName, cost, "stockBuy");
        //TODO check portfolio size

        game.addMove(STOCK_TURN, playerName, "", 0, "");
        if (turn.buyFirst) makeBuySubs(game, playerName, turn, corp);
        for(Stock s:turn.salesList) makeSaleSub(game, playerName, s);
        if (!turn.buyFirst) makeBuySubs(game, playerName, turn, corp);
        game.addSub(END_STOCK_TURN, playerName, "", 0, "");
        makePriorityAdvance(game);
        return game.getBoard();
    }

    private static void checkPar(Corp corp, int amount) {
        if (!VALID_PARS.contains(amount)) {
            throw new IllegalStateException("Invalid Par value " + amount);
        }
        if(corp.par > 0) {
            throw new IllegalStateException(corp.name+" already has its par set");
        }
    }

    private static void makeBuySubs(Game game, String playerName, StockTurn turn, Corp corp) {
        if(turn.buyType == null) return; //no purchase
        switch (turn.buyType) {
            case "par" -> game.addSub(SET_PAR, playerName, corp.name, turn.buyPar, "");
            case "bank" -> game.addSub(BANK_BUY, playerName, corp.name, corp.par, "");
            case "pool" -> game.addSub(POOL_BUY, playerName, corp.name, corp.price.getPrice(), "");
        }
    }

    private static void makeSaleSub(Game game, String playerName, Stock sale) {
        Corp c = findCorp(sale.corpName, game);
        game.addSub(STOCK_SALE, playerName, sale.corpName, sale.amount, previewDrop(sale, game));
    }

    private static String previewDrop(Stock sale, Game game) { return ""+0; } //TODO preview drop

    private static void checkPool(Corp corp) {
        if(corp.poolShares < 1) throw new IllegalStateException("No pool share available for "+corp.name);
    }

    private static void checkBank(Corp corp) {
        if(corp.bankShares < 1) throw new IllegalStateException("No bank share available for "+corp.name);
    }

    private static void addShareToPlayer(Player p, String corpName) {
        for(Stock s:p.shares) if(s.corpName.equals(corpName)) { s.amount++; return; }
        p.shares.add(new Stock(corpName, 1, false));
    }

    private static void makePrezIf(Move move, Game game) {
        Player p = findPlayer(move.getPlayer(), game);
        Stock s = getHolding(move.getCorp(), p);
        if(s == null || s.isPrez) return;
        Holding prez = findPrezHolding(move.getCorp(), game);
        if(s.amount > prez.share.amount) {
            game.addSub(CHANGE_PREZ, move.getPlayer(), move.getCorp(), 0, prez.playerName);
        }
    }

    private static void subtractShareFromPlayer(Player p, String corpName) {
        Stock nuke = null;
        for(Stock s:p.shares) if(s.corpName.equals(corpName)) {
            s.amount--;
            if (s.amount == 0) nuke = s;
        }
        p.shares.remove(nuke);
    }

    private static int calculateCost(Corp c, String buyType, int par) {
        return switch (buyType) {
            case "par" -> par * 2;
            case "bank" -> c.par;
            case "pool" -> c.price.getPrice();
            default -> 9999999; // SHOULD NOT HAPPEN
        };
    }

    final static int[] OP_COUNT = {
            3,
            3, 3,
            3, 3, 2,
            2, 2, 2, 2,
            2, 2, 2, 2, 1,
            1, 1, 1, 1, 1, 1
    };

    private static int calculateMaxOR(Game game) {
        return OP_COUNT[game.getBoard().trains.size()];
    }

    private static int calculateSalesValue(List<Stock> sales, Game game) { return 0; } //TODO calculate sales value
}
