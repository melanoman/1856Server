package mel.volvox.GameChatServer.xx1856;

import mel.volvox.GameChatServer.comm.train.StockPrice;
import mel.volvox.GameChatServer.model.xx1856.Move;
import mel.volvox.undo.UndoManager;

import java.util.List;

import static mel.volvox.GameChatServer.xx1856.Opcodes.SET_PAR;

public class StockActions {
    static final List<Game.Era> STOCK_OR_INITIAL = List.of(Game.Era.STOCK, Game.Era.INITIAL);
    static final List<Integer> VALID_PARS = List.of(65, 70, 75, 80, 90, 100);

    public static void registerAll(UndoManager<Move, Game, Action> undoMgr) {
        undoMgr.registerActionType(SET_PAR, new SetParAction());
    }

    static class SetParAction extends Action {

        @Override
        public void checkAllowed(Move move, Game game) {
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

        @Override
        public void init(Move move, Game game) {
            makePriorityAdvance(game);
        }

        @Override
        public void doAction(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            c.par = move.getAmount();
            c.bankShares = 8;
            c.poolShares = 0;
            game.getBank().debitPlayer(move.getPlayer(), 2 * move.getAmount());
            c.price = StockPrice.makePar(move.getAmount());
            //TODO put stock in portfolio
            //TODO record float type
        }

        @Override
        public void undoAction(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            c.par = 0;
            c.bankShares = 0;
            c. poolShares = 0;
            game.getBank().debitPlayer(move.getPlayer(), 2 * move.getAmount());
            c.price = null;
            //TODO remove stock from portfolio
            //TODO clear float type
        }
    }
}
