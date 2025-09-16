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
    public static final String STOCK_FOOTER = "stockFooter";
    public static final String DROP_STOCK_PRICE = "dropStockPrice";
    public static final String REORDER_CORP = "reorderCorp";
    public static final String STOCK_END_ROUND = "stockEndRound";

    public static final String UPDATE_PREZ = "updatePrez";

    public static final String TAKE_LOAN = "takeLoan";
    public static final String BUY_PRIV = "buyPriv";
    public static final String FLOAT = "float";
    public static final String FAIL_FLOAT = "failFloat";
    public static final String NEXT_CORP = "nextCorp";
    public static final String END_OP_ROUND = "endOpRound";

    // event constants
    public static final String NORMAL_EVENT = "";
    public static final String BIDOFF_EVENT = "bidoff";
    public static final String PRE_REV_EVENT = "PRE_REV";
    public static final String POST_REV_EVENT = "POST_REV";

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
        makeFollowMove(STOCK_FOOTER, board.getCurrentPlayer(), "", 0);
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
        int level = currentTrainLevel();
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
                if(priv.getAmount() > 3) continue;  //TODO check for closed
                payBankToWallet(w, priv2div.get(priv.getCorp()));
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
                if(priv.getAmount() > 3) continue; //TODO check for closed
                payWalletToBank(w, priv2div.get(priv.getCorp()));
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
            case ADD_PLAYER:
                board.getPlayers().add(move.getPlayer());
                break;
            case RENAME_PLAYER:
                board.getPlayers().set(move.getAmount(), move.getPlayer());
                break;
            case START_GAME:
                doStart(move.getCorp());
                break;
            case AUCTION_BUY:
                doAuctionBuy(move, rawMove);
                break;
            case AUCTION_PASS:
                doAuctionPass(rawMove);
                break;
            case AUCTION_BID:
                doAuctionBid(move, rawMove);
                break;
            case AUCTION_REBID:
                doAuctionRebid(move, rawMove);
                break;
            case AUCTION_LONEBUY:
                doLoneBuy(move, rawMove);
                break;
            case AUCTION_START_BIDOFF:
                doStartBidoff(move);
                break;
            case AUCTION_END_BIDOFF:
                doEndBidoff(move, rawMove);
                break;
            case AUCTION_END_ROUND:
                doAuctionEndRound(move, rawMove);
                break;
            case AUCTION_AWARD_BID:
                doAwardBid(move);
                break;
            case AUCTION_REFUND_BID:
                doRefundBid(move);
                break;
            case AUCTION_GIVEAWAY:
                doAuctionGiveaway(move, rawMove);
                break;
            case AUCTION_END_PHASE:
                doEndAuctionPhase(move);
                break;
            case AUCTION_DISCOUNT_RESET:
                doAuctionDiscountReset();
                break;
            case STOCK_PASS:
                doStockPass(move, rawMove);
                break;
            case STOCK_END_ROUND:
                doEndStockRound(move, rawMove);
                break;
            case STOCK_SET_PAR:
                doSetPar(move, rawMove);
                break;
            case STOCK_BUY_BANK:
                doBuyBank(move, rawMove);
                break;
            case STOCK_BUY_POOL:
                doBuyPool(move, rawMove);
                break;
            case UPDATE_PREZ:
                doUpdatePrez(move);
                break;
            case STOCK_SALE:
                doStockSale(move, rawMove);
                break;
            case STOCK_HEADER:
                break; //nothing to do -- this just anchors the sale list
            case DROP_STOCK_PRICE:
                doDropStockPrice(move, rawMove);
                break;
            case REORDER_CORP:
                doReorderCorp(move);
                break;
            case STOCK_FOOTER:
                doStockFooter(move, rawMove);
                break;
            case TAKE_LOAN:
                doTakeLoan(move);
                break;
            case BUY_PRIV:
                doBuyPriv(move);
                break;
            case FLOAT:
                doFloat(move);
                break;
            case FAIL_FLOAT:
                doFailFloat(move);
                break;
            case NEXT_CORP:
                doNextCorp(move);
                break;
            case END_OP_ROUND:
                doEndOpRound(move);
                break;
            default:
                throw new IllegalStateException("unknown move action: "+move.getAction());
        }
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

    private void doNextCorp(TrainMove move) {
        board.setLoanTaken(false);
        board.setCurrentCorp(move.getCorp());
    }

    private void undoNextCorp(TrainMove move) {
        board.setLoanTaken(move.getAmount() > 0);
        board.setCurrentCorp(move.getPlayer());
    }

    private void doEndOpRound(TrainMove move) {
        board.setCurrentCorp("");
        board.setLoanTaken(false);
        if(board.getCurrentOpRound() == board.getMaxOpRounds()) {
            board.setPhase(Era.STOCK.name());
        } else {
            board.setCurrentOpRound(board.getCurrentOpRound() + 1);
        }
    }

    private void undoEndOpRound(TrainMove move) {
        board.setCurrentCorp(move.getCorp());
        board.setLoanTaken(move.getAmount() > 0);
        if (board.getPhase().equals(Era.OP.name())) {
            board.setCurrentOpRound(board.getCurrentOpRound() - 1);
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
    }

    private void undoBuyPriv(TrainMove move) {
        Corp c = getCurrentCorp();
        Wallet w = findWallet(move.getPlayer());
        payWalletToCorp(w, c, move.getAmount());
        c.getPrivates().removeIf(x -> x.getCorp().equals(move.getCorp()));
        w.getPrivates().add(new Priv(move.getCorp(), 3));
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
    }

    private void undoDropStockPrice(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        findCorp(move.getCorp()).getPrice().drop(-move.getAmount());
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
    }

    private void doUpdatePrez(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        c.setPrez(move.getPlayer());
    }

    private void undoUpdatePrez(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        c.setPrez(board.getPlayers().get(move.getAmount()));
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
    private void  incrementStockPlayer(boolean actionTaken, boolean rawMove) {
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
            case ADD_PLAYER:
                board.getPlayers().remove(board.getPlayers().size()-1);
                return true;
            case RENAME_PLAYER:
                board.getPlayers().set(move.getAmount(), move.getCorp());
                return true;
            case START_GAME:
                undoStart(move.getCorp());
                return true;
            case AUCTION_BUY:
                undoAuctionBuy(move);
                return true;
            case AUCTION_LONEBUY:
                undoLoneBuy(move);
                return true;
            case AUCTION_PASS:
                undoAuctionPass(move);
                return true;
            case AUCTION_BID:
                undoAuctionBid(move);
                return true;
            case AUCTION_REBID:
                undoAuctionRebid(move);
                return true;
            case AUCTION_START_BIDOFF:
                undoStartBidoff(move);
                return true;
            case AUCTION_END_BIDOFF:
                undoEndBidoff(move);
                return true;
            case AUCTION_END_ROUND:
                undoAuctionEndRound(move);
                return true;
            case AUCTION_AWARD_BID:
                undoAwardBid(move);
                return true;
            case AUCTION_REFUND_BID:
                undoRefundBid(move);
                return true;
            case AUCTION_GIVEAWAY:
                undoAuctionGiveaway(move);
                return true;
            case AUCTION_END_PHASE:
                undoEndAuctionPhase(move);
                return true;
            case AUCTION_DISCOUNT_RESET:
                undoAuctionDiscountReset(move);
                return true;
            case STOCK_PASS:
                undoStockPass(move);
                return true;
            case STOCK_END_ROUND:
                undoEndStockRound(move);
                return true;
            case STOCK_SET_PAR:
                undoSetPar(move);
                return true;
            case STOCK_BUY_BANK:
                undoBuyBank(move);
                return true;
            case STOCK_BUY_POOL:
                undoBuyPool(move);
                return true;
            case UPDATE_PREZ:
                undoUpdatePrez(move);
                return true;
            case STOCK_SALE:
                undoStockSale(move);
                return true;
            case STOCK_HEADER:
                return true; // nothing to undo
            case DROP_STOCK_PRICE:
                undoDropStockPrice(move);
                return true;
            case REORDER_CORP:
                undoReorderCorp(move);
                return true;
            case STOCK_FOOTER:
                undoStockFooter(move);
                return true;
            case TAKE_LOAN:
                undoTakeLoan(move);
                return true;
            case BUY_PRIV:
                undoBuyPriv(move);
                return true;
            case FLOAT:
                undoFloat(move);
                return true;
            case FAIL_FLOAT:
                undoFailFloat(move);
                return true;
            case NEXT_CORP:
                undoNextCorp(move);
                return true;
            case END_OP_ROUND:
                undoEndOpRound(move);
                return true;
            default:
                return false;
        }
    }

    private void doBuyPool(TrainMove move, boolean rawMove) {
        Corp c = findCorp(move.getCorp());
        Wallet w = getPlayerWallet(move.getPlayer());
        c.setBankShares(c.getPoolShares() - 1);
        shareToWallet(w, move.getCorp(), 1);
        payWalletToBank(w, move.getAmount());
        if (rawMove) {
            updatePrez(move.getCorp());
        }
        incrementStockPlayer(true, rawMove);
    }

    private void undoBuyPool(TrainMove move) {
        Corp c = findCorp(move.getCorp());
        Wallet w = getPlayerWallet(move.getPlayer());
        c.setPoolShares(c.getPoolShares() + 1);
        shareToWallet(w, move.getCorp(), -1);
        board.setCurrentPlayer(move.getPlayer());
    }

    private void doStockFooter(TrainMove move, boolean rawMove) {
        incrementStockPlayer(true, rawMove);
    }

    private void undoStockFooter(TrainMove move) {
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
        if (create) w.getStocks().add(new Stock(corpName, shares, false, false));
    }

    private void updatePrez(String corpName) {
        Corp c = findCorp(corpName);
        int prezCount = 0;
        int maxCount = 2;
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
        incrementStockPlayer(true, rawMove);
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
        board.setCurrentPlayer(move.getPlayer());
    }

    /**
     * @return The level of the next available bank train, use 8 for D
     */
    private int nextTrainLevel() {
        return board.getTrains().size() > 1 ? board.getTrains().get(0) : 8;
    }

    private int currentTrainLevel() {
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
        switch (nextTrainLevel()) {
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
        board.setCurrentPlayer(move.getPlayer());
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

    private void doSetPar(TrainMove move, boolean rawMove) {
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
        w.getStocks().add(new Stock(move.getCorp(), 2, true, false));
        incrementStockPlayer(true, rawMove);
    }

    private int maxRounds() {
        switch (currentTrainLevel()) {
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
        if (rawMove) {
           for(Corp c: board.getCorps()) {
               if(c.getPoolShares() == 0 && c.getBankShares() == 0 && !c.getPrice().ceiling()) {
                   makeFollowMove(DROP_STOCK_PRICE, "", c.getName(), -1); //rise 1
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
                int min = nextTrainLevel();
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
        board.setPhase(move.getCorp());
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
        if(!phaseIs(Era.AUCTION)) throw new IllegalStateException("No auction");
        if (!eventIs(NORMAL_EVENT)) throw new IllegalStateException("No offering");
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
        if (!phaseIs(Era.AUCTION) || !eventIs(NORMAL_EVENT)) {
            throw new IllegalStateException("Not time for normal bids");
        }
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

    synchronized public Board1856 setPar(String corpName, int amount) {
        if(!PAR_VALUES.contains(amount)) {
            throw new IllegalStateException("Par Value must be 65, 70, 75, 80, 90, or 100");
        }
        Wallet w = findWallet(board.getCurrentPlayer());
        if (w.getCash() < amount * 2) throw new IllegalStateException(FUNDS);
        Corp corp = findCorp(corpName);
        if(corp.getPar() != 0) throw new IllegalStateException("Par is already set");
        //TODO enforce cert limits (+2)
        makePrimaryMove(STOCK_SET_PAR, board.getCurrentPlayer(), corpName, amount);
        return board;
    }

    synchronized public Board1856 buyBank(String corpName) {
        Wallet w = findWallet(board.getCurrentPlayer());
        Corp c = findCorp(corpName);
        if (c.getBankShares() < 1) throw new IllegalStateException("No Bank Shares Remain");
        if (w.getCash() < c.getPar()) throw new IllegalStateException(FUNDS);
        // TODO enforce cert limits
        // TODO enforce buy block after sale same round
        makePrimaryMove(STOCK_BUY_BANK, board.getCurrentPlayer(), corpName, c.getPar());
        return board;
    }

    synchronized public Board1856 buyPool(String corpName) {
        Wallet w = findWallet(board.getCurrentPlayer());
        Corp c = findCorp(corpName);
        if (c.getPoolShares() < 1) throw new IllegalStateException("No pool shares available");
        if(w.getCash() < c.getPrice().getPrice()) throw new IllegalStateException(FUNDS);
        // TODO enforce cert limits
        // TODO enforce no buy after sale same round
        makePrimaryMove(STOCK_BUY_POOL, board.getCurrentPlayer(), corpName, c.getPar());
        return board;
    }
}