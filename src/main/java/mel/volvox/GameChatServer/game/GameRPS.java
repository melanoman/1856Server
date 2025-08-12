package mel.volvox.GameChatServer.game;

import mel.volvox.GameChatServer.comm.Board;
import mel.volvox.GameChatServer.comm.RPSBoard;
import mel.volvox.GameChatServer.model.seating.Move;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GameRPS extends AbstractGame {
    RPSBoard board = new RPSBoard();

    //private items (not part of board, which goes to players)
    Map<String, Integer> pendingMoves = new HashMap<>();
    List<String> departingUsers = new ArrayList<>();

    @Override
    public void abandonSeat(String user) {
        departingUsers.add(user);
    }

    @Override
    public String requestSeat(String seat, String user) {
        if (PLAYER.equals(seat)) board.getNoobs().add(user);
        return user;
    }

    @Override
    public String changeSeats(String user, String newSeat) {
        if(PLAYER.equals(newSeat)) {
            board.getNoobs().add(user);
        } else {
            board.getNoobs().remove(user);
        }
        return newSeat;
    }

    @Override public void initMove(Move move) { } //TODO
    @Override public Move processMove(Move move) { return null; } //TODO
    @Override public Board getBoard() { return board; }
}
