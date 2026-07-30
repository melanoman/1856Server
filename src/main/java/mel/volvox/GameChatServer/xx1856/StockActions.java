package mel.volvox.GameChatServer.xx1856;

import mel.volvox.GameChatServer.comm.train.StockPrice;
import mel.volvox.GameChatServer.model.xx1856.Move;
import mel.volvox.undo.UndoManager;

import java.util.ArrayList;
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
        undoMgr.registerActionType(STOCK_SALE, new SaleAction());
        undoMgr.registerActionType(BLOCK_SALE, new BlockAction());
        undoMgr.registerActionType(CLEAR_BLOCK, new ClearBlock());
        undoMgr.registerActionType(START_STOCK_ROUND, new StartStockRoundAction());
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

    static class BlockAction extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) { }

        @Override public void doAction(Move move, Game game) {
            findPlayer(move.getPlayer(), game).blocks.add(move.getCorp());
        }

        @Override public void undoAction(Move move, Game game) {
            findPlayer((move.getPlayer()), game).blocks.remove(move.getCorp());
        }
    }

    static class ClearBlock extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) { }

        @Override public void doAction(Move move, Game game) {
            findPlayer(move.getPlayer(), game).blocks.remove(move.getCorp());
        }

        @Override public void undoAction(Move move, Game game) {
            findPlayer((move.getPlayer()), game).blocks.add(move.getCorp());
        }
    }

    static class SaleAction extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) {
            Player p = findPlayer(move.getPlayer(), game);
            if(!p.blocks.contains(move.getCorp())) {
                game.addSub(BLOCK_SALE, move.getPlayer(), move.getCorp(), 0, "");
            }
            int drop = Integer.parseInt(move.getDetail());
            if (drop > 0) game.addSub(PRICE_DOWN, "", move.getCorp(), drop, "");
        }

        @Override public void doAction(Move move, Game game) {
            Player p = findPlayer(move.getPlayer(), game);
            Corp c = findCorp(move.getCorp(), game);
            subtractSharesFromPlayer(p, c.name, move.getAmount());
            c.poolShares += move.getAmount();
            int total = c.price.getPrice() * move.getAmount();
            game.getBank().payPlayer(move.getPlayer(), total);
            updatePort(game, p);
        }

        @Override public void undoAction(Move move, Game game) {
            Player p = findPlayer(move.getPlayer(), game);
            Corp c = findCorp(move.getCorp(), game);
            c.poolShares -= move.getAmount();
            addSharesToPlayer(p, c.name, move.getAmount());
            int total = c.price.getPrice() * move.getAmount();
            game.getBank().debitPlayer(move.getPlayer(), total);
            updatePort(game, p);
        }
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
            c.incrementallyFunded = game.getBoard().trains.size() > 2;
            c.destinationSatisfied = game.getBoard().trains.size() < 6;
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
            addSharesToPlayer(p, move.getCorp(), 1);
            if (c.incrementallyFunded) {
                if (c.bankShares >= 5 || c.destinationSatisfied) {
                    game.getBank().player2Corp(p, c, move.getAmount());
                } else {
                    game.getBank().player2Escrow(p, c, move.getAmount());
                }
            }
        }

        @Override
        public void undoAction(Move move, Game game) {
            Player p = findPlayer(move.getPlayer(), game);
            Corp c = findCorp(move.getCorp(), game);
            c.bankShares++;
            subtractSharesFromPlayer(p, move.getCorp(), 1);
            if (c.incrementallyFunded) {
                if (c.bankShares > 5) game.getBank().corp2Player(c, p, move.getAmount());
                else game.getBank().escrow2Player(c, p, move.getAmount());
            }
        }
    }

    static class BuyPoolAction extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) {
            makePrezIf(move, game);
        }

        @Override public void doAction(Move move, Game game) {
            Player p = findPlayer(move.getPlayer(), game);
            Corp c = findCorp(move.getCorp(), game);
            c.poolShares--;
            addSharesToPlayer(p, move.getCorp(), 1);
            game.getBank().debitPlayer(move.getPlayer(), move.getAmount());
        }

        @Override public void undoAction(Move move, Game game) {
            Player p = findPlayer(move.getPlayer(), game);
            Corp c = findCorp(move.getCorp(), game);
            c.poolShares++;
            subtractSharesFromPlayer(p, move.getCorp(), 1);
            game.getBank().payPlayer(move.getPlayer(), move.getAmount());
        }
    }

    static class EndStockRoundAction extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) {
            final Board board = game.getBoard(); //for line length only
            for (Player p:board.getPlayers()) {
                List<String> blocks = new ArrayList<>(p.blocks);
                for(String block: blocks) {
                    game.addSub(CLEAR_BLOCK, p.name, block, 0, "");
                }
            }
            List<Corp> risers = new ArrayList<>();
            for(Corp c: game.getBoard().corps) {
                if (c.bankShares<1 && c.poolShares<1 && c.par>0) { //sold out
                    if (c.price.getY() > 0) risers.add(c);
                }
            }
            boolean maxEnd = false;
            for(Corp c:risers) {
                game.addSub(PRICE_UP, "", c.name, 1, "");
                if (c.price.isMax()) maxEnd = true;
            }
            if (maxEnd) {
                game.addSub(GAME_OVER, "", "", 0, Game.Era.STOCK.name());
            }
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
            game.getBoard().generation++;
        }

        @Override
        public void undoAction(Move move, Game game) {
            game.getBoard().phase = Game.Era.OP.name();
            game.getBoard().activity = move.getDetail();
            game.getBoard().currentPlayer = move.getPlayer();
            game.getBoard().generation--;
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
        Player p = findPlayer(playerName, game);

        int cost = 0;
        Corp corp = null;
        if (turn.buyType != null) {
            corp = findCorp(turn.buyCorp, game);
            if(p.blocks.contains(turn.buyCorp)) throw new IllegalStateException("Cannot buy same Corp after sale");
            switch(turn.buyType) {
                case "par" -> checkPar(corp, turn.buyPar);
                case "bank" -> checkBank(corp, p);
                case "pool" -> checkPool(corp, p);
            }
            cost = calculateCost(corp, turn.buyType, turn.buyPar);
        }
        int shareCount = 0;
        for (Stock s:turn.salesList) {
            Stock h = getHolding(s.corpName, p);
            if(h == null || h.amount < s.amount) throw new IllegalStateException("Cannot sell more shares than you have of "+s.corpName);
            Corp cc = findCorp(s.corpName, game);
            if(cc.par <= 0) throw new IllegalStateException("Cannot sell parless shares of "+s.corpName);
            if(cc.poolShares + s.amount> 5) throw new IllegalStateException("Max 50% in pool: "+s.corpName);
            if(h.isPrez && h.amount - s.amount < 2) { // if the sale dips into a prez share
                boolean recipientFound = false;
                for (Player pp : game.getBoard().getPlayers()) {
                    for (Stock ss : pp.shares) {
                        if (pp.name.equals(p.name)) continue; //looking for other players...
                        if (!ss.corpName.equals(s.corpName)) continue; //with the same stock...
                        if (ss.amount >= 2) {
                            recipientFound = true;
                            break;
                        }
                    }
                }
                if (!cc.price.willClose(previewDrop(s, game))) {
                    if (!recipientFound) throw new IllegalStateException("No one to transfer presidency to for " + s.corpName);
                }
            }
        }
        if (!turn.buyFirst) cost -= calculateSalesValue(turn.salesList, game);
        if (cost > 0) assertPlayerFunds(game, playerName, cost, "stockBuy");
        checkPortfolioLimit(p, turn, game);

        game.addMove(STOCK_TURN, playerName, "", 0, "");
        if (turn.buyFirst) makeBuySubs(game, playerName, turn, corp);
        for(Stock s:turn.salesList) makeSaleSub(game, playerName, s);
        if (!turn.buyFirst) makeBuySubs(game, playerName, turn, corp);
        game.addSub(END_STOCK_TURN, playerName, "", 0, "");
        makePriorityAdvance(game);
        return game.getBoard();
    }

    private static void checkPortfolioLimit(Player p, StockTurn turn, Game game) {
        if(turn.buyType == null || turn.buyType.isEmpty()) return;
        if(p.port < game.portfolioLimit()) return;
        if(!turn.buyFirst) {
            for(Stock s: turn.salesList) {
                if (s.amount == 0) return;
                Corp c = findCorp(s.corpName, game);
                if (c.price.getPrice() > YELLOW_ZONE) return;
            }
        }
        throw new IllegalStateException("Maximum Portfolio size is "+game.portfolioLimit());
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
        game.addSub(STOCK_SALE, playerName, sale.corpName, sale.amount, ""+previewDrop(sale, game));
    }

    private static int previewDrop(Stock sale, Game game) {
        return findCorp(sale.corpName, game).price.previewDrop(sale.amount);
    }

    private static void checkSixty(Corp c, Player p) {
        if(c.price.getPrice() > BROWN_ZONE) {
            Stock s = getHolding(c.name, p);
            if (s != null && s.amount >= 6) {
                throw new IllegalStateException("Max 60% per player unless in brown zone");
            }
        }
    }

    private static void checkPool(Corp corp, Player p) {
        if(corp.poolShares < 1) throw new IllegalStateException("No pool share available for "+corp.name);
        checkSixty(corp, p);
    }

    private static void checkBank(Corp corp, Player p) {
        if(corp.bankShares < 1) throw new IllegalStateException("No bank share available for "+corp.name);
        checkSixty(corp, p);
    }

    private static void addSharesToPlayer(Player p, String corpName, int amount) {
        for(Stock s:p.shares) if(s.corpName.equals(corpName)) { s.amount += amount; return; }
        p.shares.add(new Stock(corpName, amount, false));
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

    private static void subtractSharesFromPlayer(Player p, String corpName, int amount) {
        Stock nuke = null;
        for(Stock s:p.shares) if(s.corpName.equals(corpName)) {
            s.amount -= amount;
            if (s.amount == 0) nuke = s;
            if (s.amount < 0) { // SHOULD NEVER HAPPEN
                throw new IllegalStateException("Trying to remove unfound shares of " + corpName + " from " + p.name);
            }
        }
        if (nuke != null) p.shares.remove(nuke);
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

    private static int calculateSalesValue(List<Stock> sales, Game game) {
        int value = 0;
        for(Stock s: sales) value += findCorp(s.corpName, game).price.getPrice();
        return value;
    }
}
