package mel.volvox.GameChatServer.xx1856;

import mel.volvox.GameChatServer.model.xx1856.Move;
import mel.volvox.undo.UndoManager;

import java.util.ArrayList;
import java.util.List;

import static mel.volvox.GameChatServer.xx1856.Action.*;
import static mel.volvox.GameChatServer.xx1856.Opcodes.*;

public class AuctionActions {
    public static final String BIDOFF_ACTIVITY = "bidoff";

    public static void registerAll(UndoManager<Move, Game, Action> mgr) {
        mgr.registerActionType(BUY, new BuyPrivAction());
        mgr.registerActionType(BID, new BidAction());
        mgr.registerActionType(AWARD_BID, new AwardBidAction());
        mgr.registerActionType(CANCEL_BID, new CancelBidAction());
        mgr.registerActionType(START_BIDOFF, new StartBidoffAction());
        mgr.registerActionType(WIN_BIDOFF, new WinBidoffAction());
        mgr.registerActionType(AUCTION_PASS, new PassAction());
        mgr.registerActionType(AUCTION_PAYOUT, new PayoutAction());
        mgr.registerActionType(END_AUCTION, new EndAuctionAction());
    }

    static class BuyPrivAction extends Action {
        private static int calculatePrice(Move move, Game game) {
            int base = findPriv(move.getCorp()).price;
            return ("FLOS".equals(move.getCorp())) ? base - game.getBoard().flosDiscount : base;
        }

        @Override public void checkAllowed(Move move, Game game) {
            assertPhase(game, Game.Era.AUCTION, "BuyPriv");
            assertPlayerTurn(game, move.getPlayer(), "BuyPriv");
            assertCorpTurn(game, move.getCorp(), "BuyPriv");
            assertPlayerFunds(game, move.getPlayer(), calculatePrice(move, game), "BuyPriv");
        }

        @Override public void init(Move move, Game game) {
            makePriorityAdvance(game);
            makePrivAdvance(game);
        }

        @Override public void doAction(Move move, Game game) {
            Player player = findPlayer(move.getPlayer(), game);
            player.privs.add(move.getCorp());
            game.getBank().debitPlayer(move.getPlayer(), calculatePrice(move, game));
        }

        @Override public void undoAction(Move move, Game game) {
            Player player = findPlayer(move.getPlayer(), game);
            player.privs.remove(move.getCorp());
            game.getBank().payPlayer(move.getPlayer(), calculatePrice(move, game));
        }
    }

    static class BidAction extends Action {
        @Override public void checkAllowed(Move move, Game game) {
            assertPhase(game, Game.Era.AUCTION, "Bid");
            assertPlayerTurn(game, game.getBoard().currentPlayer, "Bid");
            if(findPrivIndex(game.getBoard().currentCorp) >= findPrivIndex(move.getCorp())) {
                throw new IllegalStateException("Too late to bid on this company");
            }
            int minBid = Math.max(topBid(move.getCorp(), game), findPriv(move.getCorp()).price) + 5;
            if (move.getAmount() < minBid) throw new IllegalStateException("Minimum bid is "+minBid);
            int oldAmount = topPlayerBid(move.getCorp(), move.getPlayer(), game);
            assertPlayerFunds(game, move.getPlayer(), move.getAmount() - oldAmount, "Bid");
        }

        @Override public void init(Move move, Game game) {
            // if there is an old bid by the same player, it will be lower than the new bid.
            Bid old = findMinPlayerBid(move.getCorp(), move.getPlayer(), game);

            // if the oldest bid is the new bid, there is nothing to cancel
            if (old != null && old.amount != move.getAmount()) makeCancelBid(old, game);
            makePriorityAdvance(game);
        }

        @Override public void doAction(Move move, Game game) {
            game.getBoard().bids.add(new Bid(move.getCorp(), move.getPlayer(), move.getAmount()));
            game.getBank().debitPlayer(move.getPlayer(), move.getAmount());
        }

        @Override public void undoAction(Move move, Game game) {
            game.getBoard().bids.removeIf(bid -> matchBid(bid, move.getCorp(), move.getPlayer(), move.getAmount()));
            game.getBank().payPlayer(move.getPlayer(), move.getAmount());
        }
    }

    static class AwardBidAction extends Action {
        @Override public void checkAllowed(Move move, Game game) { }

        @Override public void init(Move move, Game game) {
            makePrivAdvance(game);
        }

        @Override public void doAction(Move move, Game game) {
            Player player = findPlayer(move.getPlayer(), game);
            player.privs.add(move.getCorp());
            game.getBoard().bids.removeIf(bid -> bid.priv.equals(move.getCorp()));
        }

        @Override public void undoAction(Move move, Game game) {
            Player player = findPlayer(move.getPlayer(), game);
            player.privs.removeIf(priv -> priv.equals(move.getCorp()));
            game.getBoard().bids.add(new Bid(move.getCorp(), move.getPlayer(), move.getAmount()));
        }
    }

    static int topBid(String priv, Game game) {
        int amount = 0;

        for(Bid bid: game.getBoard().getBids()) {
            if (bid.priv.equals(priv) && bid.amount > amount) amount = bid.amount;
        }
        return amount;
    }

    static int topPlayerBid(String priv, String player, Game game) {
        int amount = 0;

        for (Bid bid : game.getBoard().getBids()) {
            if (bid.priv.equals(priv) && bid.amount > amount && bid.player.equals(player)) amount = bid.amount;
        }
        return amount;
    }

    // get the lowest (older) bid to cancel it in favor of the new bid. there may only be one after all execs
    static Bid findMinPlayerBid(String priv, String player, Game game) {
        Bid out = null;
        for (Bid bid: game.getBoard().getBids()) {
            if(bid.player.equals(player) && bid.priv.equals(priv)) {
                if (out == null || out.getAmount() > bid.getAmount()) out = bid;
            }
        }
        return out;
    }

    static class CancelBidAction extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) { }

        @Override public void doAction(Move move, Game game) {
            game.getBank().payPlayer(move.getPlayer(), move.getAmount());
            game.getBoard().bids.removeIf(bid -> matchBid(bid, move.getCorp(), move.getPlayer(), move.getAmount()));
        }

        @Override public void undoAction(Move move, Game game) {
            game.getBank().debitPlayer(move.getPlayer(), move.getAmount());
            game.getBoard().bids.add(new Bid(move.getCorp(), move.getPlayer(), move.getAmount()));
        }
    }

    static class StartBidoffAction extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) { }

        @Override public void doAction(Move move, Game game) {
            game.getBoard().activity = BIDOFF_ACTIVITY;
        }

        @Override public void undoAction(Move move, Game game) {
            game.getBoard().activity = move.getDetail();
        }
    }

    static boolean matchBid(Bid bid, String priv, String player, int amount) {
        return bid.priv.equals(priv) && bid.player.equals(player) && bid.amount == amount;
    }

    static void makeCancelBid(Bid bid, Game game) {
        game.addSub(CANCEL_BID, bid.getPlayer(), bid.getPriv(), bid.getAmount(), "");
    }

    static Priv nextPriv(String currentName) {
        for(int i=0; i<Priv.PRIVS.size() - 1; i++) {
            if(Priv.PRIVS.get(i).name.equals(currentName)) return Priv.PRIVS.get(i+1);
        }
        return null;
    }

    static int countBids(Priv priv, Game game) {
        int count = 0;
        for(Bid bid:game.getBoard().bids) {
            if(bid.priv.equals(priv.name)) count++;
        }
        return count;
    }

    static void makeEndAuction(Game game) {
        game.addSub(END_AUCTION, "", "", 0, "");
    }

    static void makeSoloBidWin(Priv priv, Game game) {
        Bid bid = findSoloBid(priv.name, game);
        game.addSub(CHANGE_CORP, "", priv.name, 0, game.getBoard().currentCorp);
        game.addSub(AWARD_BID, bid.player, bid.priv, bid.getAmount(), "");
    }

    static void makeStartBidoff(Priv priv, Game game) {
        makePrivChange(priv, game);
        game.addSub(START_BIDOFF, "", "", 0, game.getBoard().activity);
    }

    static void makePrivChange(Priv priv, Game game) {
        game.addSub(CHANGE_CORP, "", priv.name, 0, game.getBoard().currentCorp);
    }

    static void makePrivAdvance(Game game) {
        String currentName = game.getBoard().currentCorp;
        Priv priv = nextPriv(currentName);
        if (priv == null) makeEndAuction(game);
        else switch(countBids(priv, game)) {
            case 0 -> makePrivChange(priv, game);
            case 1 -> makeSoloBidWin(priv, game);
            default -> makeStartBidoff(priv, game);
        }
    }

    static Bid findSoloBid(String name, Game game) {
        for(Bid bid:game.getBoard().bids) {
            if (bid.priv.equals(name)) return bid;
        }
        throw new IllegalStateException("CORRUPTION Solo Bid not found");
    }

    public static int calculateBuyPrice(Game game) {
        int base = findPriv(game.getBoard().currentCorp).price;
        return ("FLOS".equals(game.getBoard().currentCorp)) ? base - game.getBoard().flosDiscount : base;
    }

    public static class WinBidoffAction extends Action {

        @Override public void checkAllowed(Move move, Game game) {
            assertPhase(game, Game.Era.AUCTION, "WinBid");
            assertCorpTurn(game, move.getCorp(), "WinBid");
            confirmPlayerHasBid(game, move.getCorp(), move.getPlayer());
            confirmOverbidMargin(game, move.getCorp(), move.getPlayer(), move.getAmount());
            confirmOverbidFunding(game, move.getCorp(), move.getPlayer(), move.getAmount());
        }

        @Override public void init(Move move, Game game) {
            List<Bid> toCancel = new ArrayList<>();
            for (Bid bid:game.getBoard().bids) {
                if(bid.priv.equals(move.getCorp())) toCancel.add(bid);
            }
            for (Bid bid: toCancel) makeCancelBid(bid, game);
            makePrivAdvance(game);
        }

        @Override public void doAction(Move move, Game game) {
            Player player = findPlayer(move.getPlayer(), game);
            player.privs.add(move.getCorp());
            game.getBank().debitPlayer(move.getPlayer(), move.getAmount());
            game.getBoard().activity = "";
        }

        @Override public void undoAction(Move move, Game game) {
            Player player = findPlayer(move.getPlayer(), game);
            player.privs.remove(move.getCorp());
            game.getBank().payPlayer(move.getPlayer(), move.getAmount());
            game.getBoard().activity = BIDOFF_ACTIVITY;
        }
    }

    private static void confirmPlayerHasBid(Game game, String priv, String player) {
        for(Bid bid: game.getBoard().bids) {
            if(bid.priv.equals(priv) && bid.player.equals(player)) return;
        }
        throw new IllegalStateException("Player "+player+" is not part of the bidding");
    }

    private static void confirmOverbidMargin(Game game, String priv, String player, int amount) {
        int topBid = 0;
        int margin = 5;
        for (Bid bid : game.getBoard().bids) {
            if (bid.priv.equals(priv) && bid.amount > topBid) {
                topBid = bid.amount;
                margin = bid.player.equals(player) ? 0 : 5;
            }
        }
        if (amount - topBid < margin) throw new IllegalStateException("Minimum bid is " + (topBid + margin));
    }

    private static void confirmOverbidFunding(Game game, String priv, String player, int amount) {
        Bid bid = findMinPlayerBid(priv, player, game);
        int overbid = amount - bid.amount;
        assertPlayerFunds(game, player, overbid, "WinBid");
    }

    static class PassAction extends Action {
        @Override public void checkAllowed(Move move, Game game) {
            assertPhase(game, Game.Era.AUCTION, "AuctionPass");
            assertPlayerTurn(game, move.getPlayer(), "AuctionPass");
            if(!game.getBoard().activity.isEmpty()) throw new IllegalStateException("Enter Bidoff result first");
        }

        @Override public void init(Move move, Game game) {
            if (game.getBoard().priorityPlayer.equals(game.getBoard().currentPlayer)) {
                makeAuctionPayout(game);
            }
        }

        @Override public void doAction(Move move, Game game) {
            game.getBoard().currentPlayer = nextPlayer(game.getBoard().currentPlayer, game).name;
        }

        @Override public void undoAction(Move move, Game game) {
            game.getBoard().currentPlayer = move.getPlayer();
        }
    }

    static void makeAuctionPayout(Game game) {
        game.addSub(AUCTION_PAYOUT, "", "", 0, "");
    }

    static class PayoutAction extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) { }

        @Override public void doAction(Move move, Game game) {
            for (Player player: game.getBoard().getPlayers()) {
                for (String priv: player.privs) {
                    game.getBank().payPlayer(player.name, Priv.findPriv(priv).dividend);
                }
            }
            game.getBoard().flosDiscount += 5;
        }

        @Override public void undoAction(Move move, Game game) {
            for (Player player : game.getBoard().getPlayers()) {
                for (String priv : player.privs) {
                    game.getBank().debitPlayer(player.name, Priv.findPriv(priv).dividend);
                }
            }
            game.getBoard().flosDiscount -= 5;
        }
    }

    static class EndAuctionAction extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) { }

        @Override public void doAction(Move move, Game game) {
            game.getBoard().phase = Game.Era.INITIAL.name();
            game.getBoard().currentCorp = "";
            for(Corp c:Corp.INIT) game.getBoard().corps.add(new Corp(c.name, c.tokensMax));
            List<Integer> trains = new ArrayList<>();
            for(int i=2; i<7; i++) for (int j=8-i; j>0; j--) trains.add(i);
            game.getBoard().trains = trains;
        }

        @Override public void undoAction(Move move, Game game) {
            game.getBoard().phase = Game.Era.AUCTION.name();
            game.getBoard().currentCorp = Priv.PRIVS.get(Priv.PRIVS.size() - 1).name;
            game.getBoard().corps = new ArrayList<>();
        }
    }
}
