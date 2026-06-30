package mel.volvox.GameChatServer.xx1856;

import mel.volvox.GameChatServer.model.xx1856.xxMove;
import mel.volvox.undo.UndoManager;

import java.util.List;

import static mel.volvox.GameChatServer.xx1856.Opcodes.*;

public class PlayerActions {
    static Player findPlayer(String name, Game game) {
        for(Player player:game.getBoard().getPlayers()) {
            if(name.equals(player.name)) return player;
        }
        return null;
    }

    public static void registerAll(UndoManager<xxMove, Game, Action> mgr) {
        mgr.registerActionType(ADD_PLAYER, new AddPlayerAction());
        mgr.registerActionType(RENAME_PLAYER, new RenamePlayerAction());
    }

    //player = name to add
    public static class AddPlayerAction extends Action {

        @Override
        public void checkAllowed(xxMove move, Game game) {
            game.assertPhase(Game.Era.GATHER, "AddPlayer");
            if(game.getBoard().getPlayers().size() > 5) throw new IllegalStateException("Game is full");
            if(findPlayer(move.getPlayer(), game) != null) throw new IllegalStateException("Duplicate Name: "+move.getPlayer());
        }

        @Override
        public void init(xxMove move, Game game) { }

        @Override
        public void doAction(xxMove move, Game game) {
            Player player = new Player();
            player.name = move.getPlayer();
            game.getBoard().getPlayers().add(player);
        }

        @Override
        public void undoAction(xxMove move, Game game) {
            List<Player> players = game.getBoard().getPlayers();
            players.remove(players.size() - 1);
        }
    }

    // player = oldname detail = newName
    static class RenamePlayerAction extends Action {

        @Override
        public void checkAllowed(xxMove move, Game game) {
            if(findPlayer(move.getPlayer(), game) == null) throw new IllegalStateException("Player not found");
            if(findPlayer(move.getDetail(), game) != null) throw new IllegalStateException("Duplicate player name");
        }

        @Override
        public void init(xxMove move, Game game) { }

        @Override
        public void doAction(xxMove move, Game game) {
            Player player = findPlayer(move.getPlayer(), game);
            assert player != null;
            player.name = move.getDetail();
        }

        @Override
        public void undoAction(xxMove move, Game game) {
            Player player = findPlayer(move.getDetail(), game);
            assert player != null;
            player.name = move.getPlayer();
        }
    }
}
