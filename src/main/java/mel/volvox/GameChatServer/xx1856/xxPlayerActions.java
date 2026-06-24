package mel.volvox.GameChatServer.xx1856;

import mel.volvox.GameChatServer.model.xx1856.xxMove;

import java.util.List;

public class xxPlayerActions {
    static xxPlayer findPlayer(String name, xx1856Game game) {
        for(xxPlayer player:game.getBoard().getPlayers()) {
            if(name.equals(player.name)) return player;
        }
        return null;
    }

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
}
