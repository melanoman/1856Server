package mel.volvox.GameChatServer.xx1856;

import mel.volvox.GameChatServer.model.xx1856.Move;
import mel.volvox.undo.UndoManager;
import mel.volvox.undo.UndoableAction;

import java.util.List;

import static mel.volvox.GameChatServer.xx1856.Opcodes.*;

public abstract class Action implements UndoableAction<Move, Game> {
    public static void registerAll(UndoManager<Move, Game, Action> undoMgr) {
        undoMgr.registerActionType(CHANGE_PLAYER, new ChangePlayerAction());
        undoMgr.registerActionType(CHANGE_PRIORITY, new ChangePriorityAction());
        undoMgr.registerActionType(CHANGE_CORP, new ChangeCorpAction());
    }

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
    static void assertPhases(Game game, List<Game.Era> intended, String caller) {
        for(Game.Era era:intended) {
            if(era.name().equals(game.getBoard().phase)) return;
        }
        throw new IllegalStateException("Edited during wrong phase -- "+ caller);
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

    //must be a SUB
    static class ChangePlayerAction extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) { }

        @Override public void doAction(Move move, Game game) {
            game.getBoard().currentPlayer = move.getPlayer();
        }

        @Override public void undoAction(Move move, Game game) {
            game.getBoard().currentPlayer = move.getDetail();
        }
    }

    //must be a SUB
    static class ChangePriorityAction extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) { }

        @Override public void doAction(Move move, Game game) {
            game.getBoard().priorityPlayer = move.getPlayer();
        }

        @Override public void undoAction(Move move, Game game) {
            game.getBoard().priorityPlayer = move.getDetail();
        }
    }


    static class ChangeCorpAction extends Action {
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) { }

        @Override public void doAction(Move move, Game game) {
            game.getBoard().currentCorp = move.getCorp();
        }

        @Override public void undoAction(Move move, Game game) {
            game.getBoard().currentCorp = move.getDetail();
        }
    }

    // utility functions for locating common objects
    static Player findPlayer(String name, Game game) {
        for(Player player:game.getBoard().getPlayers()) {
            if(name.equals(player.name)) return player;
        }
        throw new IllegalStateException("Unknown Player");
    }

    static Priv findPriv(String name) {
        for(Priv priv: Priv.PRIVS) {
            if(name.equals(priv.name)) return priv;
        }
        throw new IllegalStateException("Unknown Private Company");
    }

    static Corp findCorp(String name, Game game) {
        for (Corp corp: game.getBoard().corps) {
            if(name.equals(corp.name)) return corp;
        }
        throw new IllegalStateException("Corporation not found");
    }

    static int findPrivIndex(String name) {
        int index = 0;
        for(Priv priv:Priv.PRIVS) {
            if(priv.name.equals(name)) return index;
            index++;
        }
        throw new IllegalStateException("Unknown Private Company");
    }

    static int findPlayerIndex(String name, Game game) {
        int index = 0;
        for(Player player:game.getBoard().getPlayers()) {
            if(player.name.equals(name)) return index;
            index++;
        }
        throw new IllegalStateException("Unknown Player");
    }

    static int findCorpIndex(String name, Game game) {
        int index = 0;
        for (Corp corp: game.getBoard().corps) {
            if(corp.name.equals(name)) return index;
            index++;
        }
        throw new IllegalStateException("Unknown Private Company");
    }

    static Player nextPlayer(String current, Game game) {
        int index = findPlayerIndex(current, game);
        if (index+1 < game.getBoard().players.size()) {
            return game.getBoard().players.get(index + 1);
        }
        return game.getBoard().players.get(0);
    }

    static void makePriorityAdvance(Game game) {
        String currentName = game.getBoard().currentPlayer;
        Player next = nextPlayer(currentName, game);
        game.addSub(CHANGE_PRIORITY, next.name, "", 0, game.getBoard().priorityPlayer);
        game.addSub(CHANGE_PLAYER, next.name, "", 0, currentName);
    }

    static void makePlayerAdvance(Game game) {
        String currentName = game.getBoard().currentPlayer;
        Player next = nextPlayer(currentName, game);
        game.addSub(CHANGE_PLAYER, next.name, "", 0, currentName);
    }

    public static class NullAction extends Action {
        @Override public void doAction(Move move, Game game) { }
        @Override public void undoAction(Move move, Game game) { }
        @Override public void checkAllowed(Move move, Game game) { }
        @Override public void init(Move move, Game game) { }
    }
}
