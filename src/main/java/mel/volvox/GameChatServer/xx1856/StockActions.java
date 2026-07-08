package mel.volvox.GameChatServer.xx1856;

import mel.volvox.GameChatServer.comm.train.StockPrice;
import mel.volvox.GameChatServer.model.xx1856.Move;
import mel.volvox.undo.UndoManager;

import java.util.List;

import static mel.volvox.GameChatServer.xx1856.Opcodes.*;

public class StockActions {
    static final List<Game.Era> STOCK_OR_INITIAL = List.of(Game.Era.STOCK, Game.Era.INITIAL);
    static final List<Integer> VALID_PARS = List.of(65, 70, 75, 80, 90, 100);

    public static void registerAll(UndoManager<Move, Game, Action> undoMgr) {
        undoMgr.registerActionType(SET_PAR, new SetParAction());
        undoMgr.registerActionType(RESORT_CORP, new ResortCorpAction());
    }

    static class SetParAction extends Action {

        @Override public void checkAllowed(Move move, Game game) {
            assertPhases(game, STOCK_OR_INITIAL, "SetPar");
            Corp c = findCorp(move.getCorp(), game);
            if (c.par > 0) {
                throw new IllegalStateException("Par already set");
            }
            if (!VALID_PARS.contains(move.getAmount())) {
                throw new IllegalStateException("Invalid Par value " + move.getAmount());
            }
            assertPlayerFunds(game, move.getPlayer(), 2 * move.getAmount(), "SetPar");
            // TODO check portfolio space
        }

        @Override public void init(Move move, Game game) {
            makePriorityAdvance(game);
            int oldIndex = findCorpIndex(move.getCorp(), game);
            game.addSub(RESORT_CORP, "", move.getCorp(), oldIndex, "");
        }

        @Override public void doAction(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            c.par = move.getAmount();
            c.bankShares = 8;
            c.poolShares = 0;
            game.getBank().debitPlayer(move.getPlayer(), 2 * move.getAmount());
            c.price = StockPrice.makePar(move.getAmount());
            //TODO record float type
            findPlayer(move.getPlayer(), game).shares.add(new Stock(c.name, 2, true));
        }

        @Override public void undoAction(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            c.par = 0;
            c.bankShares = 0;
            c. poolShares = 0;
            game.getBank().payPlayer(move.getPlayer(), 2 * move.getAmount());
            c.price = null;
            //TODO clear float type
            findPlayer(move.getPlayer(), game).shares.removeIf(x -> x.corpName.equals(move.getCorp()));
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
}
