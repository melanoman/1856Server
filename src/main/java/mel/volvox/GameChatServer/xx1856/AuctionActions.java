package mel.volvox.GameChatServer.xx1856;

import mel.volvox.GameChatServer.model.xx1856.Move;
import mel.volvox.undo.UndoManager;

import static mel.volvox.GameChatServer.xx1856.Opcodes.*;

public class AuctionActions {

    public static void registerAll(UndoManager<Move, Game, Action> mgr) {
        mgr.registerActionType(BUY, new BuyPrivAction());
        mgr.registerActionType(BID, new BidAction());
    }

    static class BuyPrivAction extends Action {
        private static int calculatePrice(Move move, Game game) {
            int base = findPriv(move.getCorp()).price;
            return ("FLOS".equals(move.getCorp())) ? base - game.getBoard().flosDiscount : base;
        }

        @Override
        public void checkAllowed(Move move, Game game) {
            assertPhase(game, Game.Era.AUCTION, "BuyPriv");
            assertPlayerTurn(game, move.getPlayer(), "BuyPriv");
            assertCorpTurn(game, move.getCorp(), "BuyPriv");
            assertPlayerFunds(game, move.getPlayer(), calculatePrice(move, game), "BuyPriv");
        }

        @Override
        public void init(Move move, Game game) {
            //TODO resolve bids, advance Corp
            makePriorityAdvance(game);
            makePrivAdvance(game);
        }

        @Override
        public void doAction(Move move, Game game) {
            Player player = findPlayer(move.getPlayer(), game);
            player.privs.add(move.getCorp());
            game.getBank().debitPlayer(move.getPlayer(), calculatePrice(move, game));
        }

        @Override
        public void undoAction(Move move, Game game) {
            Player player = findPlayer(move.getPlayer(), game);
            player.privs.remove(move.getCorp());
            game.getBank().payPlayer(move.getPlayer(), calculatePrice(move, game));
        }
    }

    static class BidAction extends Action {
        @Override
        public void checkAllowed(Move move, Game game) {
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

        @Override
        public void init(Move move, Game game) {
            // if there is an old bid by the same player, it will be lower than the new bid.
            Bid old = findMinPlayerBid(move.getCorp(), move.getPlayer(), game);

            // if the oldest bid is the new bid, there is nothing to cancel
            if (old != null && old.amount != move.getAmount()) cancelBid(old, game);
        }

        @Override
        public void doAction(Move move, Game game) {
            game.getBoard().bids.add(new Bid(move.getCorp(), move.getPlayer(), move.getAmount()));
            game.getBank().debitPlayer(move.getPlayer(), move.getAmount());
        }

        @Override
        public void undoAction(Move move, Game game) {
            game.getBoard().bids.removeIf(bid -> matchBid(bid, move.getCorp(), move.getPlayer(), move.getAmount()));
            game.getBank().payPlayer(move.getPlayer(), move.getAmount());
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
            if(bid.player.equals(player)) {
                if (out == null || out.getAmount() > bid.getAmount()) out = bid;
            }
        }
        return out;
    }

    static class CancelBidAction extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) { }

        @Override
        public void doAction(Move move, Game game) {
            game.getBank().payPlayer(move.getPlayer(), move.getAmount());
            game.getBoard().bids.removeIf(bid -> matchBid(bid, move.getCorp(), move.getPlayer(), move.getAmount()));
        }

        @Override
        public void undoAction(Move move, Game game) {
            game.getBank().debitPlayer(move.getPlayer(), move.getAmount());
            game.getBoard().bids.add(new Bid(move.getCorp(), move.getPlayer(), move.getAmount()));
        }
    }

    static boolean matchBid(Bid bid, String priv, String player, int amount) {
        return bid.priv.equals(priv) && bid.player.equals(player) && bid.amount == amount;
    }

    static void cancelBid(Bid bid, Game game) {
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
        //TODO makeEndAuction
    }

    static void makeSoloBidWin(Priv priv, Game game) {
        //TODO updateCorp to this priv
        //TODO make solo bid win
        //TODO advance again
    }

    static void makeStartBidoff(Priv priv, Game game) {
        //TODO updateCorp to this priv
        //TODO set activity to bidoff
    }

    static void makePrivChange(Priv priv, Game game) {
        game.addSubUsingCorpDetail(CHANGE_CORP, priv.name, game.getBoard().currentCorp);
    }

    static void makePrivAdvance(Game game) {
        String currentName = game.getBoard().currentCorp;
        Priv priv = nextPriv(currentName);
        if (priv == null) makeEndAuction(game);
        switch(countBids(priv, game)) {
            case 0 -> makePrivChange(priv, game);
            case 1 -> makeSoloBidWin(priv, game);
            default -> makeStartBidoff(priv, game);
        }
    }
}
