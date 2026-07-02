package mel.volvox.GameChatServer.xx1856;

import mel.volvox.GameChatServer.model.xx1856.Move;
import mel.volvox.undo.UndoableAction;

public abstract class Action implements UndoableAction<Move, Game> {
    @Override
    public void exec(Move move, Game game) {
        resetBoardCounts(game);
        doAction(move, game);
    }

    @Override
    public void undo(Move move, Game game) {
        resetBoardCounts(game);
        undoAction(move, game);
    }

    public abstract void doAction(Move move, Game game);
    public abstract void undoAction(Move move, Game game);

    private void resetBoardCounts(Game game) {
        game.getBoard().moveNumber = game.getUndoMgr().size();
        game.getBoard().undoCount = game.getUndoMgr().getUndoCount();
    }

    // utility functions for checkAllowed
    static void assertPhase(Game game, Game.Era intended, String caller) {
        if(!intended.name().equals(game.getBoard().phase)) {
            throw new IllegalStateException("Edited during wrong phase -- " + caller);
        }
    }
    static void assertPlayerTurn(Game game, String player, String caller) {
        if(!player.equals(game.getBoard().currentPlayer)) {
            throw new IllegalStateException("Wrong player "+player+" for "+caller);
        }
    }

    static void assertCorpTurn(Game game, String corp, String caller) {
        if(!corp.equals(game.getBoard().currentCorp)) {
            throw new IllegalStateException("Wrong corp "+corp+" for "+caller);
        }
    }

    static void assertPlayerFunds(Game game, String player, int amount, String caller) {
        if(findPlayer(player, game).getCash() < amount) {
            throw new IllegalStateException("Insufficient Funds for "+caller);
        }
    }

    // utility functions for locating common objects
    static Player findPlayer(String name, Game game) {
        for(Player player:game.getBoard().getPlayers()) {
            if(name.equals(player.name)) return player;
        }
        throw new IllegalStateException("Unknown Player");
    }

    static Priv findPriv(String name, Game game) {
        for(Priv priv: Priv.PRIVS) {
            if(name.equals(priv.name)) return priv;
        }
        throw new IllegalStateException("Unknown Private Company");
    }
}
