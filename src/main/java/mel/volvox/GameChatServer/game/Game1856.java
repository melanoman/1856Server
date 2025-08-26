package mel.volvox.GameChatServer.game;

import lombok.Getter;
import lombok.Setter;
import mel.volvox.GameChatServer.comm.train.Corp;
import mel.volvox.GameChatServer.comm.train.Priv;
import mel.volvox.GameChatServer.comm.train.Board1856;
import mel.volvox.GameChatServer.model.train.TrainMove;
import mel.volvox.GameChatServer.model.train.TrainMoveID;
import mel.volvox.GameChatServer.comm.train.Wallet;
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

    // action constants
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

    // event constants
    public static final String NORMAL_EVENT = "";
    public static final String BIDOFF_EVENT = "bidoff";

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

    public enum Era { GATHER, AUCTION, STOCK, OP, CGRFORM, DONE }

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
    }

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
    }

    private void payPlayer(String player, int amount) {
        payWallet(getPlayerWallet(player) , amount);
    }

    private void payWallet(Wallet w, int amount) {
        w.setCash(w.getCash() + amount);
        board.setBankCash(board.getBankCash() - amount);
    }

    private void doAuctionPass(boolean rawMove) {
        incrementAuctionPlayer(false, rawMove);
    }

    private void doAuctionBid(TrainMove move, boolean rawMove) {
        Wallet w = getCurrentWallet();
        w.getPrivates().add(new Priv(move.getCorp(), move.getAmount()));
        payWallet(w, -move.getAmount());
        incrementAuctionPlayer(true, rawMove);
    }

    private void undoAuctionBid(TrainMove move) {
        board.setCurrentPlayer(move.getPlayer());
        Wallet w = getCurrentWallet();
        w.getPrivates().removeIf(priv -> move.getCorp().equals(priv.getCorp()));
        payWallet(w, move.getAmount());
    }

    private void doStartBidoff(TrainMove move) {
        board.setEvent(BIDOFF_EVENT);
        board.setCurrentCorp(move.getCorp());
    }

    private void undoStartBidoff(TrainMove move) {
        board.setCurrentCorp(move.getPlayer());
        board.setEvent(NORMAL_EVENT);
    }

    private void doAuctionEndRound(TrainMove move, boolean rawMove) {
        //pay dividends
        for(Wallet w: board.getWallets()) {
            for (Priv priv: w.getPrivates()) {
                if(priv.getAmount() > 3) continue;
                payWallet(w, priv2div.get(priv.getCorp()));
            }
        }
        board.setAuctionDiscount(board.getAuctionDiscount() + 5);
        board.setPassCount(0);
        if(rawMove && priv2price.get(board.getCurrentCorp()) == board.getAuctionDiscount()) {
            makeFollowMove(AUCTION_GIVEAWAY, board.getCurrentPlayer(), board.getCurrentCorp(), 0);
        }
    }

    private void undoAuctionEndRound(TrainMove move) {
        //unpay dividends
        for(Wallet w: board.getWallets()) {
            for (Priv priv: w.getPrivates()) {
                if(priv.getAmount() > 3) continue;
                payWallet(w, -priv2div.get(priv.getCorp()));
            }
        }
        board.setAuctionDiscount(board.getAuctionDiscount() - 5);
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
            default:
                throw new IllegalStateException("unknown move action: "+move.getAction());
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
        payWallet(w, -move.getAmount());
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
        payWallet(w, move.getAmount());
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

    private void doAuctionBuy(TrainMove move, boolean rawMove) {
        Wallet wallet = getCurrentWallet();
        payWallet(wallet, -move.getAmount());
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
        payWallet(wallet, move.getAmount());
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
            default:
                return false;
        }
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
            new Corp("WBG", 3),
            new Corp("WR", 2)
    );

    private void doEndAuctionPhase(TrainMove move) {
        board.setPhase(Era.STOCK.name());
        for(Corp c: INIT_IPO) {
            board.getCorps().add(c.dup());
        }
    }

    private void undoEndAuctionPhase(TrainMove move) {
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
                payWallet(w, move.getAmount());
                w.getPrivates().removeIf(x -> x.getCorp().equals(move.getCorp()));
            }
        }
    }

    private void undoRefundBid(TrainMove move) {
        for (Wallet w: board.getWallets()) {
            if (w.getName().equals(move.getPlayer())) {
                payWallet(w, -move.getAmount());
                w.getPrivates().add(new Priv(move.getCorp(), move.getAmount()));
            }
        }
    }

    private void doEndBidoff(TrainMove move, boolean rawMove) {
        for (Wallet w: board.getWallets()) {
            if(move.getPlayer().equals(w.getName())) {
                payWallet(w, -move.getAmount());
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
                payWallet(w, move.getAmount());
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
}