package mel.volvox.GameChatServer.xx1856;

import mel.volvox.GameChatServer.game.Game;
import mel.volvox.GameChatServer.model.xx1856.xxMove;
import mel.volvox.undo.UndoManager;

import java.util.List;

import static mel.volvox.GameChatServer.xx1856.xxOpcodes.*;

public class xxPlayerActions {
    static xxPlayer findPlayer(String name, xx1856Game game) {
        for(xxPlayer player:game.getBoard().getPlayers()) {
            if(name.equals(player.name)) return player;
        }
        return null;
    }

    public static void registerAll(UndoManager<xxMove, xx1856Game, xxAction> mgr) {
        mgr.registerActionType(ADD_PLAYER, new AddPlayerAction());
        mgr.registerActionType(RENAME_PLAYER, new RenamePlayerAction());
    }

    //player = name to add
    public static class AddPlayerAction extends xxAction {

        @Override
        public void checkAllowed(xxMove move, xx1856Game game) {
            game.assertPhase(xx1856Game.Era.GATHER, "AddPlayer");
            if(game.getBoard().getPlayers().size() > 5) throw new IllegalStateException("Game is full");
            if(findPlayer(move.getPlayer(), game) != null) throw new IllegalStateException("Duplicate Name: "+move.getPlayer());
        }

        @Override
        public void init(xxMove move, xx1856Game game) { }

        @Override
        public void exec(xxMove move, xx1856Game game) {
            xxPlayer player = new xxPlayer();
            player.name = move.getPlayer();
            game.getBoard().getPlayers().add(player);
        }

        @Override
        public void undo(xxMove move, xx1856Game game) {
            List<xxPlayer> players = game.getBoard().getPlayers();
            players.remove(players.size() - 1);
        }
    }

    // player = oldname detail = newName
    static class RenamePlayerAction extends xxAction {

        @Override
        public void checkAllowed(xxMove move, xx1856Game game) {
            if(findPlayer(move.getPlayer(), game) == null) throw new IllegalStateException("Player not found");
            if(findPlayer(move.getDetail(), game) != null) throw new IllegalStateException("Duplicate player name");
        }

        @Override
        public void init(xxMove move, xx1856Game game) { }

        @Override
        public void exec(xxMove move, xx1856Game game) {
            xxPlayer player = findPlayer(move.getPlayer(), game);
            assert player != null;
            player.name = move.getDetail();
        }

        @Override
        public void undo(xxMove move, xx1856Game game) {
            xxPlayer player = findPlayer(move.getDetail(), game);
            assert player != null;
            player.name = move.getPlayer();
        }
    }
}
