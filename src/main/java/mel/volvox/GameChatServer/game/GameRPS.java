package mel.volvox.GameChatServer.game;

import mel.volvox.GameChatServer.comm.RPSBoard;
import mel.volvox.GameChatServer.model.seating.Move;
import mel.volvox.GameChatServer.repository.MoveRepo;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static mel.volvox.GameChatServer.comm.RPSBoard.*;

public class GameRPS extends AbstractGame {
    RPSBoard board = new RPSBoard();

    //private items (not part of board, which goes to players)
    Map<String, String> pendingMoves = new HashMap<>();
    List<String> departingUsers = new ArrayList<>();

    @Override
    public void abandonSeat(String user) {
        departingUsers.add(user);
    }

    @Override
    public String requestSeat(String seat, String user) {
        if (PLAYER.equals(seat) && !board.getNoobs().contains(user)) board.getNoobs().add(user);
        return seat;
    }

    @Override
    public String changeSeats(String newSeat, String user) {
        if(PLAYER.equals(newSeat)) {
            if (!board.getNoobs().contains(user)) board.getNoobs().add(user);
        } else {
            board.getNoobs().remove(user);
        }
        return newSeat;
    }

    @Override public void initMove(Move move) { } //TODO
    public RPSBoard getStatus() { return board; } //TODO update timer

    private void freeze() {
        long now = System.currentTimeMillis();
        long delta = now - board.getTimeStart();
        board.setTime(board.getTime() - (int)(delta/1000));
        board.setTimeStart(now);
    }

    public RPSBoard pause(MoveRepo moveRepo) {
        //TODO permission check
        switch (board.getState()) {
            case RPSBoard.PAUSED: break;
            case RPSBoard.MOVING:
                board.setState(RPSBoard.PAUSED);
                freeze();
                break;
            case RPSBoard.ANNOUNCING:
                board.setState(RPSBoard.STOPPED);
                freeze();
                break;
            case RPSBoard.STOPPED: break;
            default: break;
        }
        return board;
    }

    public RPSBoard resume(MoveRepo moveRepo) {
        switch (board.getState()) {
            case RPSBoard.PAUSED:
                board.setState(RPSBoard.MOVING);
                board.setTimeStart(System.currentTimeMillis());
                break;
            case RPSBoard.MOVING:
                break;
            case RPSBoard.ANNOUNCING:
                break;
            case RPSBoard.STOPPED:
                board.setState(RPSBoard.ANNOUNCING);
                board.setTimeStart(System.currentTimeMillis());
                break;
            default: break;
        }
        return board;
    }

    static String nameOf(String choice) {
        return switch (choice) {
            case ROCK -> "rock";
            case PAPER -> "paper";
            case SCISSORS -> "scissors";
            default -> "garbage";
        };
    }

    synchronized public String chooseThrow(String user, String choice) {
        pendingMoves.put(user, choice);
        return choice;
    }
}
