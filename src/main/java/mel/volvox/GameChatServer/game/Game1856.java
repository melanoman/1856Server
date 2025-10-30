package mel.volvox.GameChatServer.game;

import lombok.Getter;
import lombok.Setter;
import mel.volvox.GameChatServer.comm.train.*;
import mel.volvox.GameChatServer.model.train.TrainMove;
import mel.volvox.GameChatServer.model.train.TrainMoveID;
import mel.volvox.GameChatServer.repository.TrainRepo;
import mel.volvox.GameChatServer.service.DiceService;

import static mel.volvox.GameChatServer.comm.train.StockPrice.encodePrice;
import static mel.volvox.GameChatServer.comm.train.StockPrice.decodePrice;

import java.util.*;

public class Game1856 extends AbstractGame {
    @Setter TrainRepo repo; // set by controller
    @Getter private final Board1856 board = new Board1856();
    @Getter private final Map<String, Integer> corp2price = new HashMap<>();

    public static final int[] START_CASH = { 0, 0, 0, 500, 375, 300, 250 };
    public static final String NONE = "";
    public static Integer DIESEL_TRAIN = 8;

    // error constants
    public static final String FUNDS = "Insufficient Funds";

    // action constants --- these make the database table human-readable
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
    public static final String BUY_POOL_TRAIN = "buyPoolTrain";
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
    public static final String BANK_DIESEL = "bankDiesel";
    public static final String TRADEIN = "tradein";
    public static final String RUST = "rust";
    public static final String RUST_TRAIN = "rustTrain";
    public static final String CLOSE_PRIVS = "closePrivs";
    public static final String REMOVE_PRIV = "removePriv";
    public static final String REDEEM_LOAN = "redeemLoan";
    public static final String END_OP_TURN = "endOpTurn";
    public static final String C2C_TRAIN_BUY = "c2cTrainBuy";
    public static final String FORCED_BANK_TRAIN = "forcedBankTrainBuy";
    public static final String FORCED_POOL_BUY = "forcedPoolTrainBuy";
    public static final String ASK_TRAIN_DROP ="askTrainDrop";
    public static final String FORCED_SALE = "forcedSale";
    public static final String DROP_TRAIN = "dropTrain";
    public static final String DROP_CGR_TRAIN = "dropCGRTrain";
    public static final String DONE_CGR_DROP = "doneCGRdrop";
    public static final String END_CGR_DROP = "endCGRdrop";
    public static final String UNFREEZE = "unfreeze";
    public static final String SET_CGR_TOKENS = "setCGRtokens";
    public static final String DROP_PORT = "dropPort";
    public static final String NEXT_CORP = "nextCorp";
    public static final String END_OP_ROUND = "endOpRound";
    public static final String BANK_BREACH = "bankBreach";

    public static final String START_CGR_REDEMPTIONS = "startCGR";
    public static final String AUTOPAY_CGR_LOANS = "autoPayCGR";
    public static final String ASK_REDEMPTION = "askRedemption";
    public static final String REDEEM_FROM_CGR = "redeemCGR";
    public static final String ABANDON_TO_CGR = "abandonCGR";
    public static final String ABANDON_CORP = "abandonCorp";
    public static final String FORM_CGR = "formCGR";
    public static final String CLEAR_BLOCKS = "clearBlocks";
    public static final String CGR_FOLD = "GCRfold";
    public static final String CGR_PAR = "CGRpar";
    public static final String CGR_ESCROW = "CGRescrow";
    public static final String CGR_CASH = "CGRcash";
    public static final String CGR_TOKEN = "CGRtoken";
    public static final String CGR_TRAIN = "CGRtrain";
    public static final String TRADE = "trade"; //trade for cgr shares
    public static final String TRADE_PREZ = "tradePrez";

    // event constants
    // TODO move error message substrings out of constants to insulate clients
    public static final String NORMAL_EVENT = "in normal turn";
    public static final String BIDOFF_EVENT = "resolving conflicting bids";
    public static final String PRE_REV_EVENT = "before revenue";
    public static final String POST_REV_EVENT = "done with revenue";
    public static final String FORCED_SALE_EVENT = "forcedSaleEvent";
    public static final String TRAIN_DROP_EVENT = "TrainDrop";
    public static final String ASK_REDEMPTION_EVENT = "AskRedemptionEvent";
    public static final String ASK_CGR_TRAIN_DROP = "CGRdrop";
    public static final String ASK_CGR_TOKENS = "CGRtokens";

    // private companies
    public static final String PRIVATE_FLOS = "flos";
    public static final String PRIVATE_WS = "ws";
    public static final String PRIVATE_CAN = "can";
    public static final String PRIVATE_GLS = "gls";
    public static final String PRIVATE_NIAG = "niag";
    public static final String PRIVATE_STC = "stc";
    public static final String CORP_CGR = "CGR";

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

    public enum Era { GATHER, AUCTION, INITIAL, STOCK, OP, DONE }

    synchronized public void loadMoves(List<TrainMove> moves) {
        history = moves;
        for(TrainMove move: moves) {
            loadMove(move);
        }
        recalculateStandings();
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
            case BANK_DIESEL -> doBankDiesel(move, rawMove);
            case TRADEIN -> doTradein(move, rawMove);
            case BUY_POOL_TRAIN -> doPoolTrain(move);
            case RUST -> doRust(move, rawMove);
            case RUST_TRAIN -> doRustTrain(move);
            case CLOSE_PRIVS -> doClosePriv(move, rawMove);
            case REMOVE_PRIV -> doRemovePriv(move);
            case REDEEM_LOAN -> doRedeemLoan(move);
            case END_OP_TURN -> doEndOpTurn(move, rawMove);
            case FORCED_BANK_TRAIN -> doForcedBankTrainBuy(move, rawMove);
            case FORCED_POOL_BUY -> doForcedPoolTrainBuy(move);
            case C2C_TRAIN_BUY -> doC2CtrainBuy(move);
            case ASK_TRAIN_DROP -> doAskTrainDrop(move, rawMove);
            case FORCED_SALE -> doForcedSale(move, rawMove);
            case NEXT_CORP -> doNextCorp(move);
            case START_CGR_REDEMPTIONS -> doStartCGRRedemptions(move, rawMove);
            case AUTOPAY_CGR_LOANS -> doAutopayCGR(move);
            case ASK_REDEMPTION -> doAskRedemption(move, rawMove);
            case REDEEM_FROM_CGR -> doRedeemFromCGR(move, rawMove);
            case ABANDON_TO_CGR -> doAbandonToCGR(move, rawMove);
            case ABANDON_CORP -> doAbandonCorp(move);
            case FORM_CGR -> doFormCGR(move, rawMove);
            case DROP_TRAIN -> doDropTrain(move);
            case DROP_CGR_TRAIN -> doDropCGRtrain(move, rawMove);
            case DONE_CGR_DROP, END_CGR_DROP -> doDoneCGRdrop(move);
            case UNFREEZE -> doUnfreeze();
            case SET_CGR_TOKENS -> doCGRtokens(move, rawMove);
            case DROP_PORT -> doDropPort(move);
            case END_OP_ROUND -> doEndOpRound(move);
            case CLEAR_BLOCKS -> doClearBlocks(move);
            case CGR_FOLD -> doCGRfold(move);
            case CGR_PAR -> doCGRpar(move);
            case CGR_ESCROW ->doCGRescrow(move);
            case CGR_CASH -> doCGRCash(move);
            case CGR_TOKEN -> doCGRToken(move);
            case CGR_TRAIN -> doCGRTrain(move);
            case TRADE -> doCGRTrade(move, false);
            case TRADE_PREZ -> doCGRTrade(move, true);
            case BANK_BREACH -> doBankBreach(move);
            default -> throw new IllegalStateException("unknown move action: "+move.getAction());
        }
        if (rawMove && board.getBankCash() < 0 && !board.isBankBreach()) {
            makeFollowMove(BANK_BREACH, "", "", 0);
        }
    }

    private void doDropCGRtrain(TrainMove move, boolean rawMove) {
        Corp c = findCorp(CORP_CGR);
        c.getTrains().remove(Integer.valueOf(move.getAmount()));
        board.getTrainPool().add(move.getAmount());
        board.getTrainPool().sort(null);
        if (rawMove) {
            if(c.getTrains().isEmpty() ||
                    (c.getTrains().size() < 4 && Collections.min(c.getTrains()) > 4)
            ) {
                makeFollowMove(END_CGR_DROP, board.getEvent(), "", 0);
            }
        }
    }

    private void undoDropCGRtrain(TrainMove move) {
        Corp c = findCorp(CORP_CGR);
        c.getTrains().add(move.getAmount());
        c.getTrains().sort(null);
        board.getTrainPool().remove(Integer.valueOf(move.getAmount()));
    }

    private void doRedeemLoan(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        payCorpToBank(c, 100);
        c.setLoans(c.getLoans() - 1);
        //TODO prevent forced buy after redemption
    }

    private void undoRedeemLoan(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        payBankToCorp(c, 100);
        c.setLoans(c.getLoans() + 1);
    }

    private void doPoolTrain(TrainMove move) {
        int size = move.getAmount();
        int price = TRAIN_PRICE[size];
        Corp c = findCorp(move.getCorp());
        payCorpToBank(c, price);
        board.getTrainPool().remove(Integer.valueOf(size));
        c.getTrains().add(size);
        c.getTrains().sort(null);
    }

    private void undoPoolTrain(TrainMove move) {
        int size = move.getAmount();
        int price = TRAIN_PRICE[size];
        Corp c = findCorp(move.getCorp());
        payBankToCorp(c, price);
        board.getTrainPool().add(size);
        board.getTrainPool().sort(null);
        c.getTrains().remove(Integer.valueOf(size));
    }

    private void doAutopayCGR(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        payCorpToBank(c, 100 * move.getAmount());
        c.setLoans(c.getLoans() - move.getAmount());
    }

    private void undoAutopayCGR(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        payBankToCorp(c, 100 * move.getAmount());
        c.setLoans(c.getLoans() + move.getAmount());
    }

    /**
     * ClosePriv and RemovePriv work as a pair.
     * ClosePriv records where the priv has been by creating Remove follows, then wipes the lists.
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
     * AMOUNT ==> positive from player, negative from corp (abs(x)-1 ==> priv.amount)
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
        if (move.getAmount() == 3) board.setCGRsize(Board1856.CGR_PENDING);
        if (move.getAmount() == 4) board.setDieselBought(true);
        for(Corp corp: board.getCorps()) {
            int count = 0;
            for(Integer train: corp.getTrains()) {
                if(train == move.getAmount()) count++;
            }
            for(int i=0; i<count; i++) {
                if (rawMove) makeFollowMove(RUST_TRAIN, "", corp.getName(), move.getAmount());
            }
        }
        if (rawMove) {
            int count = 0;
            for (Integer train : board.getTrainPool()) {
                if (train == move.getAmount()) count++;
            }
            for(int i=0; i<count; i++) {
                makeFollowMove(RUST_TRAIN, "", "", move.getAmount());
            }
        }
    }

    private void undoRust(TrainMove move) {
        if (move.getAmount() == 3) board.setCGRsize(Board1856.CGR_PRE_FORMATION);
        if (move.getAmount() == 4) board.setDieselBought(false);
        board.setEvent(POST_REV_EVENT);
    }

    private void doRustTrain(TrainMove move) {
        if(!move.getCorp().isEmpty()) {
            Corp c = findCorp(move.getCorp());
            c.getTrains().removeIf(x -> x == move.getAmount());
        } else {
            board.getTrainPool().removeIf(x -> x == move.getAmount());
        }
    }

    private void undoRustTrain(TrainMove move) {
        if (!move.getCorp().isEmpty()) {
            Corp c = findCorp(move.getCorp());
            c.getTrains().add(0, move.getAmount());
        } else {
            board.getTrainPool().add(0, move.getAmount());
        }
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
                if (stock.getCorp().equals(move.getCorp())) {
                    int amount = move.getAmount() * stock.getAmount();
                    if (board.getCGRsize() == 20 && stock.getCorp().equals(CORP_CGR)) amount /=2;
                    payBankToWallet(w, amount);
                }
            }
        }
    }

    private void undoPayout(TrainMove move) {
        board.setEvent(PRE_REV_EVENT);
        board.setTilePlayed(move.getPlayer().charAt(0) == 'T');
        board.setTokenPlayed(move.getPlayer().charAt(1) == 't');
        Corp c = findCorp(move.getCorp());
        payCorpToBank(c, c.getPoolShares() * move.getAmount());
        for(Wallet w: board.getWallets()) {
            for(Stock stock: w.getStocks()) {
                if (stock.getCorp().equals(move.getCorp())) {
                    int amount = move.getAmount() * stock.getAmount();
                    if (board.getCGRsize() == 20 && stock.getCorp().equals(CORP_CGR)) amount /=2;
                    payWalletToBank(w, amount);
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
        setNextOpCorp(rawMove);
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
            if(board.isBankBreach()) {
                board.setPhase(Era.DONE.name());
            } else {
                board.setPhase(Era.STOCK.name());
                board.setCurrentCorp("");
            }
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
        corp2price.put(c.getName(), c.getPrice().getPrice());
    }

    private void undoDropStockPrice(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        c.getPrice().drop(-move.getAmount());
        corp2price.put(c.getName(), c.getPrice().getPrice());
    }

   private void doStockStep(TrainMove move, boolean rawMove) {
       Corp c = findCorp(move.getCorp());
       switch (move.getAction()) {
           case PRICE_LEFT -> c.getPrice().left();
           case PRICE_RIGHT -> c.getPrice().right();
           case PRICE_DOWN -> c.getPrice().down();
           case PRICE_UP -> c.getPrice().up();
       }
       corp2price.put(c.getName(), c.getPrice().getPrice());
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
       corp2price.put(c.getName(), c.getPrice().getPrice());
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
            int tenths = (board.getCGRsize() == 20 && move.getCorp().equals(CORP_CGR)) ?
                    move.getAmount() / 2 : move.getAmount();
            int drop = c.getPrice().previewDrop(tenths);
            updatePrez(move.getCorp());
            if(c.getName().equals(CORP_CGR) && board.isCGRfreeze()) return;
            if (drop > 0) makeFollowMove(DROP_STOCK_PRICE, "", move.getCorp(), drop);
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
            case BANK_DIESEL -> undoBankDiesel(move);
            case TRADEIN -> undoTradein(move);
            case BUY_POOL_TRAIN -> undoPoolTrain(move);
            case REDEEM_LOAN -> undoRedeemLoan(move);
            case RUST -> undoRust(move);
            case RUST_TRAIN -> undoRustTrain(move);
            case CLOSE_PRIVS -> undoClosePriv(move);
            case REMOVE_PRIV -> undoRemovePriv(move);
            case END_OP_TURN -> undoEndOpTurn(move);
            case C2C_TRAIN_BUY -> undoC2CtrainBuy(move);
            case DROP_TRAIN -> undoDropTrain(move);
            case DROP_CGR_TRAIN -> undoDropCGRtrain(move);
            case DONE_CGR_DROP, END_CGR_DROP -> undoDoneCGRdrop(move);
            case UNFREEZE -> undoUnfreeze();
            case SET_CGR_TOKENS -> undoCGRtokens(move);
            case DROP_PORT -> undoDropPort(move);
            case FORCED_BANK_TRAIN -> undoForcedBankTrainBuy(move); //bank buy
            case FORCED_POOL_BUY -> undoForcedPoolTrainBuy(move);
            case ASK_TRAIN_DROP -> undoAskTrainDrop(move);
            case FORCED_SALE -> undoForcedSale(move);
            case NEXT_CORP -> undoNextCorp(move);
            case START_CGR_REDEMPTIONS -> undoStartCGRRedemptions(move);
            case AUTOPAY_CGR_LOANS -> undoAutopayCGR(move);
            case ASK_REDEMPTION -> undoAskRedemption(move);
            case REDEEM_FROM_CGR -> undoRedeemFromCGR(move);
            case ABANDON_TO_CGR -> undoAbandonToCGR(move);
            case ABANDON_CORP -> undoAbandonCorp(move);
            case FORM_CGR -> undoFormCGR(move);
            case END_OP_ROUND -> undoEndOpRound(move);
            case CLEAR_BLOCKS -> undoClearBlocks(move);
            case CGR_FOLD -> undoCGRfold(move);
            case CGR_PAR -> undoCGRpar(move);
            case CGR_ESCROW -> undoCGRescrow(move);
            case CGR_CASH -> undoCGRCash(move);
            case CGR_TOKEN -> undoCGRToken(move);
            case CGR_TRAIN -> undoCGRTrain(move);
            case TRADE -> undoCGRTrade(move, false);
            case TRADE_PREZ -> undoCGRTrade(move, true);
            case BANK_BREACH -> undoBankBreach(move);
            default -> { return false; }
        }
        return true;
    }

    private void doTradein(TrainMove move, boolean rawMove) {
        Corp c = findCorp(move.getCorp());
        c.getTrains().remove(Integer.valueOf(move.getAmount()));
        board.getTrainPool().add(move.getAmount());
        c.getTrains().add(DIESEL_TRAIN);
        c.getTrains().sort(null);
        payCorpToBank(c, 750);
        if (rawMove) {
            if (board.isCGRfreeze() && c.getName().equals(CORP_CGR)) {
                makeFollowMove(UNFREEZE, "", "", 0);
            }
            if (!board.isDieselBought()) {
                makeFollowMove(RUST, "", "", 4);
            }
        }
    }

    private void undoTradein(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        c.getTrains().remove(DIESEL_TRAIN);
        c.getTrains().add(move.getAmount());
        c.getTrains().sort(null);
        board.getTrainPool().remove(Integer.valueOf(move.getAmount()));
        payBankToCorp(c, 750);
    }

    private void doUnfreeze() {
        board.setCGRfreeze(false);
    }

    private void undoUnfreeze() {
        board.setCGRfreeze(true);
    }

    private void doCGRtokens(TrainMove move, boolean rawMove) {
        Corp cgr = findCorp(CORP_CGR);
        cgr.setTokensUsed(move.getAmount());
        board.setEvent(PRE_REV_EVENT);
        setNextOpCorp(rawMove);
    }

    private void undoCGRtokens(TrainMove move) {
        board.setEvent(ASK_CGR_TOKENS);
    }

    private void doBankBreach(TrainMove move) {
        board.setBankBreach(true);
    }

    private void undoBankBreach(TrainMove move) {
        board.setBankBreach(false);
    }

    private void doC2CtrainBuy(TrainMove move) {
        Corp buyer = getCurrentCorp();
        Corp seller = findCorp(move.getCorp());
        Integer size = Integer.valueOf(move.getPlayer());
        seller.getTrains().remove(size);
        buyer.getTrains().add(size);
        buyer.getTrains().sort(null);
        payCorpToCorp(buyer, seller, move.getAmount());
    }

    private void undoC2CtrainBuy(TrainMove move) {
        Corp buyer = getCurrentCorp();
        Corp seller = findCorp(move.getCorp());
        Integer size = Integer.valueOf(move.getPlayer());
        buyer.getTrains().remove(size);
        seller.getTrains().add(size);
        seller.getTrains().sort(null);
        payCorpToCorp(seller, buyer, move.getAmount());
    }

    private void doAskTrainDrop(TrainMove move, boolean rawMove) {
        board.setEvent(TRAIN_DROP_EVENT);
        board.setCurrentCorp(move.getCorp());
    }

    private void undoAskTrainDrop(TrainMove move) {
        board.setEvent(move.getPlayer());
    }

    private void doDoneCGRdrop(TrainMove move) {
        board.setEvent(ASK_CGR_TOKENS);
        Corp c = findCorp(CORP_CGR);
        for(Integer train:c.getTrains()) if(train > 4) return;
        board.setCGRfreeze(true);
    }

    private void undoDoneCGRdrop(TrainMove move) {
        board.setEvent(ASK_CGR_TRAIN_DROP);
        board.setCGRfreeze(false);
    }

    private void doDropPort(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        c.setPortRights(false);
    }

    private void undoDropPort(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        c.setPortRights(true);
    }

    //no do actions -- recording source of initial CGR assets so they can be returned
    private void doCGRfold(TrainMove move) {}
    private void doCGRCash(TrainMove move) {}
    private void doCGRToken(TrainMove move) {}
    private void doCGRTrain(TrainMove move) {}
    private void doCGRTrade(TrainMove move, boolean isPrez) {}
    private void doCGRpar(TrainMove move) {}
    private void doCGRescrow(TrainMove move) {}

    private int encodeLoanAndShares(Corp c) {
        return c.getLoans() +
                c.getPoolShares() * 100 +
                c.getBankShares() * 10000;
    }

    private void decodeLoanAndShares(Corp c, int x) {
        c.setLoans(x%100);
        c.setPoolShares(x%10000/100);
        c.setBankShares(x/10000);
    }

    private void undoCGRescrow(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        c.setEscrow(move.getAmount());
        c.setLastRun(Integer.parseInt(move.getPlayer()));
    }

    private void undoCGRpar(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        c.setPar(move.getAmount());
        decodeLoanAndShares(c, Integer.parseInt(move.getPlayer()));
    }

    private void undoCGRfold(TrainMove move) {
        Corp c = new Corp(move.getCorp(), 0);
        c.setPrez(move.getPlayer());
        c.setClosing(true);
        c.setPrice(decodePrice(move.getAmount()));
        c.setHasFloated(true);
        corp2price.put(c.getName(), c.getPrice().getPrice());
        board.getCorps().add(cgrStockIndex(c), c);
    }

    private void undoCGRCash(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        c.setCash(move.getAmount());
        decodeFlags(c, Integer.parseInt(move.getPlayer()));
    }

    private void undoCGRToken(TrainMove move){
        findCorp(move.getCorp()).setTokensUsed(move.getAmount());
        findCorp(move.getCorp()).setTokensMax(Integer.parseInt(move.getPlayer()));
    }
    private void undoCGRTrain(TrainMove move){
        findCorp(move.getCorp()).getTrains().add(0, move.getAmount());
    }

    private void undoCGRTrade(TrainMove move, boolean isPrez) {
        Wallet w = findWallet(move.getPlayer());
        w.getStocks().add(new Stock(move.getCorp(), move.getAmount(), isPrez));
    }

    private int encodeFlags(Corp c) {
        return  (c.isHasOperated() ? 1 : 0) +
                (c.isReachedDest() ? 2 : 0) +
                (c.isBridgeRights() ? 4 : 0) +
                (c.isTunnelRights() ? 8 : 0) +
                c.getFundingType() * 16;
    }

    private void decodeFlags(Corp c, int x) {
        c.setHasOperated((x&1) > 0);
        c.setReachedDest((x&2) > 0);
        c.setBridgeRights((x&4) > 0);
        c.setTunnelRights((x&8) > 0);
        c.setFundingType(x/16);
    }

    private void doStartCGRRedemptions(TrainMove move, boolean rawMove) {
        board.setEvent(ASK_REDEMPTION_EVENT);
        if (rawMove) for(Corp c: board.getCorps()) {
            if(c.getLoans() > 0 && c.getCash() >= 100) {
                int canPay = c.getCash() / 100;
                int willPay = Math.min(canPay, c.getLoans());
                makeFollowMove(AUTOPAY_CGR_LOANS, c.getPrez(), c.getName(), willPay);
            }
        }
    }

    private void undoStartCGRRedemptions(TrainMove move) {
        board.setEvent(PRE_REV_EVENT);
    }

    private void doAskRedemption(TrainMove move, boolean rawMove) {
        board.setCurrentPlayer(move.getPlayer());
    }

    private void undoAskRedemption(TrainMove move) {
        board.setCurrentPlayer(move.getCorp());
    }

    public static StockPrice calculateCGRpar(int total, int min, int size) {
        int avg = (size > 2) ? (total - min) / (size - 1) : (total / size);
        if (avg > 117) {
            int column = 2;
            for(int i = avg - 142; i > 0; i -= 25) {
                column ++;
            }
            return new StockPrice( 75 + 25*column, StockPrice.PAR_COLUMN + column, 0);
        } else if (avg > 104) {
            return new StockPrice( 110, StockPrice.PAR_COLUMN + 1, 0);
        } else {
            return new StockPrice(100, StockPrice.PAR_COLUMN, 0);
        }
    }

    private void doFormCGR(TrainMove move, boolean rawMove) {
        //calculate pool shares and share size
        int CGRpoolCount = 0;
        int CGRtotalCount = 0;
        Set<String> dead = new HashSet<>();
        Map<String, Integer> player2CGRcount = new HashMap<>();
        int CGRpriceTotal = 0;
        int CGRpriceMin = Integer.MAX_VALUE;
        int CGRcash = 0;
        boolean CGRtunnel = false;
        boolean CGRbridge = false;
        boolean hasOperated = false;
        List<Integer> CGRtrains = new ArrayList<>();
        for(Corp c : board.getCorps()) {
            if(c.isClosing()) {
                dead.add(c.getName());
                CGRpoolCount += c.getPoolShares();
                CGRtotalCount += c.getPoolShares();
                CGRpriceTotal += c.getPrice().getPrice();
                CGRpriceMin = Math.min(CGRpriceMin, c.getPrice().getPrice());
                CGRcash += c.getCash();
                if (rawMove) makeFollowMove(CGR_CASH, ""+encodeFlags(c), c.getName(), c.getCash());
                if (rawMove) makeFollowMove(CGR_TOKEN, ""+c.getTokensMax(), c.getName(), c.getTokensUsed());
                for(Integer train:c.getTrains()) {
                    if (rawMove) makeFollowMove(CGR_TRAIN, "", c.getName(), train);
                    CGRtrains.add(train);
                }
                if(c.isBridgeRights()) CGRbridge = true;
                if(c.isTunnelRights()) CGRtunnel = true;
                if(c.isHasOperated()) hasOperated = true;
            }
        }
        if(dead.isEmpty()) {
            board.setCGRsize(Board1856.CGR_ABSENT);
            setNextOpCorp(rawMove);
            return;
        }
        CGRtrains.sort(null);
        for(Wallet w: board.getWallets()) { //phase I record what will need to be restored
            int CGRcount = 0;
            for(Stock s:w.getStocks()) {
                if(dead.contains(s.getCorp())) {
                    CGRcount += s.getAmount();
                    if (rawMove) makeFollowMove(s.isPresident()? TRADE_PREZ : TRADE, w.getName(), s.getCorp(), s.getAmount());
                }
            }
            CGRtotalCount += CGRcount;
            if(CGRcount%2>0) {
                CGRpoolCount++;
            }
            if (CGRcount > 1) player2CGRcount.put(w.getName(), CGRcount/2);
        }

        board.setCGRsize(CGRtotalCount > 20 ? Board1856.CGR_TWENTY_SHARES : Board1856.CGR_TEN_SHARES);
        String currentPlayer = getCurrentCorp().getPrez();
        Wallet w = findWallet(currentPlayer);
        Wallet prez = null;
        int prezCount = 0;
        for(int i=0; i<board.getPlayers().size(); w = findWallet(nextPlayer(w.getName())),i++) {
            w.getStocks().removeIf(x -> dead.contains(x.getCorp())); //phase II -- actually delete
            if(player2CGRcount.containsKey(w.getName())) {
                int shares = player2CGRcount.get(w.getName());
                shareToWallet(w, "CGR", shares); //TODO CONST
                if(shares > prezCount) {
                    prez = w;
                    prezCount = shares;
                }
            }
        }

        for(Stock s:prez.getStocks()) {
            if(s.getCorp().equals("CGR")) s.setPresident(true);
        }

        if (rawMove) for(int i = board.getCorps().size() - 1; i >= 0; i--) {
            Corp c = board.getCorps().get(i);
            if(c.isClosing()) {
                makeFollowMove(CGR_ESCROW, ""+c.getLastRun(), c.getName(), c.getEscrow());
                makeFollowMove(CGR_PAR, ""+encodeLoanAndShares(c), c.getName(), c.getPar());
                makeFollowMove(CGR_FOLD, c.getPrez(), c.getName(), encodePrice(c.getPrice()));
            }
        }
        board.getCorps().removeIf(Corp::isClosing);

        //make CGR object
        int bankShares = (board.getCGRsize() * 2 - CGRtotalCount + 1) / 2;
        int poolShares = CGRpoolCount / 2;
        if(bankShares < 0) {
            if(poolShares + bankShares > 0) {
                poolShares = board.getCGRsize() + bankShares;
                bankShares = 0;
            } else { //not enough shares for players
                poolShares = 0;
                bankShares = 0;
            }
        }

        StockPrice par = calculateCGRpar(CGRpriceTotal, CGRpriceMin, dead.size());
        Corp cgr = new Corp("CGR", par.getPrice(), bankShares, par, poolShares, CGRcash, 0,
                10, -1, prez.getName(), Corp.CGR_TYPE, 0, 0,
                new ArrayList<>(), CGRtrains, false, CGRbridge, CGRtunnel,
                hasOperated, true, true, false);
        board.getCorps().add(newStockIndex(cgr), cgr);
        if(CGRtrains.contains(4) || CGRtrains.size() > 3) board.setEvent(ASK_CGR_TRAIN_DROP);
        else if (rawMove) makeFollowMove(END_CGR_DROP, board.getEvent(), "", 0);
    }

    private void undoFormCGR(TrainMove move) {
        board.setCurrentPlayer(move.getPlayer());
        board.setCurrentCorp(move.getCorp());
        board.setCGRsize(Board1856.CGR_PENDING);
        for(Wallet w: board.getWallets()) {
            w.getStocks().removeIf(x -> x.getCorp().equals("CGR"));
        }
        board.getCorps().removeIf(x -> x.getName().equals("CGR"));
    }

    private boolean canForceSell(Wallet w, Stock s) {
        if(w.getBlocks().contains(s.getCorp())) return false;
        Corp c = findCorp(s.getCorp());
        if (c.getPoolShares() >= 5) return false;
        if (s.isPresident()) {
            int rival = topRivalShares(c);
            if (rival == s.getAmount() && s.getCorp().equals(board.getCurrentCorp())) return false;
            if (rival < 2 && s.getAmount() == 2) return false;
        }
        return true;
    }

    public void doForcedSale(TrainMove move, boolean rawMove) {
        Corp c = findCorp(move.getCorp());
        Wallet w = findWallet(move.getPlayer());
        w.getBlocks().add(c.getName());
        sharesWalletToPool(w, c, move.getAmount());
        payBankToWallet(w, c.getPrice().getPrice() * move.getAmount());
        if(rawMove && (!board.isCGRfreeze() || !c.getName().equals(CORP_CGR))) {
            int drop = c.getPrice().previewDrop(move.getAmount());
            if (drop > 0) makeFollowMove(DROP_STOCK_PRICE, "", move.getCorp(), drop);
            updatePrez(move.getCorp());
            makeFollowMove(REORDER_CORP, "", move.getCorp(), board.getCorps().indexOf(c));
        }
        if (w.getCash() > 0) {
            board.setEvent(POST_REV_EVENT);
            if (rawMove) makeFollowMove(CLEAR_BLOCKS, w.getName(), String.join(" ", w.getBlocks()), 0);
        } else {
            checkBankrupt(w);
        }
    }

    private void checkBankrupt(Wallet w) {
        boolean bankrupt = true;
        for (Stock s: w.getStocks()) {
            if (canForceSell(w, s)) {
                bankrupt = false;
            }
        }
        if(bankrupt) board.setPhase(Era.DONE.name()); //TODO make undoable
    }

    public void undoForcedSale(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        Wallet w = findWallet(move.getPlayer());
        w.getBlocks().remove(c.getName());
        sharesPoolToWallet(w, c, move.getAmount());
        payWalletToBank(w, c.getPrice().getPrice() * move.getAmount());
        board.setEvent(FORCED_SALE_EVENT);
    }

    public void doDropTrain(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        board.getTrainPool().add(c.getTrains().remove(move.getAmount()));
        board.getTrainPool().sort(null);
        boolean noMoreDrops = true;
        boolean CGRdrop = false;
        for(Corp corp: board.getCorps()) {
            if(corp.getName().equals(CORP_CGR)) continue; // different rules for CGR
            if(corp.getTrains().size() > trainLimit(board, corp)) {
                if(corp.getName().equals(CORP_CGR)) CGRdrop = true;
                else noMoreDrops = false;
            }
        }
        if (noMoreDrops) {
            board.setEvent(CGRdrop ? ASK_CGR_TRAIN_DROP : POST_REV_EVENT);
        }
    }

    public void undoDropTrain(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        c.getTrains().add(move.getAmount(), board.getTrainPool().remove(0));
        board.setEvent(TRAIN_DROP_EVENT);
    }

    public void doForcedBankTrainBuy(TrainMove move, boolean rawMove) {
        Corp c = getCurrentCorp();
        Wallet w = findWallet(move.getPlayer());
        payCorpToBank(c, c.getCash());
        payWalletToBank(w, move.getAmount());
        int size = board.getTrains().get(0); //TODO buy D if empty
        c.getTrains().add(0, size);
        board.getTrains().remove(0);
        if (rawMove) {
            switch(board.getTrains().size()) {
                case 1: prepCGR(); break;
                case 4: makeFollowMove(CLOSE_PRIVS, "", "", 0); break;
                case 8: makeFollowMove(RUST, "", "", 2); break;
            }
            for(Corp cc: board.getCorps()) {
                if(cc.getName().equals(CORP_CGR)) continue;
                if(cc.getTrains().size() > trainLimit(board, cc)) { //TODO auto-choose if all the same
                    makeFollowMove(ASK_TRAIN_DROP, board.getEvent(), c.getName(), 0);
                }
            }
        }
        if (w.getCash() < 0) {
            board.setEvent(FORCED_SALE_EVENT);
            checkBankrupt(w);
        }
    }

    public void doForcedPoolTrainBuy(TrainMove move) {
        Corp c = getCurrentCorp();
        Wallet w = findWallet(move.getPlayer());
        payCorpToBank(c, c.getCash());
        payWalletToBank(w, move.getAmount());

        int size = board.getTrainPool().get(0);
        c.getTrains().add(size);
        c.getTrains().sort(null);
        board.getTrainPool().remove(Integer.valueOf(size));
        if (w.getCash() < 0) {
            board.setEvent(FORCED_SALE_EVENT);
            checkBankrupt(w);
        }
    }

    public void undoForcedPoolTrainBuy(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        Wallet w = findWallet(move.getPlayer());
        payBankToWallet(w, move.getAmount());
        int size = c.getTrains().get(0);
        c.getTrains().clear();
        board.getTrainPool().add(size);
        board.getTrainPool().sort(null);
        payBankToCorp(c, TRAIN_PRICE[size] - move.getAmount());
        board.setEvent(POST_REV_EVENT);
    }

    public void undoForcedBankTrainBuy(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        Wallet w = findWallet(move.getPlayer());
        payBankToWallet(w, move.getAmount());
        int size = c.getTrains().get(0);
        c.getTrains().clear();
        board.getTrains().add(0, size);
        payBankToCorp(c, TRAIN_PRICE[size] - move.getAmount());
        board.setEvent(POST_REV_EVENT);
    }

    public void doClearBlocks(TrainMove move) {
        Wallet w = findWallet(move.getPlayer());
        w.setBlocks(new ArrayList<>());
    }

    public void undoClearBlocks(TrainMove move) {
        Wallet w = findWallet(move.getPlayer());
        w.setBlocks(new ArrayList<>(Arrays.asList(move.getCorp().split(" "))));
    }

    public void doPrezInterest(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        Wallet w = findWallet(c.getPrez());
        board.setTilePlayed(false);
        board.setTokenPlayed(false);
        payWalletToBank(w, move.getAmount());
        if (w.getCash() < 0) {
            board.setEvent(FORCED_SALE_EVENT);
            checkBankrupt(w);
        }
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
            if(move.getAmount() > 4 && c.getName().equals(CORP_CGR) && board.isCGRfreeze()) {
                makeFollowMove(UNFREEZE, "", "", 0);
            }
            switch(board.getTrains().size()) {
                case 1: prepCGR(); break;
                case 4: makeFollowMove(CLOSE_PRIVS, "", "", 0); break;
                case 8: makeFollowMove(RUST, "", "", 2); break;
            }
            for(Corp cc: board.getCorps()) { //TODO general limit
                if(cc.getTrains().size() > trainLimit(board, cc)) { //TODO auto-choose if all the same
                    makeFollowMove(ASK_TRAIN_DROP, board.getEvent(), c.getName(), 0);
                }
            }
        }
    }

    private void prepCGR() {
        makeFollowMove(RUST, "", "", 3);
        for(Corp c: board.getCorps()) {
            if(c.isPortRights()) {
                makeFollowMove(DROP_PORT, "", c.getName(), 0);
            }
        }
    }

    public void doBankDiesel(TrainMove move, boolean rawMove) {
        Corp c = getCurrentCorp();
        c.getTrains().add(DIESEL_TRAIN);
        payCorpToBank(c, 1100);
        if (rawMove) {
            if(board.isCGRfreeze() && move.getCorp().equals(CORP_CGR)) {
                makeFollowMove(UNFREEZE, "", "", 0);
            }
            if(!board.isDieselBought()) {
                makeFollowMove(RUST, "", "", 4);
            }
        }
    }

    public void undoBankDiesel(TrainMove move) {
        Corp c = getCurrentCorp();
        c.getTrains().remove(Integer.valueOf(DIESEL_TRAIN));
        payBankToCorp(c, 1100);
    }

    public void undoBankTrain(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        payBankToCorp(c, TRAIN_PRICE[move.getAmount()]);
        board.getTrains().add(0, move.getAmount());
        c.getTrains().remove(Integer.valueOf(move.getAmount()));
        board.setEvent(POST_REV_EVENT);
    }

    public void doDestination(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        payBankToCorp(c, move.getAmount());
        c.setEscrow(0);
        c.setReachedDest(true);
        c.setFundingType(Corp.INCREMENTAL_TYPE);
        board.setBankCash(board.getBankCash() - move.getAmount());
    }

    public void undoDestination(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        payCorpToBank(c, move.getAmount());
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
            boolean prez = w.getName().equals(c.getPrez());
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
            payBankToCorp(c, amount*10);
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
        return switch (board.getTrains().size()) {
            case 0, 1 -> board.isDieselBought() ? 8 : 6;
            case 2, 3, 4 -> 5;
            case 5, 6, 7, 8 -> 4;
            case 9, 10, 11, 12, 13 -> 3;
            default -> 2;
        };
    }

    private int currentFloatType() {
        return switch (availableTrainLevel()) {
            case 2, 3, 4 -> Corp.DESTINATION_TYPE;
            case 5 -> Corp.INCREMENTAL_TYPE;
            default -> Corp.ALL_AT_ONCE_TYPE;
        };
    }

    private void undoSetPar(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        c.setPar(0);
        corp2price.remove(c.getName());
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
            if (c2.getPrice() == null) continue;
            int p = c.getPrice().getPrice();
            int p2 = c2.getPrice().getPrice();
            if (p2 < p) continue;
            if (p2 == p && c2.getPrice().getX() < c.getPrice().getX()) continue;
            return i+1;
        }
        return 0;
    }

    private int cgrStockIndex(Corp c) {
        int i = board.getCorps().size();
        while (i > 0) {
            i--;
            Corp c2 = board.getCorps().get(i);
            if (c2.getPrice() == null) continue;
            int p = c.getPrice().getPrice();
            int p2 = c2.getPrice().getPrice();
            if (p2 < p) continue;
            if (p2 == p && c2.getPrice().getX() <= c.getPrice().getX()) continue;
            return i+1;
        }
        return 0;
    }

    private void doSetPar(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        c.setPar(move.getAmount());
        corp2price.put(c.getName(), move.getAmount());
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
        return switch (highestTrainSold()) {
            case 2 -> 1;
            case 3, 4 -> 2;
            default -> 3;
        };
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
        setNextOpCorp(rawMove);
    }

    private String nextPlayer(String currentPlayer) {
        int index = board.getPlayers().indexOf(currentPlayer) + 1;
        if (index >= board.getPlayers().size()) index = 0;
        return board.getPlayers().get(index);
    }

    private void makeRedemptionMove() {
        Set<String> needsWork = new HashSet<>();
        for(Corp c: board.getCorps()) {
            if(c.isClosing() || c.getLoans() == 0) continue;
            needsWork.add(c.getPrez());
        }
        if(needsWork.isEmpty()) {
            makeFollowMove(FORM_CGR, board.getCurrentPlayer(), board.getCurrentCorp(), 0);
        } else {
            String player = getCurrentCorp().getPrez();
            while(!needsWork.contains(player)) player = nextPlayer(player);
            makeFollowMove(ASK_REDEMPTION, player, board.getCurrentPlayer(), 0);
        }
    }

    private void setNextOpCorp(boolean rawMove) {
        if (!rawMove) return;
        if (board.getCGRsize() == Board1856.CGR_PENDING) {
            String prez = getCurrentCorp().getPrez();
            int prezIndex = board.getPlayers().indexOf(prez);
            makeFollowMove(START_CGR_REDEMPTIONS, board.getCurrentPlayer(), board.getCurrentCorp(), prezIndex);
            makeRedemptionMove();
            return;
        }
        for (Corp corp: board.getCorps()) {
            if (corp.isHasOperated()) continue;
            if (!corp.isHasFloated()) {
                int min = availableTrainLevel();
                if (10 - corp.getBankShares() < min) {
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
        recalculateStandings();
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

    public Board1856 redo() { return redo(true); }

    synchronized public Board1856 redo(boolean recalc) {
        if (board.getUndoCount() < 1) return board;
        TrainMove currentMove = history.get(board.getMoveNumber()-board.getUndoCount());
        doMove(currentMove, false);
        board.setUndoCount(board.getUndoCount()-1);
        if (board.getUndoCount() > 0) {
            TrainMove nextMove = history.get(board.getMoveNumber() - board.getUndoCount());
            if (nextMove.isFollow()) return redo(recalc);
        }
        if(recalc) recalculateStandings();
        return board;
    }

    synchronized public Board1856 redoAll() {
        while (board.getUndoCount() > 0) {
            redo(false);
        }
        recalculateStandings();
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
        Corp c = maybeFindCorp(name);
        if (c == null) throw new IllegalStateException("Unknown Corp: "+name);
        return c;
    }

    private Corp maybeFindCorp(String name) {
        for (Corp c: board.getCorps()) {
            if(c.getName().equals(name)) return c;
        }
        return null;
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
        makePrimaryMove(STOCK_SET_PAR, board.getCurrentPlayer(), corpName, par);
        makeFollowMove(END_STOCK_ACTION, board.getCurrentPlayer(), "", 0);
        return board;
    }

    private void canBuyBank(Corp c, Wallet w, int extraCash) {
        enforcePhase(Era.STOCK, Era.INITIAL);
        if (c.getBankShares() < 1) throw new IllegalStateException("No Bank Shares Remain");
        if (w.getCash() + extraCash < c.getPar()) throw new IllegalStateException(FUNDS);
        if (w.getBlocks().contains(c.getName())) throw new IllegalStateException("No buy after sell same round");
        for (Stock s: w.getStocks()) {
            if(!s.getCorp().equals(c.getName())) continue;
            if(s.getAmount() >= 6 && c.getPrice().getPrice() > 40) {
                throw new IllegalStateException("Max 60% of company");
            }
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
        if (certCount(w) + ((isPar) ? 2 : 1) > certLimit() + extraCerts) {
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
            if(corp2price.get(s.getCorp()) <= 50) continue; // yellow and brown stock don't count
            if(board.getCGRsize() > 10 && s.getCorp().equals(CORP_CGR)) {
                out += (s.getAmount() + 1) / 2;
            } else {
                out += s.getAmount();
            }
            if (s.isPresident()) out --;
        }
        System.out.println(w.getName()+":"+out);
        return out + w.getPrivates().size();
    }

    /**
     * for use previewing a stock sale during the stock round. will not work for forced sales
     */
    private boolean prezWillChange(StockSale sale) {
        Corp c = findCorp(sale.getName());
        if(!board.getCurrentPlayer().equals(c.getPrez())) return false;
        Wallet w = findWallet(board.getCurrentPlayer());
        for(Stock stock: w.getStocks()) {
            if(sale.getName().equals(stock.getCorp())) {
                int rival = topRivalShares(c);
                int after = stock.getAmount() - sale.getAmount();
                return rival > after;
            }
        }
        return false;
    }

    private int certValue(List<StockSale> sales) {
        int out = 0;
        for(StockSale s: sales) {
            out += s.getAmount();
            if (prezWillChange(s)) out--;
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
        if(c.getName().equals(CORP_CGR) && board.isCGRfreeze()) return board;
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
        if(c.getName().equals(CORP_CGR) && board.isCGRfreeze()) return board;
        if(c.getPrice().leftEdge()) {
            if (!c.getPrice().floor()) { //TODO check closure (where?) price == 30, not floor?
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

    synchronized public Board1856 buyPoolTrain(int size) {
        enforcePhase(Era.OP);
        enforceEvent(POST_REV_EVENT);
        if (!board.getTrainPool().contains(size)) throw new IllegalStateException("Pool has no trains size "+size);
        Corp c = getCurrentCorp();
        if(c.getCash() < TRAIN_PRICE[size]) throw new IllegalStateException(FUNDS);
        if(c.getTrains().size() >= trainLimit(board, c)) throw new IllegalStateException("Too many trains");
        makePrimaryMove(BUY_POOL_TRAIN, "", c.getName(), size);
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
        enforcePhase(Era.OP);
        if(c.isBridgeRights()) throw new IllegalStateException("Already have bridge rights");
        if(c.getCash() < 50) throw new IllegalStateException(FUNDS);
        if(board.getBridgeTokens() < 1) throw new IllegalStateException("No more bridge tokens");
        makePrimaryMove(BUY_BRIDGE, "", c.getName(), 0);
    }

    private void buyTunnel(Corp c) {
        enforcePhase(Era.OP);
        if(c.isTunnelRights()) throw new IllegalStateException("Already have tunnel rights");
        if(c.getCash() < 50) throw new IllegalStateException(FUNDS);
        if(board.getTunnelTokens() < 1) throw new IllegalStateException("No Tunnel Tokens Left");
        makePrimaryMove(BUY_TUNNEL, "", c.getName(), 0);
    }

    public Board1856 forcedBankBuy() {
        enforcePhase(Era.OP);
        enforceEvent(POST_REV_EVENT);
        Corp c = getCurrentCorp();
        if (!c.getTrains().isEmpty()) throw new IllegalStateException("Prez may not contribute voluntarily");
        int size = (board.getTrains().isEmpty()) ? 8 : board.getTrains().get(0);
        for(Integer poolTrain: board.getTrainPool()) {
            if(poolTrain < size) throw new IllegalStateException("Must buy smaller train in pool");
        }
        int price =  TRAIN_PRICE[size];
        int remainder = price - c.getCash();
        if (remainder < 0) throw new IllegalStateException("Buy a train normally before ending your turn");
        makePrimaryMove(FORCED_BANK_TRAIN, c.getPrez(), c.getName(), remainder);
        return board;
    }

    public Board1856 forcedPoolBuy() {
        enforcePhase(Era.OP);
        enforceEvent(POST_REV_EVENT);
        Corp c = getCurrentCorp();
        if (!c.getTrains().isEmpty()) throw new IllegalStateException("Prez may not contribute voluntarily");
        if (board.getTrainPool().isEmpty()) throw new IllegalStateException("No trains in pool");
        int size = board.getTrainPool().get(0);
        int price = TRAIN_PRICE[size];
        int remainder = price - c.getCash();
        if (remainder < 0) throw new IllegalStateException("Buy a train normally before ending your turn");
        makePrimaryMove(FORCED_POOL_BUY, c.getPrez(), c.getName(), remainder);
        return board;
    }

    public Board1856 dropTrain(String corpName, int size) {
        enforcePhase(Era.OP);
        enforceEvent(TRAIN_DROP_EVENT);
        Corp c = findCorp(corpName);
        int index = c.getTrains().indexOf(size);
        if (index < 0) throw new IllegalStateException("No train of that size to drop");
        makePrimaryMove(DROP_TRAIN, "", corpName, index);
        return board;
    }

    private int topRivalShares(Corp c) {
        int max = 0;
        for(Wallet w: board.getWallets()) {
            if (w.getName().equals(c.getPrez())) continue;
            for(Stock s:w.getStocks()) {
                if(s.getCorp().equals(c.getName())) {
                    if(s.getAmount() > max) max = s.getAmount();
                }
            }
        }
        return max;
    }

    public Board1856 forcedSale(String corpName, int amount) {
        enforceEvent(FORCED_SALE_EVENT);
        Corp c = findCorp(corpName);
        Wallet w = findWallet(getCurrentCorp().getPrez());
        if(w.getBlocks().contains(corpName)) throw new IllegalStateException("Cannot split sale into two blocks-undo?");
        for(Stock s:w.getStocks()) {
            if(s.getCorp().equals(corpName)) {
                if (corpName.equals(board.getCurrentCorp()) && (topRivalShares(c) > s.getAmount() - amount)) {
                    throw new IllegalStateException("Cannot change debtor Prez");
                }
                if (amount > s.getAmount()) throw new IllegalStateException("Not enough shares");
                if (c.getPoolShares() + amount > 5) throw new IllegalStateException("max 50% in pool");
                if(s.isPresident() && s.getAmount() - amount < 2) {
                    boolean noPrez = true;
                    for (Wallet w2: board.getWallets()) {
                        if(w2 == w) continue;
                        for (Stock ss: w2.getStocks()) {
                            if (ss.getCorp().equals(corpName)) {
                                if(ss.getAmount() > 1) noPrez = false;
                            }
                        }
                    }
                    if(noPrez) throw new IllegalStateException("No one to become president of "+corpName);
                }
            }
        }
        int value = c.getPrice().getPrice() * amount;
        int overage = w.getCash() + value;
        if (overage > c.getPrice().getPrice()) throw new IllegalStateException("Extra sales not allowed");
        makePrimaryMove(FORCED_SALE, w.getName(), c.getName(), amount);
        return board;
    }

    private void recalculateStandings() {
        for(Wallet w: board.getWallets()) {
            int value = w.getCash();
            for(Priv p: w.getPrivates()) value += priv2price.get(p.getCorp());
            for(Stock s: w.getStocks()) {
                Corp c = findCorp(s.getCorp());
                value += (c.getPrice().getPrice() - 10 * c.getLoans()) * s.getAmount();
            }
            w.setValue(value);
        }
    }

    public Board1856 redeemFromCGR(String corpName) {
        Corp c = findCorp(corpName);
        Wallet w = getCurrentWallet();

        if(!w.getName().equals(c.getPrez())) throw new IllegalStateException("Not your turn");
        if(c.isClosing()) throw new IllegalStateException("Already abandoned -- undo?");
        if(c.getLoans() == 0) throw new IllegalStateException("No loans to redeem");

        int debt = 100 * c.getLoans();
        int assets = w.getCash() + c.getCash();
        if (debt > assets) throw new IllegalStateException(FUNDS);
        makePrimaryMove(REDEEM_FROM_CGR, ""+c.getCash(), c.getName(), c.getLoans());
        return board;
    }

    public Board1856 abandonToCGR() {
        makePrimaryMove(ABANDON_TO_CGR, board.getCurrentPlayer(), "", 0);
        return board;
    }

    private void doAbandonToCGR(TrainMove move, boolean rawMove) {
        if(!rawMove) return;
        boolean first = true;
        for(Corp c: board.getCorps()) {
            if(c.getPrez().equals(board.getCurrentPlayer())) {
                if(c.isClosing()) continue;
                if(c.getLoans() == 0) continue;
                makeFollowMove(ABANDON_CORP, c.getPrez(), c.getName(), 0);
            }
        }
        makeRedemptionMove();
    }

    private void undoAbandonToCGR(TrainMove move) {
        board.setCurrentPlayer(move.getPlayer());
        board.setEvent(ASK_REDEMPTION_EVENT);
    }

    private void doRedeemFromCGR(TrainMove move, boolean rawMove) {
        Corp c = findCorp(move.getCorp());
        Wallet w = findWallet(board.getCurrentPlayer());
        int corpShare = Integer.parseInt(move.getPlayer());
        payCorpToBank(c, corpShare); //should equal corp cash
        payWalletToBank(w, 100 * move.getAmount() - corpShare);
        c.setLoans(0);
    }

    private void undoRedeemFromCGR(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        Wallet w = findWallet(c.getPrez());
        int corpShare = Integer.parseInt(move.getPlayer());
        payBankToCorp(c, corpShare);
        payBankToWallet(w, 100 * move.getAmount() - corpShare);
        c.setLoans(move.getAmount());
    }

    private void doAbandonCorp(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        c.setClosing(true);
    }

    private void undoAbandonCorp(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        c.setClosing(false);
    }

    public Board1856 redeemLoan() {
        enforcePhase(Era.OP);
        Corp c = getCurrentCorp();
        if(c.getLoans() == 0) throw new IllegalStateException("No loans to redeem");
        if(c.getTrains().isEmpty()) throw new IllegalStateException("Buy a train first");
        if(c.getCash() < 100) throw new IllegalStateException(FUNDS);
        makePrimaryMove(REDEEM_LOAN, "", c.getName(), 0);
        return board;
    }

    public Board1856 dropCGRtrain(int size) {
        enforcePhase(Era.OP);
        enforceEvent(ASK_CGR_TRAIN_DROP);
        Corp c = findCorp(CORP_CGR);
        if(!c.getTrains().contains(size)) throw new IllegalStateException("No such train to drop");
        if(size >= 5) {
            if(Collections.min(c.getTrains()) < 5) throw new IllegalStateException("Non-permanent trains first");
            if(c.getTrains().size() < 4) throw new IllegalStateException("No voluntary drops of permanent trains");
        }
        makePrimaryMove(DROP_CGR_TRAIN, "", CORP_CGR, size);
        return board;
    }

    public Board1856 stopCGRdrop() {
        enforcePhase(Era.OP);
        enforceEvent(ASK_CGR_TRAIN_DROP);
        Corp c = findCorp(CORP_CGR);
        if (c.getTrains().size() > 3) throw new IllegalStateException("Too many trains");
        makePrimaryMove(DONE_CGR_DROP, "", CORP_CGR, 0);
        return board;
    }

    public Board1856 c2cBuy(String sellCorpName, int size, int price) {
        enforcePhase(Era.OP);
        Corp buyer = getCurrentCorp();
        Corp seller = findCorp(sellCorpName);
        boolean cgrInvoled = buyer.getName().equals(CORP_CGR) || seller.getName().equals(CORP_CGR);
        if (cgrInvoled && price != TRAIN_PRICE[size]) throw new IllegalStateException("CGR must use face price");
        if(!seller.getTrains().contains(size)) throw new IllegalStateException("Train not found");
        if(buyer.getCash() < price) throw new IllegalStateException(FUNDS);
        makePrimaryMove(C2C_TRAIN_BUY, ""+size, sellCorpName, price);
        return board;
    }

    public Board1856 CGRtoken(int amount) {
        enforcePhase(Era.OP);
        enforceEvent(ASK_CGR_TOKENS);
        if (amount < 1) throw new IllegalStateException("Must be at least one token");
        if (amount > 10) throw new IllegalStateException("Cannot be more than 10 tokens");
        makePrimaryMove(SET_CGR_TOKENS, "", "", amount);
        return board;
    }

    public Board1856 bankDiesel() {
        enforcePhase(Era.OP);
        enforceEvent(POST_REV_EVENT);
        if(board.getTrains().size() > 1) throw new IllegalStateException("Diesels not available");
        Corp c = getCurrentCorp();
        if (c.getCash() < 1100) throw new IllegalStateException(FUNDS);
        if (c.getTrains().size() >= trainLimit(board, c)) throw new IllegalStateException("Too many trains");
        makePrimaryMove(BANK_DIESEL, "", c.getName(), 0);
        return board;
    }

    public Board1856 tradein(int size) {
        enforcePhase(Era.OP);
        enforceEvent(POST_REV_EVENT);
        Corp c = getCurrentCorp();
        if(!c.getTrains().contains(size)) throw new IllegalStateException("No such train");
        if(c.getCash() < 750) throw new IllegalStateException(FUNDS);
        makePrimaryMove(TRADEIN, "", c.getName(), size);
        return board;
    }
}