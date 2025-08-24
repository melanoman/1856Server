package mel.volvox.GameChatServer.game;

import lombok.Getter;
import lombok.Setter;
import mel.volvox.GameChatServer.comm.train.Priv;
import mel.volvox.GameChatServer.comm.train.Board1856;
import mel.volvox.GameChatServer.model.train.TrainMove;
import mel.volvox.GameChatServer.model.train.TrainMoveID;
import mel.volvox.GameChatServer.comm.train.Wallet;
import mel.volvox.GameChatServer.repository.TrainRepo;
import mel.volvox.GameChatServer.service.DiceService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public static final String AUCTION_PASS = "auctionPass";
    public static final String AUCTION_LONEBUY = "auctionLoneBuy";
    public static final String START_BIDOFF = "startBidoff";
    public static final String END_BIDOFF = "endBidoff";

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

    private List<TrainMove> history = new ArrayList<>();

    public enum Era { GATHER, AUCTION, STOCK, OP, DONE }

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
        StringBuffer buf = new StringBuffer(SHUFFLE_STOCK.substring(0, size));
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
    }

    private void doAuctionPass(TrainMove move, boolean rawMove) {
        incrementPlayer(false, rawMove);
    }

    private void doAuctionBid(TrainMove move, boolean rawMove) {
        Wallet w = getCurrentWallet();
        w.getPrivates().add(new Priv(move.getCorp(), move.getAmount()));
        w.setCash(w.getCash()-move.getAmount());
        incrementPlayer(true, rawMove);
    }

    private void undoAuctionBid(TrainMove move) {
        board.setCurrentPlayer(move.getPlayer());
        Wallet w = getCurrentWallet();
        w.getPrivates().removeIf(priv -> move.getCorp().equals(priv.getCorp()));
        w.setCash(w.getCash()+move.getAmount());
    }

    private void doStartBidoff(TrainMove move) {
        board.setEvent(BIDOFF_EVENT);
        board.setCurrentCorp(move.getCorp());
    }

    private void undoStartBidoff(TrainMove move) {
        board.setCurrentCorp(move.getPlayer());
        board.setEvent(NORMAL_EVENT);
    }

    private void doMove(TrainMove move, boolean rawMove) {
        board.setPassCount(move.getOldPassCount());
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
                doAuctionPass(move, rawMove);
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
            case START_BIDOFF:
                doStartBidoff(move);
                break;
            case END_BIDOFF:
                doEndBidoff(move, rawMove);
                break;
            default:
                throw new IllegalStateException("unknown move action: "+move.getAction());
        }
    }

    private void doAuctionRebid(TrainMove move, boolean rawMove) {
        Wallet w = getCurrentWallet();
        for (Priv priv: w.getPrivates()) {
            if(priv.getCorp().equals(move.getCorp())) {
                priv.setAmount(priv.getAmount() + move.getAmount());
            }
        }
        w.setCash(w.getCash()-move.getAmount());
        incrementPlayer(true, rawMove);
    }

    private void undoAuctionRebid(TrainMove move) {
        board.setCurrentPlayer(move.getPlayer());
        Wallet w = getCurrentWallet();
        for (Priv priv: w.getPrivates()) {
            if(priv.getCorp().equals(move.getCorp())) {
                priv.setAmount(priv.getAmount() - move.getAmount());
            }
        }
        w.setCash(w.getCash()+move.getAmount());
    }

    private void endAuctionRound(boolean rawMove) {
        board.setPassCount(0);
        board.setAuctionDiscount(board.getAuctionDiscount()+5);
        if (priv2price.get(board.getCurrentCorp()) == board.getAuctionDiscount()) {
            throw new IllegalStateException("TODO all pass in auction (pay priv, discount)");
        }
    }

    private void endAuctionPhase() {
        // TODO start first stock round
        throw new IllegalStateException("TODO end auction phase");
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
                        makeMove(AUCTION_LONEBUY, w.getName(), privName, priv.getAmount(), true);
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
                makeMove(START_BIDOFF, board.getCurrentCorp(), next, 0, true);
            }
        } else if(NONE.equals(next)) {
            endAuctionPhase();
        } else {
            board.setCurrentCorp(next);
        }
    }

    private void incrementPlayer(boolean actionTaken, boolean rawMove) {
        if(actionTaken) board.setPassCount(0);
        else {
            board.setPassCount(board.getPassCount()+1);
            if(rawMove && board.getPassCount() == board.getPlayers().size()) {
                endRound(rawMove);
            }
        }
        int index = board.getPlayers().indexOf(board.getCurrentPlayer()) + 1;
        if (index >= board.getPlayers().size()) index = 0;
        board.setCurrentPlayer(board.getPlayers().get(index));
        if (actionTaken) board.setPriorityHolder(board.getPlayers().get(index));
    }

    public void endRound(boolean rawMove) {
        // switch is fighting the use of .name()
        if (phaseIs(Era.AUCTION)) endAuctionRound(rawMove);
        else throw new IllegalStateException("TODO end round of type "+board.getPhase());
    }

    private void doAuctionBuy(TrainMove move, boolean rawMove) {
        Wallet wallet = getCurrentWallet();
        wallet.setCash(wallet.getCash() - move.getAmount());
        wallet.getPrivates().add(new Priv(move.getCorp(), 3));
        incrementPlayer(true, rawMove);
        incrementPrivate(rawMove);
    }

    private void undoAuctionBuy(TrainMove move) {
        board.setPhase(Era.AUCTION.name());
        board.setCurrentPlayer(move.getPlayer());
        board.setCurrentCorp(move.getCorp());
        Wallet wallet = getCurrentWallet();
        wallet.setCash(wallet.getCash() + move.getAmount());
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
            case START_BIDOFF:
                undoStartBidoff(move);
                return true;
            case END_BIDOFF:
                undoEndBidoff(move);
                return true;
            default:
                return false;
        }
    }

    private void doEndBidoff(TrainMove move, boolean rawMove) {
        System.out.println("TODO doEndBidoff");
    }

    private void undoEndBidoff(TrainMove move) {
        System.out.println("TODO undoEndBidoff");

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
    private void makeMove(String action, String player, String corp, int amount) {
        makeMove(action, player, corp, amount, false);
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
        if (!Era.GATHER.name().equals(board.getPhase()) ||
                board.getPlayers().size() >= 6 ||
                board.getPlayers().contains(name)) {
            return false;
        }
        makeMove(ADD_PLAYER, name, "", 0);
        return true;
    }

    synchronized public Board1856 renamePlayer(String oldName, String newName) {
        int seat = board.getPlayers().indexOf(oldName);
        if(seat < 0 || board.getPlayers().contains(newName)) return board;
        makeMove(RENAME_PLAYER, newName, oldName, seat);
        return board;
    }

    synchronized public Board1856 startGame(boolean shuffle) {
        if (!Era.GATHER.name().equals(board.getPhase()) ||
                board.getPlayers().size() < 3 ||
                board.getPlayers().size() > 6) {
            throw new IllegalStateException("Game is not startable");
        }
        makeMove(START_GAME, "", makeShuffle(shuffle, board.getPlayers().size()), 0);
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
        makeMove(AUCTION_BUY, board.getCurrentPlayer(), board.getCurrentCorp(), price);
        return board;
    }

    synchronized public Board1856 pass() {
        if (phaseIs(Era.AUCTION) && eventIs(NORMAL_EVENT)) {
            makeMove(AUCTION_PASS, board.getCurrentPlayer(), "", 1);
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
            makeMove(AUCTION_REBID, board.getCurrentPlayer(), corp, incr);
        } else {
            if(amount > w.getCash()) {
                throw new IllegalStateException(FUNDS);
            }
            makeMove(AUCTION_BID, board.getCurrentPlayer(), corp, amount);
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
            makeMove(END_BIDOFF, player, corp, amount);
        } else {
            throw new IllegalStateException("Minimum Raise is $5");
        }
        return board;
    }
}