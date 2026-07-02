package mel.volvox.GameChatServer.xx1856;

import mel.volvox.GameChatServer.model.xx1856.Move;

public class AuctionActions {
    public static class BuyPrivAction extends Action {
        private static int calculatePrice(Move move, Game game) {
            int base = findPriv(move.getCorp(), game).price;
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
            // TODO make SUB actions to advance to next Priv or end of round
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
}
