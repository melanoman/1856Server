package mel.volvox.GameChatServer.xx1856;

import mel.volvox.GameChatServer.model.xx1856.Move;
import mel.volvox.undo.UndoManager;

import static mel.volvox.GameChatServer.xx1856.Opcodes.*;

public class PriceActions {
    public static void registerAll(UndoManager<Move, Game, Action> undoMgr) {
        undoMgr.registerActionType(PRICE_RIGHT, new MoveRight());
        undoMgr.registerActionType(PRICE_UP, new MoveUp());
        undoMgr.registerActionType(RESORT_CORP, new ResortCorpAction());

    }

    static int compareCorpOrder(Corp c, Corp old) {
        if (c.par > 0 && old.par <= 0) return 1;
        if (c.par <=0 && old.par > 0) return -1;
        if (c.par <=0) return 0;
        if (c.price.getPrice() > old.price.getPrice()) return 1;
        if (c.price.getPrice() < old.price.getPrice()) return -1;
        return (c.price.getX() - old.price.getX());
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

    static class MoveRight extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) {
            game.addSub(RESORT_CORP, "", move.getCorp(), findCorpIndex(move.getCorp(), game), "");
        }

        @Override public void doAction(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            c.price.right();
            if (c.price.getPrice() == 55) game.updateAllPorts(); // exiting yellow
        }

        @Override public void undoAction(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            c.price.left();
            if (c.price.getPrice() == 50) game.updateAllPorts(); //entering yellow
        }
    }

    static class MoveUp extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) {
            game.addSub(RESORT_CORP, "", move.getCorp(), findCorpIndex(move.getCorp(), game), "");
        }

        @Override public void doAction(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            c.price.up();
            if (c.price.getPrice() == 55) game.updateAllPorts(); // exiting yellow
        }

        @Override public void undoAction(Move move, Game game) {
            Corp c = findCorp(move.getCorp(), game);
            c.price.down();
            if (c.price.getPrice() == 50) game.updateAllPorts(); //entering yellow
        }
    }
}
