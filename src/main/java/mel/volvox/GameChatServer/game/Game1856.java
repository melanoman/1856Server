package mel.volvox.GameChatServer.game;

import lombok.Getter;
import lombok.Setter;
import mel.volvox.GameChatServer.comm.train.*;
import mel.volvox.GameChatServer.model.train.TrainMove;
import mel.volvox.GameChatServer.model.train.TrainMoveID;
import mel.volvox.GameChatServer.repository.TrainRepo;
import mel.volvox.GameChatServer.service.DiceService;

import java.util.*;

public class Game1856 extends AbstractGame {
    @Setter TrainRepo repo; // set by controller
    @Getter private final Board1856 board = new Board1856();
    public static final int[] START_CASH = { 0, 0, 0, 500, 375, 300, 250 };
    public static final String NONE = "";

    // error constants
    public static final String FUNDS = "Insufficient Funds";

    // action constants --- these make the database table human readable
    public static final String ADD_PLAYER = "addPlayer";
    public static final String RENAME_PLAYER = "renamePlayer";
    public static final String START_GAME = "startGame";

    public static final String AUCTION_BID = "auctionBid";
    public static final String AUCTION_REBID = "auctionRebid";
    public static final String AUCTION_BUY = "auctionBuy";
    public static final String AUCTION_DISCOUNT_RESET = "discountReset";
    public static final String AUCTION_PASS = "auctionPass";
    public static final String AUCTION_END_ROUND = "auctionEndRound";
    public static final String AUCTION_GIVEAWAY = "auctionFree";
    public static final String AUCTION_LONEBUY = "auctionLoneBuy";
    public static final String AUCTION_AWARD_BID = "awardBid";
    public static final String AUCTION_REFUND_BID = "refundBid";
    public static final String AUCTION_START_BIDOFF = "startBidoff";
    public static final String AUCTION_END_BIDOFF = "endBidoff";
    public static final String AUCTION_END_PHASE = "endAuctionPhase";

    public static final String STOCK_PASS = "stockPass";
    public static final String STOCK_SET_PAR = "stockSetPar";
    public static final String STOCK_BUY_BANK = "stockBuyBank";
    public static final String STOCK_BUY_POOL = "stockBuyPool";
    public static final String STOCK_HEADER = "stockHeader";
    public static final String STOCK_SALE = "stockSale";
    public static final String END_STOCK_ACTION = "stockFooter";
    public static final String REORDER_CORP = "reorderCorp";
    public static final String STOCK_END_ROUND = "stockEndRound";

    public static final String DROP_STOCK_PRICE = "dropStockPrice";
    public static final String PRICE_DOWN = "priceDown";
    public static final String PRICE_UP = "priceUp";
    public static final String PRICE_RIGHT = "priceRight";
    public static final String PRICE_LEFT = "priceLeft";
    public static final String UPDATE_PREZ = "updatePrez";

    public static final String USE_WS = "useWS";
    public static final String USE_CAN = "useCAN";
    public static final String USE_GLS = "useGLS";
    public static final String BUY_BRIDGE = "buyBridge";
    public static final String BUY_TUNNEL = "buyTunnel";

    public static final String TAKE_LOAN = "takeLoan";
    public static final String BUY_PRIV = "buyPriv";
    public static final String FLOAT = "float";
    public static final String FAIL_FLOAT = "failFloat";
    public static final String PAY_TOKEN = "payToken";
    public static final String PAY_TILE = "payTile";
    public static final String DESTINATION = "destination";
    public static final String RUN = "run";
    public static final String INTEREST = "payInterest";
    public static final String PREZ_INTEREST = "prezInterest";
    public static final String PAYOUT = "payout";
    public static final String WITHHOLD = "withhold";
    public static final String BUY_BANK_TRAIN = "buyBankTrain";
    public static final String RUST = "rust";
    public static final String RUST_TRAIN = "rustTrain";
    public static final String CLOSE_PRIVS = "closePrivs";
    public static final String REMOVE_PRIV = "removePriv";
    public static final String END_OP_TURN = "endOpTurn";
    public static final String NEXT_CORP = "nextCorp";
    public static final String END_OP_ROUND = "endOpRound";
    public static final String CLEAR_BLOCKS = "clearBlocks";

    // event constants
    // TODO move error message substrings out of constants to insulate clients
    public static final String NORMAL_EVENT = "in normal turn";
    public static final String BIDOFF_EVENT = "resolving conflicting bids";
    public static final String PRE_REV_EVENT = "before revenue";
    public static final String POST_REV_EVENT = "done with revenue";
    public static final String FORCED_INTEREST_SALE = "InterestSale";

    // private companies
    public static final String PRIVATE_FLOS = "flos";
    public static final String PRIVATE_WS = "ws";
    public static final String PRIVATE_CAN = "can";
    public static final String PRIVATE_GLS = "gls";
    public static final String PRIVATE_NIAG = "niag";
    public static final String PRIVATE_STC = "stc";

    public static final Map<String, Integer> priv2price = Map.of(
            PRIVATE_FLOS, 20,
            PRIVATE_WS,   40,
            PRIVATE_CAN,  50,
            PRIVATE_GLS,  70,
            PRIVATE_NIAG,100,
            PRIVATE_STC, 100
    );

    public static final Map<String, String> priv2next = Map.of(
            PRIVATE_FLOS, PRIVATE_WS,
            PRIVATE_WS, PRIVATE_CAN,
            PRIVATE_CAN, PRIVATE_GLS,
            PRIVATE_GLS, PRIVATE_NIAG,
            PRIVATE_NIAG, PRIVATE_STC,
            PRIVATE_STC, NONE
    );

    public static final Map<String, Integer> priv2div = Map.of(
            PRIVATE_FLOS, 5,
            PRIVATE_WS, 10,
            PRIVATE_CAN, 10,
            PRIVATE_GLS, 15,
            PRIVATE_NIAG, 20,
            PRIVATE_STC, 20
    );

    private List<TrainMove> history = new ArrayList<>();

    private void checkSalesList(List<StockSale> sales) {
        if (phaseIs(Era.INITIAL)) throw new IllegalStateException("No sales in 1st stock round");
        Wallet w = getCurrentWallet();
        for(StockSale sale: sales) {
            boolean legal = false;
            Corp c = findCorp(sale.getName());
            for (Stock stock: w.getStocks()) {
                if (stock.getCorp().equals(sale.getName())) {
                    if(stock.getAmount() < sale.getAmount()) break;
                    if(sale.getAmount()+c.getPoolShares() > 5) {
                        throw new IllegalStateException("Pool shares may not exceed 50%");
                    }
                    if(stock.isPresident() && stock.getAmount() - sale.getAmount() < 2) {
                        boolean noPrez = true;
                        for (Wallet w2: board.getWallets()) {
                            if(w2 == w) continue;
                            for (Stock s: w2.getStocks()) {
                                if (s.getCorp().equals(c.getName())) {
                                    if(s.getAmount() > 1) noPrez = false;
                                }
                            }
                        }
                        if(noPrez) throw new IllegalStateException("No one to become president of "+sale.getName());
                    }
                    legal = true;
                }
            }
            if (!legal) throw new IllegalStateException("Not enough shares of "+sale.getName());
        }
    }

    public Board1856 makeSales(List<StockSale> stockSales) {
        checkSalesList(stockSales);
        makePrimaryMove(STOCK_HEADER, board.getCurrentPlayer(), "", 0);
        for(StockSale sale: stockSales) {
            makeFollowMove(STOCK_SALE, board.getCurrentPlayer(), sale.getName(), sale.getAmount());
        }
        makeFollowMove(END_STOCK_ACTION, board.getCurrentPlayer(), "", 0);
        return board;
    }

    public Board1856 takeLoan() {
        if (board.isLoanTaken()) throw new IllegalStateException("One loan per turn");
        Corp corp = findCorp(board.getCurrentCorp());
        if(corp.getLoans() >= 10 - corp.getBankShares() - corp.getPoolShares()) {
            throw new IllegalStateException("Too many loans for Corp");
        }
        if (board.getTrains().size() < 2) throw new IllegalStateException("Too late for loans");
        int amount = board.getEvent().equals(PRE_REV_EVENT) ? 100 : 90;
        makePrimaryMove(TAKE_LOAN, "", corp.getName(), amount);
        return board;
    }

    private void doTakeLoan(TrainMove move) {
        Corp corp = findCorp(move.getCorp());
        corp.setLoans(corp.getLoans() + 1);
        payBankToCorp(corp, move.getAmount());
        board.setLoanTaken(true);
    }

    private void undoTakeLoan(TrainMove move) {
        Corp corp = findCorp(move.getCorp());
        corp.setLoans(corp.getLoans() - 1);
        payCorpToBank(corp, move.getAmount());
        board.setLoanTaken(false);
    }

    private Corp getCurrentCorp() {
        for(Corp c: board.getCorps()) {
            if(c.getName().equals(board.getCurrentCorp())) return c;
        }
        throw new IllegalStateException("No Corporation is operating");
    }

    private Wallet findPrivOwner(String privName) {
        for(Wallet w:board.getWallets()) {
            for(Priv p:w.getPrivates()) {
                if(p.getCorp().equals(privName)) return w;
            }
        }
        return null;
    }

    /**
     * this is for corp buying private from player
     */
    public Board1856 buyPriv(String privName, int price) {
        int level = highestTrainSold();
        if (level < 3) throw new IllegalStateException("No Company sales before first 3-train");
        if (level > 5) throw new IllegalStateException("Private companies are closed");
        int basePrice = priv2price.get(privName);
        if(price*2 < basePrice) throw new IllegalStateException("Minimum price is "+(basePrice/2));
        if(basePrice*2 < price) throw new IllegalStateException("Maximum price is "+(basePrice*2));
        if (getCurrentCorp().getCash() < price) throw new IllegalStateException(FUNDS);
        Wallet w = findPrivOwner(privName);
        if(w == null) throw new IllegalStateException(privName+" already sold");
        makePrimaryMove(BUY_PRIV, w.getName(), privName, price);
        return board;
    }

    public enum Era { GATHER, AUCTION, INITIAL, STOCK, OP, CGRFORM, DONE }

    synchronized public void loadMoves(List<TrainMove> moves) {
        history = moves;
        for(TrainMove move: moves) {
            loadMove(move);
        }
    }

    public void loadMove(TrainMove move) {
        doMove(move, false);
        board.setMoveNumber(board.getMoveNumber()+1);
    }

    final private static String SHUFFLE_STOCK = "123456";

    static String makeShuffle(boolean shuffle, int size) {
        StringBuilder buf = new StringBuilder(SHUFFLE_STOCK.substring(0, size));
        if(shuffle) {
            for (int i = size; i > 0; i--) {
                int j = DiceService.Roll(i);
                char tmp = buf.charAt(i - 1);
                buf.setCharAt(i - 1, buf.charAt(j - 1));
                buf.setCharAt(j - 1, tmp);
            }
        }
        return buf.toString();
    }

    private void undoStart(String order) {
        List<String> newPlayers = new ArrayList<>();
        for(char c='1'; c-'1'<order.length(); c++) {
            newPlayers.add(board.getPlayers().get(order.indexOf(c)));
        }
        board.setPlayers(newPlayers);
        board.setPhase(Era.GATHER.name());
        board.setWallets(new ArrayList<>());
        board.setTrains(new ArrayList<>());
    }

    List<Integer> TRAIN_START = List.of(2,2,2,2,2,2, 3,3,3,3,3, 4,4,4,4, 5,5,5, 6,6);

    private void doStart(String order) {
        List<String> newPlayers = new ArrayList<>();
        for(int i=0; i<order.length(); i++) {
            newPlayers.add(board.getPlayers().get(order.charAt(i)-'1'));
        }
        board.setPlayers(newPlayers);
        board.setPhase(Era.AUCTION.name());
        board.setCurrentPlayer(newPlayers.get(0));
        board.setPriorityHolder(newPlayers.get(0));
        board.setPassCount(0);
        board.setCurrentCorp(PRIVATE_FLOS);
        board.setEvent(NORMAL_EVENT);
        for (String player: board.getPlayers()) {
            Wallet wallet= new Wallet();
            wallet.setName(player);
            wallet.setCash(START_CASH[board.getPlayers().size()]);
            board.getWallets().add(wallet);
        }
        board.setBankCash(10500); //12k minus 1500 for total player starting cash
        board.getTrains().addAll(TRAIN_START);
    }

    private void payBankToWallet(Wallet w, int amount) {
        w.setCash(w.getCash() + amount);
        board.setBankCash(board.getBankCash() - amount);
    }

    private void payWalletToBank(Wallet w, int amount) {
        w.setCash(w.getCash() - amount);
        board.setBankCash(board.getBankCash() + amount);
    }

    private void payWalletToCorp(Wallet w, Corp c, int amount) {
        w.setCash(w.getCash() - amount);
        c.setCash(c.getCash() + amount);
    }

    private void payCorpToWallet(Wallet w, Corp c, int amount) {
        w.setCash(w.getCash() + amount);
        c.setCash(c.getCash() - amount);
    }

    private void doAuctionPass(boolean rawMove) {
        incrementAuctionPlayer(false, rawMove);
    }

    private void doAuctionBid(TrainMove move, boolean rawMove) {
        Wallet w = getCurrentWallet();
        w.getPrivates().add(new Priv(move.getCorp(), move.getAmount()));
        payWalletToBank(w, move.getAmount());
        incrementAuctionPlayer(true, rawMove);
    }

    private void undoAuctionBid(TrainMove move) {
        board.setCurrentPlayer(move.getPlayer());
        Wallet w = getCurrentWallet();
        w.getPrivates().removeIf(priv -> move.getCorp().equals(priv.getCorp()));
        payBankToWallet(w, move.getAmount());
    }

    private void doStartBidoff(TrainMove move) {
        board.setEvent(BIDOFF_EVENT);
        board.setCurrentCorp(move.getCorp());
    }

    private void undoStartBidoff(TrainMove move) {
        board.setCurrentCorp(move.getPlayer());
        board.setEvent(NORMAL_EVENT);
    }

    private void payPrivs() {
        for(Wallet w: board.getWallets()) {
            for (Priv priv: w.getPrivates()) {
                if(priv.getAmount() > 3) continue;
                payBankToWallet(w, priv2div.get(priv.getCorp()));
            }
        }
        for(Corp c:board.getCorps()) {
            for (Priv p: c.getPrivates()) {
                payBankToCorp(c, priv2div.get(p.getCorp()));
            }
        }
    }

    private void doAuctionEndRound(TrainMove move, boolean rawMove) {
        payPrivs();
        if (board.getCurrentCorp().equals(PRIVATE_FLOS)) {
            board.setAuctionDiscount(board.getAuctionDiscount() + 5);
        }
        board.setPassCount(0);
        if(rawMove && priv2price.get(board.getCurrentCorp()) == board.getAuctionDiscount()) {
            makeFollowMove(AUCTION_GIVEAWAY, board.getCurrentPlayer(), board.getCurrentCorp(), 0);
        }
    }

    private void unpayPrivs() {
        for(Wallet w: board.getWallets()) {
            for (Priv priv: w.getPrivates()) {
                if(priv.getAmount() > 3) continue;
                payWalletToBank(w, priv2div.get(priv.getCorp()));
            }
        }
        for(Corp c:board.getCorps()) {
            for (Priv p: c.getPrivates()) {
                payCorpToBank(c, priv2div.get(p.getCorp()));
            }
        }
    }

    private void undoAuctionEndRound(TrainMove move) {
        if (board.getCurrentCorp().equals(PRIVATE_FLOS)) {
            board.setAuctionDiscount(board.getAuctionDiscount() - 5);
        }
    }

    private void doMove(TrainMove move, boolean rawMove) {
        switch (move.getAction()) {
            case ADD_PLAYER-> board.getPlayers().add(move.getPlayer());
            case RENAME_PLAYER -> board.getPlayers().set(move.getAmount(), move.getPlayer());
            case START_GAME -> doStart(move.getCorp());
            case AUCTION_BUY -> doAuctionBuy(move, rawMove);
            case AUCTION_PASS -> doAuctionPass(rawMove);
            case AUCTION_BID -> doAuctionBid(move, rawMove);
            case AUCTION_REBID -> doAuctionRebid(move, rawMove);
            case AUCTION_LONEBUY -> doLoneBuy(move, rawMove);
            case AUCTION_START_BIDOFF -> doStartBidoff(move);
            case AUCTION_END_BIDOFF -> doEndBidoff(move, rawMove);
            case AUCTION_END_ROUND -> doAuctionEndRound(move, rawMove);
            case AUCTION_AWARD_BID -> doAwardBid(move);
            case AUCTION_REFUND_BID -> doRefundBid(move);
            case AUCTION_GIVEAWAY -> doAuctionGiveaway(move, rawMove);
            case AUCTION_END_PHASE -> doEndAuctionPhase(move);
            case AUCTION_DISCOUNT_RESET -> doAuctionDiscountReset();
            case STOCK_PASS -> doStockPass(move, rawMove);
            case STOCK_END_ROUND -> doEndStockRound(move, rawMove);
            case STOCK_SET_PAR -> doSetPar(move);
            case STOCK_BUY_BANK -> doBuyBank(move, rawMove);
            case STOCK_BUY_POOL -> doBuyPool(move, rawMove);
            case UPDATE_PREZ -> doUpdatePrez(move);
            case STOCK_SALE -> doStockSale(move, rawMove);
            case STOCK_HEADER -> { } //nothing to do -- this just anchors the sale list
            case DROP_STOCK_PRICE -> doDropStockPrice(move, rawMove);
            case PRICE_DOWN, PRICE_UP, PRICE_LEFT, PRICE_RIGHT -> doStockStep(move, rawMove);
            case REORDER_CORP -> doReorderCorp(move);
            case END_STOCK_ACTION -> doEndStockAction(true, rawMove);
            case TAKE_LOAN -> doTakeLoan(move);
            case USE_WS -> doUseWS(move);
            case USE_CAN -> doUseCAN(move);
            case USE_GLS -> doUseGLS(move);
            case BUY_BRIDGE -> doBuyBridge(move);
            case BUY_TUNNEL -> doBuyTunnel(move);
            case BUY_PRIV -> doBuyPriv(move);
            case FLOAT -> doFloat(move);
            case FAIL_FLOAT -> doFailFloat(move);
            case PAY_TOKEN -> doPayToken(move);
            case PAY_TILE -> doPayTile(move);
            case DESTINATION -> doDestination(move);
            case RUN -> doLastRun(move);
            case INTEREST -> doInterest(move);
            case PREZ_INTEREST -> doPrezInterest(move);
            case PAYOUT -> doPayout(move);
            case WITHHOLD -> doWithhold(move);
            case BUY_BANK_TRAIN -> doBankTrain(move, rawMove);
            case RUST -> doRust(move, rawMove);
            case RUST_TRAIN -> doRustTrain(move);
            case CLOSE_PRIVS -> doClosePriv(move, rawMove);
            case REMOVE_PRIV -> doRemovePriv(move);
            case END_OP_TURN -> doEndOpTurn(move, rawMove);
            case NEXT_CORP -> doNextCorp(move);
            case END_OP_ROUND -> doEndOpRound(move);
            case CLEAR_BLOCKS -> doClearBlocks(move);
            default -> throw new IllegalStateException("unknown move action: "+move.getAction());
        }
    }

    /**
     * ClosePriv and RemovePriv work as a pair.
     * ClosePriv records where the privs has been by creating Remove follows, then wipes the lists.
     * Remove Priv does nothing on do, but then has all then info on undo.
     * This avoids mutating the list while ClosePriv is still iterating on do
     */
    private void doClosePriv(TrainMove move, boolean rawMove) {
        for(Wallet w: board.getWallets()) {
            if (rawMove) for(Priv p: w.getPrivates()) {
                makeFollowMove(REMOVE_PRIV, w.getName(), p.getCorp(), p.getAmount()+1);
                if(p.getCorp().equals(PRIVATE_NIAG)) board.setBridgeTokens(3);
                if(p.getCorp().equals(PRIVATE_STC)) board.setTunnelTokens(3);
            }
            w.setPrivates(new ArrayList<>());
        }
        for(Corp c: board.getCorps()) {
            if (rawMove) for(Priv p: c.getPrivates()) {
                makeFollowMove(REMOVE_PRIV, c.getName(), p.getCorp(), -p.getAmount()-1);
            }
            c.setPrivates(new ArrayList<>());
        }
    }

    private void undoClosePriv(TrainMove move) {
        // nothing to do here
    }

    private void doRemovePriv(TrainMove move) {
        // nothing to do here
    }

    /**
     * CORP ==> name of priv
     * AMOUNT ==> positiv from player, negative from corp (abs(x)-1 ==> priv.amount)
     * PLAYER ==> name of player or corp losing the priv
     */
    private void undoRemovePriv(TrainMove move) {
        if(move.getAmount() > 0) { //player
            Wallet w = findWallet(move.getPlayer());
            w.getPrivates().add(0, new Priv(move.getCorp(), move.getAmount()-1));
            if(move.getCorp().equals(PRIVATE_NIAG)) board.setBridgeTokens(0);
            if(move.getCorp().equals(PRIVATE_STC)) board.setTunnelTokens(0);
        } else { // corp
            Corp c = findCorp(move.getPlayer());
            c.getPrivates().add(0, new Priv(move.getCorp(), -move.getAmount()-1));
        }
    }

    private void doRust(TrainMove move, boolean rawMove) {
        for(Corp corp: board.getCorps()) {
            int count = 0;
            for(Integer train: corp.getTrains()) {
                if(train == move.getAmount()) { // only remove the
                    count++;
                }
            }
            for(int i=0; i<count; i++) {
                makeFollowMove(RUST_TRAIN, "", corp.getName(), move.getAmount());
            }
        }
    }

    private void undoRust(TrainMove move) {
        //nothing to do
    }

    private void doRustTrain(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        c.getTrains().removeIf(x -> x == move.getAmount());
    }

    private void undoRustTrain(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        c.getTrains().add(0, move.getAmount());
    }

    private void doUseWS(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        if (move.getAmount() > 0) { // lay token for free
            c.setTokensUsed(c.getTokensUsed() + 1);
        }
        c.getPrivates().removeIf(x -> x.getCorp().equals(PRIVATE_WS));
    }

    private void doUseCAN(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        for (Priv p: c.getPrivates()) {
            if(p.getCorp().equals(PRIVATE_CAN)) {
                p.setAmount(0);
            }
        }
    }

    private void doUseGLS(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        c.getPrivates().removeIf(x -> x.getCorp().equals(PRIVATE_GLS));
        c.setPortRights(true);
    }

    private void undoUseWS(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        if (move.getAmount() > 0) { // lay token for free
            c.setTokensUsed(c.getTokensUsed() - 1);
        }
        c.getPrivates().add(new Priv(PRIVATE_WS, 3));
    }

    private void undoUseCAN(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        for (Priv p: c.getPrivates()) {
            if(p.getCorp().equals(PRIVATE_CAN)) {
                p.setAmount(3);
            }
        }
    }

    private void undoUseGLS(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        c.getPrivates().add(new Priv(PRIVATE_GLS, 3));
        c.setPortRights(false);
    }

    private void doBuyBridge(TrainMove move) {
        Corp buyer = findCorp(move.getCorp());
        Corp seller = findPrivCorp(PRIVATE_NIAG);
        if (seller == null) payCorpToBank(buyer, 50);
        else payCorpToCorp(buyer, seller, 50);
        buyer.setBridgeRights(true);
        board.setBridgeTokens(board.getBridgeTokens() - 1);
    }

    private void doBuyTunnel(TrainMove move) {
        Corp buyer = findCorp(move.getCorp());
        Corp seller = findPrivCorp(PRIVATE_STC);
        if (seller == null) payCorpToBank(buyer, 50);
        else payCorpToCorp(buyer, seller, 50);
        buyer.setTunnelRights(true);
        board.setTunnelTokens(board.getTunnelTokens() - 1);
    }

    private void undoBuyBridge(TrainMove move) {
        Corp buyer = findCorp(move.getCorp());
        Corp seller = findPrivCorp(PRIVATE_NIAG);
        if (seller == null) payBankToCorp(buyer, 50);
        else payCorpToCorp(seller, buyer, 50);
        buyer.setBridgeRights(false);
        board.setBridgeTokens(board.getBridgeTokens() + 1);
    }

    private void undoBuyTunnel(TrainMove move) {
        Corp buyer = findCorp(move.getCorp());
        Corp seller = findPrivCorp(PRIVATE_STC);
        if (seller == null) payBankToCorp(buyer, 50);
        else payCorpToCorp(seller, buyer, 50);
        buyer.setTunnelRights(false);
        board.setTunnelTokens(board.getTunnelTokens() + 1);
    }

    private Corp findPrivCorp(String privName) {
        for (Corp c: board.getCorps()) {
            for (Priv p: c.getPrivates()) {
                if (p.getCorp().equals(privName)) return c;
            }
        }
        return null;
    }

    private void payCorpToCorp(Corp from, Corp to, int amount) {
        from.setCash(from.getCash() - amount);
        to.setCash(to.getCash() + amount);
    }

    private void doInterest(TrainMove move) {
        payCorpToBank(findCorp(move.getCorp()), move.getAmount());
    }

    private void undoInterest(TrainMove move) {
        payBankToCorp(findCorp(move.getCorp()), move.getAmount());
    }

    private void doPayout(TrainMove move) {
        board.setEvent(POST_REV_EVENT);
        board.setTilePlayed(false);
        board.setTokenPlayed(false);
        Corp c = findCorp(move.getCorp());
        payBankToCorp(c, c.getPoolShares() * move.getAmount());
        for(Wallet w: board.getWallets()) {
            for(Stock stock: w.getStocks()) {
                if (stock.getCorp().equals(move.getCorp())) { //TODO special CGR payouts
                    payBankToWallet(w, move.getAmount() * stock.getAmount());
                }
            }
        }
        //TODO special CGR movements
    }

    private void undoPayout(TrainMove move) {
        board.setEvent(PRE_REV_EVENT);
        board.setTilePlayed(move.getPlayer().charAt(0) == 'T');
        board.setTokenPlayed(move.getPlayer().charAt(1) == 't');
        Corp c = findCorp(move.getCorp());
        payCorpToBank(c, c.getPoolShares() * move.getAmount());
        for(Wallet w: board.getWallets()) {
            for(Stock stock: w.getStocks()) {
                if (stock.getCorp().equals(move.getCorp())) { //TODO special CGR payouts
                    payWalletToBank(w, move.getAmount() * stock.getAmount());
                }
            }
        }
    }

    private void doWithhold(TrainMove move) {
        board.setEvent(POST_REV_EVENT);
        board.setTilePlayed(false);
        board.setTokenPlayed(false);
        Corp c = findCorp(move.getCorp());
        payBankToCorp(c, move.getAmount());
    }

    private void undoWithhold(TrainMove move) {
        board.setEvent(PRE_REV_EVENT);
        board.setTilePlayed(move.getPlayer().charAt(0) == 'T');
        board.setTokenPlayed(move.getPlayer().charAt(1) == 't');
        Corp c = findCorp(move.getCorp());
        payCorpToBank(c, move.getAmount());
    }

    private void doFloat(TrainMove move) {
        Corp corp = findCorp(move.getCorp());
        corp.setHasFloated(true);
        corp.setTokensUsed(1);
        board.setCurrentCorp(corp.getName());
        board.setLoanTaken(false);
    }

    private void undoFloat(TrainMove move) {
        Corp corp = findCorp(move.getCorp());
        corp.setHasFloated(false);
        corp.setTokensUsed(0);
        board.setCurrentCorp(move.getPlayer());
        board.setLoanTaken(move.getAmount() > 0);
    }

    private void doFailFloat(TrainMove move) {
        Corp corp = findCorp(move.getCorp());
        corp.setHasOperated(true);
    }

    private void undoFailFloat(TrainMove move) {
        Corp corp = findCorp(move.getCorp());
        corp.setHasOperated(false);
    }

    private void doEndOpTurn(TrainMove move, boolean rawMove) {
        board.setEvent(PRE_REV_EVENT);
        findCorp(move.getCorp()).setHasOperated(true);
        if (rawMove) setNextOpCorp();
    }

    private void undoEndOpTurn(TrainMove move) {
        board.setEvent(POST_REV_EVENT);
        findCorp(move.getCorp()).setHasOperated(false);
    }

    private void doNextCorp(TrainMove move) {
        board.setLoanTaken(false);
        board.setCurrentCorp(move.getCorp());
    }

    private void undoNextCorp(TrainMove move) {
        board.setLoanTaken(move.getAmount() > 0);
        board.setCurrentCorp(move.getPlayer());
    }

    private void doEndOpRound(TrainMove move) {
        for(Corp c: board.getCorps()) {
            c.setHasOperated(false);
        }
        board.setLoanTaken(false);
        board.setPassCount(0);
        if(board.getCurrentOpRound() == board.getMaxOpRounds()) {
            board.setPhase(Era.STOCK.name());
            board.setCurrentCorp("");
        } else {
            board.setCurrentOpRound(board.getCurrentOpRound() + 1);
            board.setCurrentCorp(board.getCorps().get(0).getName());
            payPrivs();
        }
    }

    private void undoEndOpRound(TrainMove move) {
        for(Corp c: board.getCorps()) {
            c.setHasOperated(true);
        }
        board.setCurrentCorp(move.getCorp());
        board.setLoanTaken(move.getAmount() > 0);
        getCurrentCorp().setHasOperated(false);
        if (board.getPhase().equals(Era.OP.name())) {
            board.setCurrentOpRound(board.getCurrentOpRound() - 1);
            unpayPrivs();
        } else {
            board.setCurrentOpRound(move.getAmount() > 0 ? move.getAmount() : -move.getAmount());
            board.setMaxOpRounds(board.getCurrentOpRound());
            board.setPhase(Era.OP.name());
        }
    }

    private void doBuyPriv(TrainMove move) {
        Corp c = getCurrentCorp();
        Wallet w = findWallet(move.getPlayer());
        payCorpToWallet(w, c, move.getAmount());
        w.getPrivates().removeIf(x -> x.getCorp().equals(move.getCorp()));
        c.getPrivates().add(new Priv(move.getCorp(), 3));
        switch(move.getCorp()) {
            case PRIVATE_STC -> { c.setTunnelRights(true); board.setTunnelTokens(3); }
            case PRIVATE_NIAG -> { c.setBridgeRights(true); board.setBridgeTokens(3); }
        }
    }

    private void undoBuyPriv(TrainMove move) {
        Corp c = getCurrentCorp();
        Wallet w = findWallet(move.getPlayer());
        payWalletToCorp(w, c, move.getAmount());
        c.getPrivates().removeIf(x -> x.getCorp().equals(move.getCorp()));
        w.getPrivates().add(new Priv(move.getCorp(), 3));
        switch(move.getCorp()) {
            case PRIVATE_STC -> { c.setTunnelRights(false); board.setTunnelTokens(0);}
            case PRIVATE_NIAG -> { c.setBridgeRights(false); board.setBridgeTokens(0);}
        }
    }

    //TODO if bridge/tunnel rust in player hands, put 3 tokens in bank

    private void doReorderCorp(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        board.getCorps().remove(c);
        board.getCorps().add(newStockIndex(c), c);
    }

    private void undoReorderCorp(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        board.getCorps().remove(c);
        board.getCorps().add(move.getAmount(), c);
    }

    private void doDropStockPrice(TrainMove move, boolean rawMove) {
        Corp c = findCorp(move.getCorp());
        c.getPrice().drop(move.getAmount());
    }

    private void undoDropStockPrice(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        c.getPrice().drop(-move.getAmount());
    }

   private void doStockStep(TrainMove move, boolean rawMove) {
        Corp c = findCorp(move.getCorp());
        switch (move.getAction()) {
            case PRICE_LEFT -> c.getPrice().left();
            case PRICE_RIGHT -> c.getPrice().right();
            case PRICE_DOWN -> c.getPrice().down();
            case PRICE_UP -> c.getPrice().up();
        }
       if (rawMove) {
           makeFollowMove(REORDER_CORP, "", move.getCorp(), board.getCorps().indexOf(c));
       }
   }

   private void undoStockStep(TrainMove move) {
       Corp c = findCorp(move.getCorp());
       switch (move.getAction()) {
           case PRICE_LEFT -> c.getPrice().right();
           case PRICE_RIGHT -> c.getPrice().left();
           case PRICE_DOWN -> c.getPrice().up();
           case PRICE_UP -> c.getPrice().down();
       }
   }

    private void sharesPoolToWallet(Wallet w, Corp corp, int amount) {
        corp.setPoolShares(corp.getPoolShares() - amount);
        shareToWallet(w, corp.getName(), amount);
    }

    private void sharesWalletToPool(Wallet w, Corp corp, int amount) {
        corp.setPoolShares(corp.getPoolShares() + amount);
        shareToWallet(w, corp.getName(), -amount);
    }

    private void doStockSale(TrainMove move, boolean rawMove) {
        Wallet w = getPlayerWallet(move.getPlayer());
        Corp c = findCorp(move.getCorp());
        sharesWalletToPool(w, c, move.getAmount());
        payBankToWallet(w, move.getAmount() * c.getPrice().getPrice());
        w.getBlocks().add(move.getCorp()); // one per xaction
        if(rawMove) {
            int drop = c.getPrice().previewDrop(move.getAmount());
            if (drop > 0) makeFollowMove(DROP_STOCK_PRICE, "", move.getCorp(), drop);
            updatePrez(move.getCorp());
            makeFollowMove(REORDER_CORP, "", move.getCorp(), board.getCorps().indexOf(c));
        }
    }

    private void undoStockSale(TrainMove move) {
        Wallet w = getPlayerWallet(move.getPlayer());
        Corp c = findCorp(move.getCorp());
        sharesPoolToWallet(w, c, move.getAmount());
        payWalletToBank(w, move.getAmount() * c.getPrice().getPrice());
        w.getBlocks().remove(move.getCorp()); // only remove one!
    }

    private void doUpdatePrez(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        Wallet w = findWallet(c.getPrez());
        for (Stock s: w.getStocks()) {
            if(s.getCorp().equals(c.getName())) s.setPresident(false);
        }
        c.setPrez(move.getPlayer());
        w = findWallet(c.getPrez());
        for(Stock s: w.getStocks()) {
            if(s.getCorp().equals(c.getName())) s.setPresident(true);
        }
    }

    private void undoUpdatePrez(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        Wallet w = findWallet(c.getPrez());
        for (Stock s: w.getStocks()) {
            if(s.getCorp().equals(c.getName())) s.setPresident(false);
        }
        c.setPrez(board.getPlayers().get(move.getAmount()));
        w = findWallet(c.getPrez());
        for(Stock s: w.getStocks()) {
            if(s.getCorp().equals(c.getName())) s.setPresident(true);
        }
    }

    private void doAuctionGiveaway(TrainMove move, boolean rawMove) {
        Wallet w = getPlayerWallet(move.getPlayer());
        w.getPrivates().add(new Priv(move.getCorp(), 3));
        incrementPrivate(rawMove);
        board.setAuctionDiscount(0);
    }

    private void undoAuctionGiveaway(TrainMove move) {
        Wallet w = getPlayerWallet(move.getPlayer());
        w.getPrivates().removeIf(x -> move.getCorp().equals(x.getCorp()));
        board.setCurrentCorp(move.getCorp());
        board.setAuctionDiscount(priv2price.get(move.getCorp()));
    }

    private void doAuctionRebid(TrainMove move, boolean rawMove) {
        Wallet w = getCurrentWallet();
        for (Priv priv: w.getPrivates()) {
            if(priv.getCorp().equals(move.getCorp())) {
                priv.setAmount(priv.getAmount() + move.getAmount());
            }
        }
        payWalletToBank(w, move.getAmount());
        incrementAuctionPlayer(true, rawMove);
    }

    private void undoAuctionRebid(TrainMove move) {
        board.setCurrentPlayer(move.getPlayer());
        Wallet w = getCurrentWallet();
        for (Priv priv: w.getPrivates()) {
            if(priv.getCorp().equals(move.getCorp())) {
                priv.setAmount(priv.getAmount() - move.getAmount());
            }
        }
        payBankToWallet(w, move.getAmount());
    }

    private int countBids(String privName) {
        int out = 0;
        for(Wallet w: board.getWallets()) {
            for(Priv priv: w.getPrivates()) {
                if(privName.equals(priv.getCorp())) {
                    out++;
                    break;
                }
            }
        }
        return out;
    }

    private int highBid(String privName) {
        int out = 0;
        for(Wallet w: board.getWallets()) {
            for(Priv priv: w.getPrivates()) {
                if(privName.equals(priv.getCorp())) {
                    if(priv.getAmount() > out) out = priv.getAmount();
                    break;
                }
            }
        }
        return out;
    }

    private void loneBuy(String privName, boolean rawMove) {
        if (rawMove) {
            for(Wallet w: board.getWallets()) {
                for (Priv priv : w.getPrivates()) {
                    if (privName.equals(priv.getCorp())) {
                        makeFollowMove(AUCTION_LONEBUY, w.getName(), privName, priv.getAmount());
                    }
                }
            }
        }
    }

    private void doLoneBuy(TrainMove move, boolean rawMove) {
        for(Wallet w: board.getWallets()) {
            if(w.getName().equals(move.getPlayer())) {
                for (Priv priv:w.getPrivates()) {
                    if (priv.getCorp().equals(move.getCorp())) {
                        priv.setAmount(3);
                    }
                }
            }
        }
        board.setCurrentCorp(move.getCorp());
        incrementPrivate(rawMove);
    }

    private void undoLoneBuy(TrainMove move) {
        for(Wallet w:board.getWallets()) {
            if(w.getName().equals(move.getPlayer())) {
                for(Priv priv: w.getPrivates()) {
                    if(priv.getCorp().equals(move.getCorp())) {
                        priv.setAmount(move.getAmount());
                    }
                }
            }
        }
    }

    private void incrementPrivate(boolean rawMove) {
        String next = priv2next.get(board.getCurrentCorp());
        int numBids = countBids(next);
        if (numBids == 1) {
            loneBuy(next, rawMove);
        } else if(numBids > 1) {
            if(rawMove) {
                makeFollowMove(AUCTION_START_BIDOFF, board.getCurrentCorp(), next, 0);
            }
        } else if(NONE.equals(next)) {
            if (rawMove) {
                makeFollowMove(AUCTION_END_PHASE, "", "", 0);
            }
        } else {
            board.setCurrentCorp(next);
            board.setEvent(NORMAL_EVENT);
        }
    }

    private void incrementAuctionPlayer(boolean actionTaken, boolean rawMove) {
        int index = board.getPlayers().indexOf(board.getCurrentPlayer()) + 1;
        if (index >= board.getPlayers().size()) index = 0;
        board.setCurrentPlayer(board.getPlayers().get(index));
        if (actionTaken) {
            board.setPriorityHolder(board.getPlayers().get(index));
            board.setPassCount(0);
        } else {
            board.setPassCount(board.getPassCount()+1);
            if(rawMove && board.getPassCount() == board.getPlayers().size()) {
                makeFollowMove(AUCTION_END_ROUND, board.getCurrentPlayer(), "", 0);
            }
        }
    }

    //TODO refactor to extract commonality with incrAuction, stockPass
    private void doEndStockAction(boolean actionTaken, boolean rawMove) {
        int index = board.getPlayers().indexOf(board.getCurrentPlayer()) + 1;
        if (index >= board.getPlayers().size()) index = 0;
        board.setCurrentPlayer(board.getPlayers().get(index));
        if (actionTaken) {
            board.setPriorityHolder(board.getPlayers().get(index));
            board.setPassCount(0);
        } else {
            board.setPassCount(board.getPassCount()+1);
            if(rawMove && board.getPassCount() == board.getPlayers().size()) {
                makeFollowMove(STOCK_END_ROUND, board.getCurrentPlayer(), board.getPhase(), 0);
            }
        }
    }

    private void doAuctionBuy(TrainMove move, boolean rawMove) {
        Wallet wallet = getCurrentWallet();
        payWalletToBank(wallet, move.getAmount());
        if (rawMove && board.getAuctionDiscount() > 0) {
            makeFollowMove(AUCTION_DISCOUNT_RESET, "", "", board.getAuctionDiscount());
        }
        wallet.getPrivates().add(new Priv(move.getCorp(), 3));
        incrementAuctionPlayer(true, rawMove);
        incrementPrivate(rawMove);
    }

    private void undoAuctionBuy(TrainMove move) {
        board.setPhase(Era.AUCTION.name());
        board.setCurrentPlayer(move.getPlayer());
        board.setCurrentCorp(move.getCorp());
        Wallet wallet = getCurrentWallet();
        payBankToWallet(wallet, move.getAmount());
        wallet.getPrivates().removeIf(priv -> priv.getCorp().equals(move.getCorp()));
    }

    private void undoAuctionPass(TrainMove move) {
        board.setCurrentPlayer(move.getPlayer());
    }

    synchronized public boolean undoMove(TrainMove move) {
        board.setPassCount(move.getOldPassCount());
        switch (move.getAction()) {
            case ADD_PLAYER -> board.getPlayers().remove(board.getPlayers().size()-1);
            case RENAME_PLAYER -> board.getPlayers().set(move.getAmount(), move.getCorp());
            case START_GAME -> undoStart(move.getCorp());
            case AUCTION_BUY -> undoAuctionBuy(move);
            case AUCTION_LONEBUY -> undoLoneBuy(move);
            case AUCTION_PASS -> undoAuctionPass(move);
            case AUCTION_BID -> undoAuctionBid(move);
            case AUCTION_REBID -> undoAuctionRebid(move);
            case AUCTION_START_BIDOFF -> undoStartBidoff(move);
            case AUCTION_END_BIDOFF -> undoEndBidoff(move);
            case AUCTION_END_ROUND -> undoAuctionEndRound(move);
            case AUCTION_AWARD_BID -> undoAwardBid(move);
            case AUCTION_REFUND_BID -> undoRefundBid(move);
            case AUCTION_GIVEAWAY -> undoAuctionGiveaway(move);
            case AUCTION_END_PHASE -> undoEndAuctionPhase(move);
            case AUCTION_DISCOUNT_RESET -> undoAuctionDiscountReset(move);
            case STOCK_PASS -> undoStockPass(move);
            case STOCK_END_ROUND -> undoEndStockRound(move);
            case STOCK_SET_PAR -> undoSetPar(move);
            case STOCK_BUY_BANK -> undoBuyBank(move);
            case STOCK_BUY_POOL-> undoBuyPool(move);
            case UPDATE_PREZ -> undoUpdatePrez(move);
            case STOCK_SALE -> undoStockSale(move);
            case STOCK_HEADER ->  {}  // nothing to undo
            case DROP_STOCK_PRICE -> undoDropStockPrice(move);
            case PRICE_DOWN, PRICE_UP, PRICE_LEFT, PRICE_RIGHT -> undoStockStep(move);
            case REORDER_CORP -> undoReorderCorp(move);
            case END_STOCK_ACTION -> undoEndStockAction(move);
            case TAKE_LOAN -> undoTakeLoan(move);
            case USE_WS -> undoUseWS(move);
            case USE_CAN -> undoUseCAN(move);
            case USE_GLS -> undoUseGLS(move);
            case BUY_BRIDGE -> undoBuyBridge(move);
            case BUY_TUNNEL -> undoBuyTunnel(move);
            case BUY_PRIV -> undoBuyPriv(move);
            case FLOAT -> undoFloat(move);
            case FAIL_FLOAT -> undoFailFloat(move);
            case PAY_TOKEN -> undoPayToken(move);
            case PAY_TILE -> undoPayTile(move);
            case DESTINATION -> undoDestination(move);
            case RUN -> undoLastRun(move);
            case INTEREST -> undoInterest(move);
            case PREZ_INTEREST -> undoPrezInterest(move);
            case PAYOUT -> undoPayout(move);
            case WITHHOLD -> undoWithhold(move);
            case BUY_BANK_TRAIN -> undoBankTrain(move);
            case RUST -> undoRust(move);
            case RUST_TRAIN -> undoRustTrain(move);
            case CLOSE_PRIVS -> undoClosePriv(move);
            case REMOVE_PRIV -> undoRemovePriv(move);
            case END_OP_TURN -> undoEndOpTurn(move);
            case NEXT_CORP -> undoNextCorp(move);
            case END_OP_ROUND -> undoEndOpRound(move);
            case CLEAR_BLOCKS -> undoClearBlocks(move);
            default -> { return false; }
        }
        return true;
    }

    public void doClearBlocks(TrainMove move) {
        Wallet w = findWallet(move.getPlayer());
        w.setBlocks(new ArrayList<>());
    }

    public void undoClearBlocks(TrainMove move) {
        Wallet w = findWallet(move.getPlayer());
        w.setBlocks(new ArrayList<String>(Arrays.asList(move.getCorp().split(" "))));
    }

    public void doPrezInterest(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        Wallet w = findWallet(c.getPrez());
        board.setTilePlayed(false);
        board.setTokenPlayed(false);
        payWalletToBank(w, move.getAmount());
        if (w.getCash() < 0) board.setEvent(FORCED_INTEREST_SALE);
        else board.setEvent(POST_REV_EVENT);
    }

    public void undoPrezInterest(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        Wallet w = findWallet(c.getPrez());
        board.setEvent(PRE_REV_EVENT);
        board.setTilePlayed(move.getPlayer().charAt(0) == 'T');
        board.setTokenPlayed(move.getPlayer().charAt(1) == 't');
        payBankToWallet(w, move.getAmount());
    }

    public void doBankTrain(TrainMove move, boolean rawMove) {
        Corp c = findCorp(move.getCorp());
        payCorpToBank(c, TRAIN_PRICE[move.getAmount()]);
        c.getTrains().add(board.getTrains().remove(0));
        if (rawMove) { // TODO first Diesel
            switch(board.getTrains().size()) {
                case 1: makeFollowMove(RUST, "", "", 3); break;
                case 4: makeFollowMove(CLOSE_PRIVS, "", "", 0); break;
                case 8: makeFollowMove(RUST, "", "", 2); break;
            }
        }
    }

    public void undoBankTrain(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        payBankToCorp(c, TRAIN_PRICE[move.getAmount()]);
        board.getTrains().add(0, move.getAmount());
        c.getTrains().remove(Integer.valueOf(move.getAmount()));
    }

    public void doDestination(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        c.setCash(c.getCash() + move.getAmount());
        c.setEscrow(0);
        c.setReachedDest(true);
        c.setFundingType(Corp.INCREMENTAL_TYPE);
        board.setBankCash(board.getBankCash() - move.getAmount());
    }

    public void undoDestination(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        c.setCash(c.getCash() - move.getAmount());
        c.setEscrow(move.getAmount());
        c.setReachedDest(false);
        c.setFundingType(Corp.DESTINATION_TYPE);
        board.setBankCash(board.getBankCash() + move.getAmount());
    }

    private void doLastRun(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        c.setLastRun(move.getAmount());
    }

    private void undoLastRun(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        c.setLastRun(Integer.parseInt(move.getPlayer()));
    }

    private void doPayTile(TrainMove move) {
        Corp c = getCurrentCorp();
        board.setTilePlayed(true);
        payCorpToBank(c, move.getAmount());
    }

    private void undoPayTile(TrainMove move) {
        Corp c = getCurrentCorp();
        board.setTilePlayed(false);
        payBankToCorp(c, move.getAmount());
    }

    private void doPayToken(TrainMove move) {
        Corp c = getCurrentCorp();
        board.setTokenPlayed(true);
        c.setTokensUsed(c.getTokensUsed() + 1);
        payCorpToBank(c, move.getAmount());
    }

    private void undoPayToken(TrainMove move) {
        Corp c = getCurrentCorp();
        board.setTokenPlayed(false);
        c.setTokensUsed(c.getTokensUsed() - 1);
        payBankToCorp(c, move.getAmount());
    }

    private void doBuyPool(TrainMove move, boolean rawMove) {
        Corp c = findCorp(move.getCorp());
        Wallet w = getPlayerWallet(move.getPlayer());
        c.setPoolShares(c.getPoolShares() - 1);
        shareToWallet(w, move.getCorp(), 1);
        payWalletToBank(w, move.getAmount());
        if (rawMove) {
            updatePrez(move.getCorp());
        }
    }

    private void undoBuyPool(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        Wallet w = getPlayerWallet(move.getPlayer());
        c.setPoolShares(c.getPoolShares() + 1);
        shareToWallet(w, move.getCorp(), -1);
        payBankToWallet(w, move.getAmount());
    }

    private void undoEndStockAction(TrainMove move) {
        board.setCurrentPlayer(move.getPlayer());
    }

    private void shareToWallet(Wallet w, String corpName, int shares) {
        boolean wipe = false;
        boolean create = true;
        for (Stock s: w.getStocks()) {
            if (s.getCorp().equals(corpName)) {
                create = false;
                s.setAmount(s.getAmount() + shares);
                if (s.getAmount() == 0) wipe = true;
            }
        }
        if (wipe) w.getStocks().removeIf(x -> corpName.equals(x.getCorp()));
        if (create) w.getStocks().add(new Stock(corpName, shares, false));
    }

    private void updatePrez(String corpName) {
        Corp c = findCorp(corpName);
        int prezCount = 0;
        int maxCount = 1;
        String newPrez = "";
        for(Wallet w: board.getWallets()) {
            boolean prez = false;
            if (w.getName().equals(c.getPrez())) prez = true;
            for(Stock s: w.getStocks()) {
                if (s.getCorp().equals(corpName)) {
                    if (prez) prezCount = s.getAmount();
                    if (s.getAmount() > maxCount) {
                        maxCount = s.getAmount();
                        newPrez = w.getName();
                    }
                }
            }
        }
        if (maxCount > prezCount) {
            makeFollowMove(UPDATE_PREZ, newPrez, corpName, board.getPlayers().indexOf(c.getPrez()));
        }
    }

    private void payWalletToCorpOrEscrow(Wallet w, Corp c, int amount) {
        if(c.getBankShares() < 5) payWalletToEscrow(w, c, amount);
        else payWalletToCorp(w, c, amount);
    }

    private void payWalletFromCorpOrEscrow(Wallet w, Corp c, int amount) {
        if(c.getBankShares() < 5) payEscrowToWallet(w, c, amount);
        else payCorpToWallet(w, c, amount);
    }

    private void payWalletToEscrow(Wallet w, Corp c, int amount) {
        w.setCash(w.getCash() - amount);
        c.setEscrow(c.getEscrow() + amount);
        board.setBankCash(board.getBankCash() + amount); //bank holds the escrow
    }

    private void payEscrowToWallet(Wallet w, Corp c, int amount) {
        w.setCash(w.getCash() + amount);
        c.setEscrow(c.getEscrow() - amount);
        board.setBankCash(board.getBankCash() - amount);
    }

    private void fundCorpIfSix(Wallet w, Corp c, int amount) {
        payWalletToBank(w, amount); // bank holds escrow
        if(c.getBankShares() == 4) { //release escrow
            payBankToCorp(c, amount*10); //TODO TEST ALL_AT_ONCE FUNDING
        }
    }

    private void defundCorpIfSix(Wallet w, Corp c, int amount) {
        payBankToWallet(w, amount);
        if(c.getBankShares() == 4) {
            payCorpToBank(c, amount*10);
        }
    }

    private void payBankToCorp(Corp c, int amount) {
        c.setCash(c.getCash() + amount);
        board.setBankCash(board.getBankCash() - amount);
    }

    private void payCorpToBank(Corp c, int amount) {
        c.setCash(c.getCash() - amount);
        board.setBankCash(board.getBankCash() + amount);
    }

    private void doBuyBank(TrainMove move, boolean rawMove) {
        Corp c = findCorp(move.getCorp());
        Wallet w = getPlayerWallet(move.getPlayer());
        c.setBankShares(c.getBankShares() - 1);
        shareToWallet(w, move.getCorp(), 1);
        if (rawMove) {
            updatePrez(move.getCorp());
        }
        switch (c.getFundingType()) {
            case Corp.DESTINATION_TYPE -> payWalletToCorpOrEscrow(w, c, move.getAmount());
            case Corp.INCREMENTAL_TYPE -> payWalletToCorp(w, c, move.getAmount());
            case Corp.ALL_AT_ONCE_TYPE -> fundCorpIfSix(w, c, move.getAmount());
        }
    }

    private void undoBuyBank(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        Wallet w = getPlayerWallet(move.getPlayer());
        switch (c.getFundingType()) {
            case Corp.DESTINATION_TYPE -> payWalletFromCorpOrEscrow(w, c, move.getAmount());
            case Corp.INCREMENTAL_TYPE -> payCorpToWallet(w, c, move.getAmount());
            case Corp.ALL_AT_ONCE_TYPE -> defundCorpIfSix(w, c, move.getAmount());
        }
        c.setBankShares(c.getBankShares() + 1);
        shareToWallet(w, move.getCorp(), -1);
    }

    /**
     * @return The level of the next available bank train, use 8 for D
     */
    private int availableTrainLevel() {
        return board.getTrains().size() > 1 ? board.getTrains().get(0) : 8;
    }

    /**
     * @return The highest level of train sold
     */
    private int highestTrainSold() {
        switch (board.getTrains().size()) {
            case 0: case 1:
                return 6; //TODO figure out if a D has been sold
            case 2: case 3: case 4:
                return 5;
            case 5: case 6: case 7: case 8:
                return 4;
            case 9: case 10: case 11: case 12: case 13:
                return 3;
            default:
                return 2;
        }
    }

    private int currentFloatType() {
        switch (availableTrainLevel()) {
            case 2: case 3: case 4:
                return Corp.DESTINATION_TYPE;
            case 5:
                return Corp.INCREMENTAL_TYPE;
            default:
                return Corp.ALL_AT_ONCE_TYPE;
        }
    }

    private void undoSetPar(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        c.setPar(0);
        c.setPrez("");
        c.setBankShares(10);
        c.setPrice(null);
        board.getCorps().remove(c);
        board.getCorps().add(oldParIndex(c), c);
        Wallet w = findWallet(move.getPlayer());
        if(c.getFundingType() != Corp.ALL_AT_ONCE_TYPE) {
            payCorpToWallet(w, c, 2 * move.getAmount());
        } else {
            payBankToWallet(w,2 * move.getAmount());
        }
        c.setFundingType(0);
        w.getStocks().removeIf(x -> x.getCorp().equals(move.getCorp()));
    }

    private int oldParIndex(Corp c) {
        int i = board.getCorps().size();
        while (i > 0) {
            i--;
            Corp c2 = board.getCorps().get(i);
            if (c2.getPrice() != null || c2.getName().compareTo(c.getName()) > 0) continue;
            return i+1;
        }
        return 0;
    }

    private int newStockIndex(Corp c) {
        int i = board.getCorps().size();
        while (i > 0) {
            i--;
            Corp c2 = board.getCorps().get(i);
            if (c2.getPrice() == null || c2.getPrice().getPrice() < c.getPrice().getPrice()) continue;
            return i+1;
        }
        return 0;
    }

    private void doSetPar(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        c.setPar(move.getAmount());
        c.setPrez(move.getPlayer());
        c.setBankShares(8);
        c.setPrice(StockPrice.makePar(move.getAmount()));
        c.setFundingType(currentFloatType());
        board.getCorps().remove(c);
        board.getCorps().add(newStockIndex(c), c);
        Wallet w = findWallet(move.getPlayer());
        if (c.getFundingType() != Corp.ALL_AT_ONCE_TYPE) {
            payWalletToCorp(w, c, 2 * move.getAmount());
        } else {
            payWalletToBank(w, 2 * move.getAmount()); //bank holds escrow
        }
        w.getStocks().add(new Stock(move.getCorp(), 2, true));
    }

    private int maxRounds() {
        switch (highestTrainSold()) {
            case 2:         return 1;
            case 3: case 4: return 2;
            default:        return 3;
        }
    }

    private void doEndStockRound(TrainMove move, boolean rawMove) {
        board.setCurrentOpRound(1);
        board.setMaxOpRounds(maxRounds());
        board.setPhase(Era.OP.name());
        board.setEvent(PRE_REV_EVENT);
        for(Corp c: board.getCorps()) {
            c.setHasOperated(false);
        }

        if (rawMove) {
            List<Corp> risers = new ArrayList<>();
            for(Corp c: board.getCorps()) {
                if(c.getPoolShares() == 0 && c.getBankShares() == 0 && !c.getPrice().ceiling()) {
                    risers.add(c);
                }
            }
            for (Corp c: risers) {
                makeFollowMove(PRICE_UP, "", c.getName(), 1);
            }
            for (Wallet w: board.getWallets()) {
                if(!w.getBlocks().isEmpty()) {
                    makeFollowMove(CLEAR_BLOCKS, w.getName(), String.join(" ", w.getBlocks()), 0);
                }
            }
        }
        payPrivs();
        if (rawMove) setNextOpCorp();
    }

    private void setNextOpCorp() {
        for (Corp corp: board.getCorps()) {
            if (corp.isHasOperated()) continue;
            if (!corp.isHasFloated()) {
                int min = availableTrainLevel();
                if (10-corp.getBankShares() < min) {
                    makeFollowMove(FAIL_FLOAT, "", corp.getName(), 0);
                    continue;
                } else {
                    makeFollowMove(FLOAT, board.getCurrentCorp(), corp.getName(), board.isLoanTaken() ? 1: 0);
                    return;
                }
            }
            makeFollowMove(NEXT_CORP, board.getCurrentCorp(), corp.getName(), board.isLoanTaken() ? 1: 0);
            return;
        }
        makeFollowMove(END_OP_ROUND, "", board.getCurrentCorp(),
                       board.isLoanTaken() ? board.getMaxOpRounds(): -board.getMaxOpRounds()
        );
    }

    private void undoEndStockRound(TrainMove move) {
        board.setCurrentOpRound(0);
        board.setMaxOpRounds(0);
        board.setPhase(Era.STOCK.name());
        board.setEvent(NORMAL_EVENT);
        unpayPrivs();
    }

    private void doStockPass(TrainMove move, boolean rawMove) {
        int index = board.getPlayers().indexOf(board.getCurrentPlayer()) + 1;
        if (index >= board.getPlayers().size()) index = 0;
        board.setCurrentPlayer(board.getPlayers().get(index));
        board.setPassCount(board.getPassCount()+1);
        if(rawMove && board.getPassCount() == board.getPlayers().size()) {
            makeFollowMove(STOCK_END_ROUND, board.getCurrentPlayer(), board.getPhase(), 0);
        }
    }

    private void undoStockPass(TrainMove move) {
        board.setCurrentPlayer(move.getPlayer());
    }

    private void undoAuctionDiscountReset(TrainMove move) {
        board.setAuctionDiscount(move.getAmount());
    }

    private void doAuctionDiscountReset() {
        board.setAuctionDiscount(0);
    }

    static List<Corp> INIT_IPO = List.of(
            new Corp("BBG", 3),
            new Corp("CA", 3),
            new Corp("CPR", 4),
            new Corp("CV", 3),
            new Corp("GT", 4),
            new Corp("GW", 4),
            new Corp("LPS", 2),
            new Corp("TGB", 2),
            new Corp("THB", 2),
            new Corp("WGB", 3),
            new Corp("WR", 2)
    );

    static List<Integer> PAR_VALUES = List.of(65, 70, 75, 80, 90, 100);

    private void doEndAuctionPhase(TrainMove move) {
        board.setPhase(Era.INITIAL.name());
        for(Corp c: INIT_IPO) {
            board.getCorps().add(c.dup());
        }
    }

    private void undoEndAuctionPhase(TrainMove move) {
        board.setCorps(new ArrayList<>());
        board.setPhase(Era.AUCTION.name());
    }

    private void doAwardBid(TrainMove move) {
        for (Wallet w: board.getWallets()) {
            if(w.getName().equals(move.getPlayer())) {
                for(Priv priv: w.getPrivates()) {
                    if(priv.getCorp().equals(move.getCorp())) {
                        priv.setAmount(3);
                    }
                }
            }
        }
    }

    private void undoAwardBid(TrainMove move) {
        for (Wallet w: board.getWallets()) {
            if(w.getName().equals(move.getPlayer())) {
                for(Priv priv: w.getPrivates()) {
                    if(priv.getCorp().equals(move.getCorp())) {
                        priv.setAmount(move.getAmount());
                    }
                }
            }
        }
    }

    private void doRefundBid(TrainMove move) {
        for (Wallet w: board.getWallets()) {
            if (w.getName().equals(move.getPlayer())) {
                payBankToWallet(w, move.getAmount());
                w.getPrivates().removeIf(x -> x.getCorp().equals(move.getCorp()));
            }
        }
    }

    private void undoRefundBid(TrainMove move) {
        for (Wallet w: board.getWallets()) {
            if (w.getName().equals(move.getPlayer())) {
                payWalletToBank(w, move.getAmount());
                w.getPrivates().add(new Priv(move.getCorp(), move.getAmount()));
            }
        }
    }

    private void doEndBidoff(TrainMove move, boolean rawMove) {
        for (Wallet w: board.getWallets()) {
            if(move.getPlayer().equals(w.getName())) {
                payWalletToBank(w, move.getAmount());
            }
        }
        incrementPrivate(rawMove);
        if (rawMove) {
            for (Wallet w: board.getWallets()) {
                Priv award = null;
                Priv refund = null;
                for (Priv priv: w.getPrivates()) {
                    if(priv.getCorp().equals(move.getCorp())) {
                        if(w.getName().equals(move.getPlayer())) {
                            award = priv;
                        } else {
                            refund = priv;
                        }
                    }
                }
                if (award != null) makeFollowMove(AUCTION_AWARD_BID, move.getPlayer(), move.getCorp(), award.getAmount());
                if (refund != null) makeFollowMove(AUCTION_REFUND_BID, w.getName(), move.getCorp(), refund.getAmount());
            }
        }
    }

    private void undoEndBidoff(TrainMove move) {
        for (Wallet w: board.getWallets()) {
            if(move.getPlayer().equals(w.getName())) {
                payBankToWallet(w, move.getAmount());
            }
        }
        board.setEvent(BIDOFF_EVENT);
        board.setCurrentCorp(move.getCorp());
    }

    private void lockUndo() {
        while(board.getUndoCount() > 0) {
            TrainMove out = history.get(board.getMoveNumber()-1);
            repo.delete(out);
            history.remove(out);
            board.setUndoCount(board.getUndoCount()-1);
            board.setMoveNumber(board.getMoveNumber()-1);
        }
    }
    private void makePrimaryMove(String action, String player, String corp, int amount) {
        makeMove(action, player, corp, amount, false);
    }

    private void makeFollowMove(String action, String player, String corp, int amount) {
        makeMove(action, player, corp, amount, true);
    }

    private void makeMove(String action, String player, String corp, int amount, boolean isFollow) {
        if(board.getUndoCount() > 0) {
            lockUndo();
        }
        TrainMoveID id = new TrainMoveID(board.getName(), board.getMoveNumber()+1);
        TrainMove out = new TrainMove(id, action, player, corp, amount, board.getPassCount(), isFollow);
        repo.save(out);
        board.setMoveNumber(id.getSerialNumber());
        history.add(out);
        doMove(out, true);
    }

    void recalculatePriority() {
        if(phaseIs(Era.GATHER)) return;
        int index = board.getPlayers().indexOf(board.getCurrentPlayer()) - board.getPassCount();
        if (index < 0) index += board.getPlayers().size();
        board.setPriorityHolder(board.getPlayers().get(index));
    }

    synchronized public Board1856 undo() {
        if (board.getUndoCount() == board.getMoveNumber()) return board;
        TrainMove move = history.get(board.getMoveNumber()-board.getUndoCount()-1);
        if (undoMove(move)) {
            board.setUndoCount(board.getUndoCount()+1);
            board.setPassCount(move.getOldPassCount());
            recalculatePriority();
            if (move.isFollow()) return undo();
        }
        return board;
    }

    synchronized public Board1856 redo() {
        if (board.getUndoCount() < 1) return board;
        TrainMove currentMove = history.get(board.getMoveNumber()-board.getUndoCount());
        doMove(currentMove, false);
        board.setUndoCount(board.getUndoCount()-1);
        if (board.getUndoCount() > 0) {
            TrainMove nextMove = history.get(board.getMoveNumber() - board.getUndoCount());
            if (nextMove.isFollow()) return redo();
        }
        return board;
    }

    synchronized public Board1856 redoAll() {
        while (board.getUndoCount() > 0) {
            redo();
        }
        return board;
    }

    synchronized public boolean addPlayer(String name) {
        if(!Era.GATHER.name().equals(board.getPhase())) {
            throw new IllegalStateException("Too Late to Add Players");
        }
        if (board.getPlayers().contains(name)) {
            throw new IllegalStateException("Duplicate Name not allowed");
        }
        if(board.getPlayers().size() > 6) {
            throw new IllegalStateException("Maximum Player Count: 6");
        }
        makePrimaryMove(ADD_PLAYER, name, "", 0);
        return true;
    }

    synchronized public Board1856 renamePlayer(String oldName, String newName) {
        int seat = board.getPlayers().indexOf(oldName);
        if(seat < 0) {
            throw new IllegalStateException("Player not found");
        }
        if (board.getPlayers().contains(newName)) {
            throw new IllegalStateException("Duplicate Name not allowed");
        }
        makePrimaryMove(RENAME_PLAYER, newName, oldName, seat);
        return board;
    }

    synchronized public Board1856 startGame(boolean shuffle) {
        if(board.getPlayers().size() < 3) {
            throw new IllegalStateException("Need at least 3 players to start");
        }
        if (!Era.GATHER.name().equals(board.getPhase()) || board.getPlayers().size() > 6) {
            throw new IllegalStateException("Game is not startable");
        }
        makePrimaryMove(START_GAME, "", makeShuffle(shuffle, board.getPlayers().size()), 0);
        return board;
    }

    private Wallet getCurrentWallet() {
        return board.getWallets().get(board.getPlayers().indexOf(board.getCurrentPlayer()));
    }

    private boolean phaseIs(Era phase) {
        return board.getPhase().equals(phase.name());
    }

    private boolean eventIs(String event) {
        return board.getEvent().equals(event);
    }

    synchronized public Board1856 auctionBuy() {
        enforcePhase(Era.AUCTION);
        enforceEvent(NORMAL_EVENT);
        int price = priv2price.get(board.getCurrentCorp()) - board.getAuctionDiscount();
        int cash = getCurrentWallet().getCash();
        if( price > cash) throw new IllegalStateException(("not enough cash"));
        makePrimaryMove(AUCTION_BUY, board.getCurrentPlayer(), board.getCurrentCorp(), price);
        return board;
    }

    synchronized public Board1856 pass() {
        if (phaseIs(Era.AUCTION) && eventIs(NORMAL_EVENT)) {
            makePrimaryMove(AUCTION_PASS, board.getCurrentPlayer(), "", 1);
            return board;
        }
        if (phaseIs(Era.STOCK) || phaseIs(Era.INITIAL)) {
            makePrimaryMove(STOCK_PASS, board.getCurrentPlayer(), "", 1);
            return board;
        }
        throw new IllegalStateException("Pass not allowed here");
    }

    /**
     * @return 0 if not a rebid, amount to increase bid if rebid
     */
    private int calcRebidIncrement(Wallet w, String corp, int amount) {
        for (Priv priv: w.getPrivates()) {
            if(priv.getCorp().equals(corp)) {
                return amount - priv.getAmount();
            }
        }
        return 0; // not a rebid
    }

    synchronized public Board1856 bid(String corp, int amount) {
        enforcePhase(Era.AUCTION);
        enforceEvent(NORMAL_EVENT);
        int overbid = highBid(corp) + 5;
        int price = priv2price.get(corp);
        int minBid = (price > overbid) ? price + 5 : overbid;

        if (amount < minBid) {
            throw new IllegalStateException("Minimum Bid is "+minBid);
        }
        int incr = calcRebidIncrement(getCurrentWallet(), corp, amount);
        Wallet w = getCurrentWallet();
        if (incr > 0) {
            if(incr > w.getCash()) {
                throw new IllegalStateException(FUNDS);
            }
            makePrimaryMove(AUCTION_REBID, board.getCurrentPlayer(), corp, incr);
        } else {
            if(amount > w.getCash()) {
                throw new IllegalStateException(FUNDS);
            }
            makePrimaryMove(AUCTION_BID, board.getCurrentPlayer(), corp, amount);
        }
        return board;
    }

    private Wallet getPlayerWallet(String player) {
        int playerIndex = board.getPlayers().indexOf(player);
        if(playerIndex < 0) throw new IllegalStateException("Unknown Player");
        return board.getWallets().get(playerIndex);
    }

    synchronized public Board1856 bidoff(String player, int amount) {
        enforcePhase(Era.AUCTION);
        enforceEvent(BIDOFF_EVENT);
        Wallet w = getPlayerWallet(player);
        String corp = board.getCurrentCorp();
        int oldMin = highBid(corp);

        int increment = 99999;
        for (Priv priv: w.getPrivates()) {
            if (priv.getCorp().equals(corp)) increment = amount - priv.getAmount();
        }
        if (increment > w.getCash()) throw new IllegalStateException(FUNDS);
        if ((increment == 0 && amount == oldMin) || amount >= oldMin + 5) {
            makePrimaryMove(AUCTION_END_BIDOFF, player, corp, increment);
        } else {
            throw new IllegalStateException("Minimum Raise is $5");
        }
        return board;
    }

    private Corp findCorp(String name) {
        for (Corp c: board.getCorps()) {
            if(c.getName().equals(name)) return c;
        }
        throw new IllegalStateException("Unknown Corp");
    }

    private Wallet findWallet(String name) {
        for(Wallet w: board.getWallets()) {
            if (w.getName().equals(name)) return w;
        }
        throw new IllegalStateException("Unknown Player");
    }

    private void canPar(Corp c, int par, Wallet w, int extraCash) {
        enforcePhase(Era.STOCK, Era.INITIAL);
        if(!PAR_VALUES.contains(par)) {
            throw new IllegalStateException("Par Value must be 65, 70, 75, 80, 90, or 100");
        }
        if (w.getCash() + extraCash < par * 2) throw new IllegalStateException(FUNDS);
        if(c.getPar() != 0) throw new IllegalStateException("Par is already set");
    }

    synchronized public Board1856 setPar(String corpName, int par) {
        Wallet w = findWallet(board.getCurrentPlayer());
        Corp corp = findCorp(corpName);
        canPar(corp, par, w, 0);
        if (certCount(w) + 2 > certLimit()) throw new IllegalStateException("Certificate Limit Violation");
        //TODO enforce cert limits (+2)
        makePrimaryMove(STOCK_SET_PAR, board.getCurrentPlayer(), corpName, par);
        makeFollowMove(END_STOCK_ACTION, board.getCurrentPlayer(), "", 0);
        return board;
    }

    private void canBuyBank(Corp c, Wallet w, int extraCash) {
        enforcePhase(Era.STOCK, Era.INITIAL);
        if (c.getBankShares() < 1) throw new IllegalStateException("No Bank Shares Remain");
        if (w.getCash() + extraCash < c.getPar()) throw new IllegalStateException(FUNDS);
        if (w.getBlocks().contains(c.getName())) throw new IllegalStateException("No buy after sell same round");
        for (Stock s: w.getStocks()) { //TODO exception for low price sections
            if(!s.getCorp().equals(c.getName())) continue;
            if(s.getAmount() >= 6) throw new IllegalStateException("Max 60% of company");
        }
    }

    synchronized public Board1856 buyBank(String corpName) {
        Wallet w = findWallet(board.getCurrentPlayer());
        Corp c = findCorp(corpName);
        canBuyBank(c, w, 0);
        if (certCount(w) + 1 > certLimit()) throw new IllegalStateException("Certificate Limit Violation");
        makePrimaryMove(STOCK_BUY_BANK, board.getCurrentPlayer(), corpName, c.getPar());
        makeFollowMove(END_STOCK_ACTION, board.getCurrentPlayer(), "", 0);
        return board;
    }

    private void canBuyPool(Corp c, Wallet w, int extraCash) {
        enforcePhase(Era.STOCK, Era.INITIAL);
        if (c.getPoolShares() < 1) throw new IllegalStateException("No pool shares available");
        if(w.getCash() + extraCash < c.getPrice().getPrice()) throw new IllegalStateException(FUNDS);
        if (w.getBlocks().contains(c.getName())) throw new IllegalStateException("No buy after sell same round");
        for (Stock s: w.getStocks()) { //TODO exception for low price sections
            if(!s.getCorp().equals(c.getName())) continue;
            if(s.getAmount() >= 6) throw new IllegalStateException("Max 60% of company");
        }
    }

    synchronized public Board1856 buyPool(String corpName) {
        Wallet w = findWallet(board.getCurrentPlayer());
        Corp c = findCorp(corpName);
        canBuyPool(c, w, 0);
        if (certCount(w) + 1 > certLimit()) throw new IllegalStateException("Certificate Limit Violation");
        makePrimaryMove(STOCK_BUY_POOL, board.getCurrentPlayer(), corpName, c.getPrice().getPrice());
        makeFollowMove(END_STOCK_ACTION, board.getCurrentPlayer(), "", 0);
        return board;
    }

    synchronized public Board1856 buySell(String buyType, String corpName, int par, List<StockSale> sales) {
        boolean isBank = false;
        boolean isPar = false;
        switch (buyType) {
            case "bank" -> isBank = true;
            case "par" -> isPar = true;
            case "pool" -> {}
            default -> throw new IllegalStateException("Unknown Buy Type "+buyType);
        }
        Wallet w = findWallet(board.getCurrentPlayer());
        Corp c = findCorp(corpName);
        if (isBank) canBuyBank(c, w, 0);
        else if (isPar) canPar(c, par, w, 0);
        else canBuyPool(c, w, 0);
        checkSalesList(sales);
        if (certCount(w) + ((isPar) ? 2 : 1) > certLimit()) throw new IllegalStateException("Certificate Limit Violation");
        makePrimaryMove(STOCK_HEADER, board.getCurrentPlayer(), "", 0);
        if (isBank) makeFollowMove(STOCK_BUY_BANK, w.getName(), c.getName(), c.getPar());
        else if (isPar) makeFollowMove(STOCK_SET_PAR, w.getName(), c.getName(), par);
        else makeFollowMove(STOCK_BUY_POOL, w.getName(), c.getName(), c.getPrice().getPrice());
        for(StockSale sale: sales) {
            makeFollowMove(STOCK_SALE, board.getCurrentPlayer(), sale.getName(), sale.getAmount());
        }
        makeFollowMove(END_STOCK_ACTION, board.getCurrentPlayer(), "", 0);
        return board;
    }

    private int salesValue(List<StockSale> sales) {
        int value = 0;
        for (StockSale sale: sales) {
            Corp c = findCorp(sale.getName());
            value += c.getPrice().getPrice() * sale.getAmount();
        }
        return value;
    }

    synchronized public Board1856 sellBuy(String buyType, String buyCorpName, int par, List<StockSale> sales) {
        boolean isBank = false;
        boolean isPar = false;
        switch (buyType) {
            case "bank" -> isBank = true;
            case "par" -> isPar = true;
            case "pool" -> {}
            default -> throw new IllegalStateException("Unknown Buy Type "+buyType);
        }
        Wallet w = findWallet(board.getCurrentPlayer());
        for(StockSale s: sales) {
            if(s.getName().equals(buyCorpName)) throw new IllegalStateException("No buy after sell same round");
        }
        Corp c = findCorp(buyCorpName);
        checkSalesList(sales);
        int extraCash = salesValue(sales);
        int extraCerts = certValue(sales);
        if (certCount(w) + ((isPar) ? 2 : 1) > certLimit() - extraCerts) {
            throw new IllegalStateException("Certificate Limit Violation");
        }
        if (isBank) canBuyBank(c, w, extraCash);
        else if (isPar) canPar(c, par, w, extraCash);
        else canBuyPool(c, w, extraCash);
        makePrimaryMove(STOCK_HEADER, board.getCurrentPlayer(), "", 0);
        for(StockSale sale: sales) {
            makeFollowMove(STOCK_SALE, board.getCurrentPlayer(), sale.getName(), sale.getAmount());
        }
        if (isBank) makeFollowMove(STOCK_BUY_BANK, w.getName(), c.getName(), c.getPar());
        else if (isPar) makeFollowMove(STOCK_SET_PAR, w.getName(), c.getName(), par);
        else makeFollowMove(STOCK_BUY_POOL, w.getName(), c.getName(), c.getPrice().getPrice());
        makeFollowMove(END_STOCK_ACTION, board.getCurrentPlayer(), "", 0);
        return board;
    }

    private int certCount(Wallet w) {
        int out = 0;
        for (Stock s: w.getStocks()) {
            out += s.getAmount();
            if (s.isPresident()) out --;
        }
        return out + w.getPrivates().size();
    }

    private int certValue(List<StockSale> sales) {
        int out = 0;
        for(StockSale s: sales) {
            out += s.getAmount();
            //if (prezWillChange()) out++; TODO anticipate change in prez
        }
        return out;
    }

    synchronized public Board1856 payToken() {
        enforcePhase(Era.OP);
        enforceEvent(PRE_REV_EVENT);
        if(board.isTokenPlayed()) throw new IllegalStateException("One paid token per turn");
        Corp c = getCurrentCorp();
        if(c.getTokensUsed() >= c.getTokensMax()) throw new IllegalStateException("All tokens used");
        int price = c.getTokensUsed() > 1 ? 100 : 40;
        if(c.getCash() < price) throw new IllegalStateException(FUNDS);
        makePrimaryMove(PAY_TOKEN, "", board.getCurrentCorp(), price);
        return board;
    }

    synchronized public Board1856 payTile() {
        enforcePhase(Era.OP);
        enforceEvent(PRE_REV_EVENT);
        if(board.isTilePlayed()) throw new IllegalStateException("One paid tile per turn");
        Corp c = getCurrentCorp();
        if(c.getCash() < 40) throw new IllegalStateException(FUNDS);
        makePrimaryMove(PAY_TILE, "", board.getCurrentCorp(), 40);
        return board;
    }

    // (T)ile and (t)oken state restoration string
    private String restorePreRev() {
        return board.isTilePlayed() ?
                (board.isTokenPlayed() ? "Tt" : "T-") :
                (board.isTokenPlayed() ? "-t" : "--");
    }
    synchronized public Board1856 payout(int amount) {
        enforcePhase(Era.OP);
        enforceEvent(PRE_REV_EVENT);
        if(amount % 10 != 0) throw new IllegalStateException("Revenue must be a multiple of 10");
        if(amount < 10) throw new IllegalStateException("Minimum payout is $10");
        Corp c = getCurrentCorp();
        int interest = 10 * c.getLoans();
        if (interest <= c.getCash()) {
            makePrimaryMove(RUN, ""+c.getLastRun(), c.getName(), amount);
            if (interest == 0) {
                makeFollowMove(PAYOUT, restorePreRev(), c.getName(), amount / 10);
            } else {
                makeFollowMove(INTEREST, "", c.getName(), interest);
                makeFollowMove(PAYOUT, restorePreRev(), c.getName(), amount / 10);
            }
        } else {
            int downPayment = c.getCash() / 10 * 10;
            int remainder = interest - downPayment;
            if (remainder >= amount) throw new IllegalStateException("Too much interest to pay out");
            makePrimaryMove(RUN, ""+c.getLastRun(), c.getName(), amount);
            makeFollowMove(INTEREST, "", c.getName(), downPayment);
            makeFollowMove(PAYOUT, restorePreRev(), c.getName(), (amount - remainder) / 10);
        }
        if(c.getPrice().rightEdge()) {
            if (!c.getPrice().ceiling()) {
                makeFollowMove(PRICE_UP, "", c.getName(), 1);
            } //TODO make sure this triggers end of game
        } else {
                makeFollowMove(PRICE_RIGHT, "", c.getName(), 1);
        }
        return board;
    }

    synchronized public Board1856 withhold(int amount) {
        enforcePhase(Era.OP);
        enforceEvent(PRE_REV_EVENT);
        if(amount % 10 != 0) throw new IllegalStateException("Revenue must be a multiple of 10");
        if(amount < 0) throw new IllegalStateException("Negative Revenue is not possible");
        Corp c = getCurrentCorp();
        int interest = 10 * c.getLoans();
        if (interest > c.getCash()) {
            int downPayment = c.getCash()/10*10;
            int remainder = interest - downPayment;
            makePrimaryMove(RUN, ""+c.getLastRun(), c.getName(), amount);
            makeFollowMove(INTEREST, "", c.getName(), downPayment);
            if (remainder > amount) {
                makeFollowMove(PREZ_INTEREST, restorePreRev(), c.getName(), remainder - amount);
            } else {
                makeFollowMove(WITHHOLD, restorePreRev(), c.getName(), amount - remainder);
            }
        } else {
            makePrimaryMove(RUN, ""+c.getLastRun(), c.getName(), amount);
            if (interest > 0) makeFollowMove(INTEREST, "", c.getName(), interest);
            makeFollowMove(WITHHOLD, restorePreRev(), c.getName(), amount);
        }
        if(c.getPrice().leftEdge()) {
            if (!c.getPrice().floor()) { //TODO check closure (where?) price == 30, notfloor?
                makeFollowMove(PRICE_DOWN, "", c.getName(), 1);
            }
        } else {
            makeFollowMove(PRICE_LEFT, "", c.getName(), 1);
        }
        return board;
    }

    synchronized public Board1856 destination() {
        enforcePhase(Era.OP);
        enforceEvent(PRE_REV_EVENT);
        Corp corp = getCurrentCorp();
        if (corp.isReachedDest()) throw new IllegalStateException("Already at destination");
        if (corp.getFundingType() != Corp.DESTINATION_TYPE) throw new IllegalStateException("No destination to reach");
        makePrimaryMove(DESTINATION, "", corp.getName(), corp.getEscrow());
        return board;
    }

    int[] TRAIN_PRICE = { 0, 0, 100, 225, 350, 550, 700, 0, 1100 };
    int[] TRAIN_LIMIT = { 0, 0, 4, 4, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2};
    private int trainLimit(Board1856 board, Corp corp) {
        if(corp.getName().equals("CGR")) return 3;
        return TRAIN_LIMIT[highestTrainSold()];
    }

    int[] EARLY_CERT_LIMIT = {20, 16, 13, 11};
    int[][] LATE_CERT_LIMIT = {
            {10, 13, 15, 18, 20, 22, 25, 28},
            {8, 10, 12, 14, 16, 18, 20, 22},
            {7, 8, 10, 11, 13, 15, 16, 18},
            {6, 7, 8, 10, 11, 12, 14, 15}
    };

    private int certLimit() {
        if(board.getTrains().size() > 1) {
            return EARLY_CERT_LIMIT[board.getPlayers().size() - 3];
        }
        return LATE_CERT_LIMIT[board.getPlayers().size() - 3][board.getCorps().size() - 4];
    }

    private void enforcePhase(Era e1, Era e2) {
        if (board.getPhase().equals(e1.name())) return;
        if (board.getPhase().equals(e2.name())) return;
        throw new IllegalStateException("Action not legal in this phase");
    }

    /**
     * when a generic exception will do
     */
    private void enforcePhase(Era era) {
        if (!phaseIs(era)) throw new IllegalStateException("Not in "+era.name()+" round");
    }

    private void enforceEvent(String event) {
        if(!eventIs(event)) throw new IllegalStateException("Not currently "+event);
    }

    synchronized public Board1856 buyBankTrain() {
        // TODO TRADE-INS
        // TODO FORCED CONTRIBUTIONS
        // TODO ENFORCE LIMIT DROP TO POOL
        enforcePhase(Era.OP);
        int size = (board.getTrains().isEmpty()) ? 8 : board.getTrains().get(0);
        int price =  TRAIN_PRICE[size];
        Corp c = getCurrentCorp();
        if (c.getTrains().size() >= trainLimit(board, c)) throw new IllegalStateException("Too many trains");
        if (price > c.getCash()) throw new IllegalStateException(FUNDS);
        makePrimaryMove(BUY_BANK_TRAIN, "", c.getName(), size);
        return board;
    }

    synchronized public Board1856 endOpTurn() {
        enforcePhase(Era.OP);
        enforceEvent(POST_REV_EVENT);
        makePrimaryMove(END_OP_TURN, "", board.getCurrentCorp(), 0);
        return board;
    }

    synchronized public Board1856 usePriv(String priv, boolean option) {
        enforcePhase(Era.OP);
        Corp c = getCurrentCorp();
        switch (priv) {
            case PRIVATE_WS -> useWS(c, option);
            case PRIVATE_CAN -> useCAN(c);
            case PRIVATE_GLS -> useGLS(c);
            case PRIVATE_NIAG -> buyBridge(c);
            case PRIVATE_STC -> buyTunnel(c);
            default -> throw new IllegalStateException("No such Private: "+priv);
        }
        return board;
    }

    private void enforceOwns(Corp c, String privName) {
        for(Priv p: c.getPrivates()) {
            if (p.getCorp().equals(privName)) return;
        }
        throw new IllegalStateException("Corp does not own this private");
    }

    private void enforceHasToken(Corp c) {
        if (c.getTokensUsed() == c.getTokensMax()) throw new IllegalStateException("No more tokens");
    }

    private void useWS(Corp c, boolean option) {
        enforceOwns(c, PRIVATE_WS);
        if (option) enforceHasToken(c);
        makePrimaryMove(USE_WS, "", c.getName(), option ? 1 : 0);
    }

    private void useCAN(Corp c) {
        enforceOwns(c, PRIVATE_CAN);
        makePrimaryMove(USE_CAN, "", c.getName(), 0);
    }

    private void useGLS(Corp c) {
        enforceOwns(c, PRIVATE_GLS);
        makePrimaryMove(USE_GLS, "", c.getName(), 0);
    }

    private void buyBridge(Corp c) {
        if(c.isBridgeRights()) throw new IllegalStateException("Already have bridge rights");
        if(c.getCash() < 50) throw new IllegalStateException(FUNDS);
        if(board.getBridgeTokens() < 1) throw new IllegalStateException("No more bridge tokens");
        makePrimaryMove(BUY_BRIDGE, "", c.getName(), 0);
    }

    private void buyTunnel(Corp c) {
        if(c.isTunnelRights()) throw new IllegalStateException("Already have tunnel rights");
        if(c.getCash() < 50) throw new IllegalStateException(FUNDS);
        if(board.getTunnelTokens() < 1) throw new IllegalStateException("No Tunnel Tokens Left");
        makePrimaryMove(BUY_TUNNEL, "", c.getName(), 0);
    }

    private Priv findPriv(String privName) {
        for(Corp c: board.getCorps()) {
            for(Priv p: c.getPrivates()) {
                if(p.getCorp().equals(privName)) return p;
            }
        }
        return null;
    }
}