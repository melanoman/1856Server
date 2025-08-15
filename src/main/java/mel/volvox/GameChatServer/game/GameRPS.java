package mel.volvox.GameChatServer.game;

import mel.volvox.GameChatServer.comm.RPSBoard;
import mel.volvox.GameChatServer.comm.RPSResult;
import mel.volvox.GameChatServer.model.seating.Move;
import mel.volvox.GameChatServer.repository.MoveRepo;

import java.util.*;
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
    synchronized public String changeSeats(String newSeat, String user) {
        if(PLAYER.equals(newSeat)) {
            if (!board.getNoobs().contains(user)) board.getNoobs().add(user);
        } else {
            //System.out.println("Ladder remove:"+user); //TODO fix remove
            //board.getNoobs().remove(user);
        }
        return newSeat;
    }

    @Override public void initMove(Move move) { } //TODO
    public RPSBoard getStatus() { return board; }

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

    synchronized public String chooseThrow(String user, String choice) {
        pendingMoves.put(user, choice);
        return choice;
    }

    final private int NOMOVE = 0;
    final private int ROCKVAL = 1;
    final private int PAPERVAL = 2;
    final private int SCISSORVAL =3;

    private int moveValue(String move) {
        if (move == null) return NOMOVE;  //TODO can this be deleted? (is string switch null safe?)
        return switch (move) {
            case RPSBoard.ROCK -> ROCKVAL;
            case RPSBoard.PAPER -> PAPERVAL;
            case RPSBoard.SCISSORS -> SCISSORVAL;
            default -> NOMOVE;
        };
    }

    private boolean upsetWin(String firstPlayer, String secondPlayer) {
        int secondMove = moveValue(pendingMoves.get(secondPlayer));
        if (secondMove == NOMOVE) return false;
        int firstMove = moveValue(pendingMoves.get(firstPlayer));
        if (firstMove == NOMOVE) return true;
        if (firstMove == secondMove) return false;
        return  (secondMove == ROCKVAL && firstMove == SCISSORVAL)  ||
                (secondMove == PAPERVAL && firstMove == ROCKVAL) ||
                (secondMove == SCISSORVAL && firstMove == PAPERVAL);
    }

    void addResult(String name, int count, List<RPSResult> results) {
        int tmp = count + (board.isOddRound()?0:-1);
        results.add(new RPSResult(count+1, 0 == ((tmp%4)/2), name));
    }

    synchronized private void makeResults() {
        if (board.getResults().isEmpty() ) {
            board.setState(STOPPED);
            return;
        }
        List<RPSResult> newResults = new ArrayList<>();
        Iterator<RPSResult> iter = board.getResults().iterator();
        int count = -1;
        if (board.isOddRound()) {
            count = 0;
            addResult(iter.next().getName(), 0, newResults);
        }
        while (iter.hasNext()) {
            count++;
            String firstPlayer = iter.next().getName();
            if (iter.hasNext()) {
                String secondPlayer = iter.next().getName();
                if (upsetWin(firstPlayer, secondPlayer)) {
                    addResult(secondPlayer, count, newResults);
                    addResult(firstPlayer, count, newResults);
                } else {
                    addResult(firstPlayer, count, newResults);
                    addResult(secondPlayer, count, newResults);
                }
                //TODO keep idle player counter
            } else {
                addResult(firstPlayer, count, newResults);
            }
        }
        addNoobs();
        pendingMoves = new HashMap<>();
        board.setTime(ROUND_INTERVAL);
        board.setOddRound(!board.isOddRound());
        board.setState(ANNOUNCING);
        board.setResults(newResults);
        if (board.getResults().size() == 2) {
            board.setOddRound(false);
        } else if (board.getResults().size() < 2) {
            board.setState(STOPPED);
        }
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

    synchronized public RPSBoard firstGame(int time) {
        board.setOddRound(false);
        return startGame(time);
    }

    private void addNoob(String noob) {
        int count = board.getResults().size();
        addResult(noob, count, board.getResults());
    }

    private void addNoobs() {
        Set<String> players = new HashSet<>();
        for(RPSResult result: board.getResults()) players.add(result.getName());
        for(String noob: board.getNoobs()) {
            if (players.contains(noob)) continue;
            addNoob(noob);
        }
        board.setNoobs(new ArrayList<>());
    }

    synchronized public RPSBoard startGame(int time) {
        //TODO evict overly idle players
        //TODO make sure game state is startable (concurrency protection)

        addNoobs();
        board.setTime(time);
        board.setTimeStart(System.currentTimeMillis());
        scheduler.schedule(updater, time+1, TimeUnit.SECONDS);
        board.setState(MOVING);
        return board;
    }
}
