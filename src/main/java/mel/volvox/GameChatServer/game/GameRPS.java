package mel.volvox.GameChatServer.game;

import mel.volvox.GameChatServer.comm.RPSBoard;
import mel.volvox.GameChatServer.model.seating.Move;
import mel.volvox.GameChatServer.repository.MoveRepo;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static mel.volvox.GameChatServer.comm.RPSBoard.*;

public class GameRPS extends AbstractGame {
    private static final int GAME_LENGTH = 15;
    private static final int ROUND_INTERVAL = 10;
    RPSBoard board = new RPSBoard();
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    //private items (not part of board, which goes to players)
    Map<String, String> pendingMoves = new HashMap<>();
    List<String> departingUsers = new ArrayList<>();

    @Override
    synchronized public void abandonSeat(String user) {
        departingUsers.add(user);
    }

    @Override
    synchronized public String requestSeat(String seat, String user) {
        if (PLAYER.equals(seat) && !board.getNoobs().contains(user)) board.getNoobs().add(user);
        return seat;
    }

    @Override
    synchronized public String changeSeats(String newSeat, String user) {
        if(PLAYER.equals(newSeat)) {
            if (!board.getNoobs().contains(user)) board.getNoobs().add(user);
        } else {
            board.getNoobs().remove(user);
        }
        return newSeat;
    }

    @Override public void initMove(Move move) { } //TODO
    public RPSBoard getStatus() { return board; } //TODO update timer

    private void unfreeeze() {
        board.setTimeStart(System.currentTimeMillis());
        scheduler.schedule(updater, board.getTime()+1, TimeUnit.SECONDS);
    }

    private int timeLeft(long now) {
        int delta = (int)(now - board.getTimeStart());
        return board.getTime() - delta/1000;
    }

    private void freeze() {
        long now = System.currentTimeMillis();
        int wait = timeLeft(now);
        if (wait > 0) {
            board.setTime(timeLeft(now));
            board.setTimeStart(now);
        } else {
            doUpdate();
        }
    }

    synchronized public RPSBoard pause(MoveRepo moveRepo) {
        //TODO permission check
        switch (board.getState()) {
            case RPSBoard.VIRGIN:
                startGame(GAME_LENGTH);
                break; // TODO GAME_LENGTH make adjustable
            case RPSBoard.PAUSED:
                break;
            case RPSBoard.MOVING:
                board.setState(RPSBoard.PAUSED);
                freeze();
                break;
            case RPSBoard.ANNOUNCING:
                board.setState(RPSBoard.STOPPED);
                freeze();
                break;
            case RPSBoard.STOPPED:
                break;
            default: break;
        }
        return board;
    }

    synchronized public RPSBoard resume(MoveRepo moveRepo) {
        switch (board.getState()) {
            case RPSBoard.VIRGIN:
                startGame(GAME_LENGTH);
                break; // TODO GAME_LENGTH make adjustable
            case RPSBoard.PAUSED:
                board.setState(RPSBoard.MOVING);
                unfreeeze();
                break;
            case RPSBoard.MOVING:
                break;
            case RPSBoard.ANNOUNCING:
                break;
            case RPSBoard.STOPPED:
                board.setState(RPSBoard.ANNOUNCING);
                unfreeeze();
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

    private void makeResults() {
        //TODO adjust the ladder, store the results
        board.setState(ANNOUNCING);
        board.setTime(ROUND_INTERVAL);
        unfreeeze();
    }

    synchronized private void doUpdate() {
        int wait = timeLeft(System.currentTimeMillis());
        if (wait > 0) { // premature, reschedule for later
            scheduler.schedule(updater, wait+1, TimeUnit.SECONDS);
            return;
        }
        switch (board.getState()) {
            case ANNOUNCING -> startGame(GAME_LENGTH);
            case MOVING -> makeResults();
        }
    }

    Runnable updater = new Runnable() {
        @Override public void run() {
            doUpdate();
        }
    };

    synchronized public RPSBoard startGame(int time) {
        //TODO evict overly idle players
        board.getLadder().addAll(board.getNoobs());
        pendingMoves = new HashMap<>(); //wipe out old moves
        board.setTime(time);
        board.setTimeStart(System.currentTimeMillis());
        scheduler.schedule(updater, time+1, TimeUnit.SECONDS);
        board.setState(MOVING);
        return board;
    }
}
