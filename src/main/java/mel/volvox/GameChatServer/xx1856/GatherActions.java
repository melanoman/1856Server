package mel.volvox.GameChatServer.xx1856;

import mel.volvox.GameChatServer.model.xx1856.Move;
import mel.volvox.GameChatServer.service.DiceService;
import mel.volvox.undo.UndoManager;

import java.util.ArrayList;
import java.util.List;

import static mel.volvox.GameChatServer.xx1856.Opcodes.*;

public class GatherActions {
    static Player findPlayer(String name, Game game) {
        for(Player player:game.getBoard().getPlayers()) {
            if(name.equals(player.name)) return player;
        }
        return null;
    }

    public static void registerAll(UndoManager<Move, Game, Action> mgr) {
        mgr.registerActionType(ADD_PLAYER, new AddPlayerAction());
        mgr.registerActionType(RENAME_PLAYER, new RenamePlayerAction());
        mgr.registerActionType(START_GAME, new StartGameAction());
        mgr.registerActionType(SHUFFLE, new ShuffleAction());
    }

    //player = name to add
    public static class AddPlayerAction extends Action {

        @Override
        public void checkAllowed(Move move, Game game) {
            game.assertPhase(Game.Era.GATHER, "AddPlayer");
            if(game.getBoard().getPlayers().size() > 5) throw new IllegalStateException("Game is full");
            if(findPlayer(move.getPlayer(), game) != null) throw new IllegalStateException("Duplicate Name: "+move.getPlayer());
        }

        @Override
        public void init(Move move, Game game) { }

        @Override
        public void doAction(Move move, Game game) {
            Player player = new Player();
            player.name = move.getPlayer();
            game.getBoard().getPlayers().add(player);
        }

        @Override
        public void undoAction(Move move, Game game) {
            List<Player> players = game.getBoard().getPlayers();
            players.remove(players.size() - 1);
        }
    }

    // player = oldname detail = newName
    static class RenamePlayerAction extends Action {

        @Override
        public void checkAllowed(Move move, Game game) {
            game.assertPhase(Game.Era.GATHER, "RenamePlayer");
            if(findPlayer(move.getPlayer(), game) == null) throw new IllegalStateException("Player not found");
            if(findPlayer(move.getDetail(), game) != null) throw new IllegalStateException("Duplicate player name");
        }

        @Override
        public void init(Move move, Game game) { }

        @Override
        public void doAction(Move move, Game game) {
            Player player = findPlayer(move.getPlayer(), game);
            assert player != null;
            player.name = move.getDetail();
        }

        @Override
        public void undoAction(Move move, Game game) {
            Player player = findPlayer(move.getDetail(), game);
            assert player != null;
            player.name = move.getPlayer();
        }
    }

    // detail = shuffle order (123... for no shuffle)
    static class StartGameAction extends Action {
        @Override
        public void checkAllowed(Move move, Game game) {
            game.assertPhase(Game.Era.GATHER, "StartGame");
            int playerCount = game.getBoard().getPlayers().size();
            if(playerCount < 3) throw new IllegalStateException("Too few players (min=3)");
            if(playerCount > 6) throw new IllegalStateException("Too many players (max=6)");
        }

        static final String NO_SHUFFLE = "012345";

        @Override
        public void init(Move move, Game game) {
            int size = game.getBoard().getPlayers().size();
            StringBuilder buf = new StringBuilder(NO_SHUFFLE.substring(0, size));
            if(move.getAmount() == 1) {
                for (int i = size; i > 0; i--) {
                    int j = DiceService.Roll(i);
                    char tmp = buf.charAt(i - 1);
                    buf.setCharAt(i - 1, buf.charAt(j - 1));
                    buf.setCharAt(j - 1, tmp);
                }
            }
            game.addSubUsingDetail(SHUFFLE, buf.toString());
        }

        @Override
        public void doAction(Move move, Game game) { } // all work is done in shuffle

        @Override
        public void undoAction(Move move, Game game) { }
    }

    static class ShuffleAction extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) { }

        @Override
        public void doAction(Move move, Game game) {
            List<Player> newOrder = new ArrayList<>();
            for(int i=0; i<move.getDetail().length(); i++) { //shuffle from script
                int index = move.getDetail().charAt(i) - '0';
                newOrder.add(game.getBoard().getPlayers().get(index));
            }
            game.getBoard().players = newOrder;
            game.getBoard().phase = Game.Era.AUCTION.name();
            game.getBoard().currentCorp = Priv.PRIVS.get(0).getName();
            game.getBoard().currentPlayer = game.getBoard().getPlayers().get(0).name;
            game.getBoard().priorityPlayer = game.getBoard().getPlayers().get(0).name;
        }

        @Override
        public void undoAction(Move move, Game game) {
            List<Player> oldOrder = new ArrayList<>();
            for(int i=0; i<move.getDetail().length(); i++) { //unshuffle from script
                int index = move.getDetail().indexOf('0' + i);
                oldOrder.add(game.getBoard().getPlayers().get(index));
            }
            game.getBoard().players = oldOrder;
            game.getBoard().phase = Game.Era.GATHER.name();
            game.getBoard().currentCorp = null;
            game.getBoard().currentPlayer = null;
            game.getBoard().priorityPlayer = null;
        }
    }
}
