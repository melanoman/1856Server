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

    private String moveName(int move) {
        return switch (move) {
            case ROCKVAL -> ROCK;
            case PAPERVAL -> PAPER;
            case SCISSORVAL -> SCISSORS;
            default -> "";
        };
    }

    private boolean calculateParity(int position, boolean odd) {
        int offset = odd ? position+1 : position;
        return 0 == (offset%4)/2;
    }

    private void addResult(
               List<RPSResult>list, boolean odd,
               String self, String opponent,
               String choice, String oChoice,
               String type, int delta
    ) {
        RPSResult out = new RPSResult();
        out.setPosition(list.size());
        out.setParity(calculateParity(list.size(), odd));
        out.setPlayer(self);
        out.setOpponent(opponent);
        out.setChoice(choice);
        out.setOChoice(oChoice);
        out.setType(type);
        out.setDelta(delta);

        list.add(out);
    }

    private void addResultPair(
            List<RPSResult>list, boolean odd, boolean upset, boolean forfeit, boolean draw,
            String p1, String p2, int c1, int c2
    ) {
        int delta = upset ? 1 : 0;
        String winner = upset ? p2 : p1;
        String loser = upset ? p1 : p2;
        String winMove = moveName(upset ? c2 : c1);
        String loseMove = moveName(upset ? c1 : c2);
        String winText, loseText;
        if (forfeit) {
            winText = draw ? "2xforfeit" : "forfeitee";
            loseText = draw ? "2xforfeit" : "forfeiter";
        } else {
            winText = draw ? "draw" : "win";
            loseText = draw ? "draw" : "lose";
        }
        addResult(list, odd, winner, loser, winMove, loseMove, winText, delta);
        addResult(list, odd, loser, winner, loseMove, winMove, loseText, -delta);
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
            String player = iter.next().getPlayer();
            addResult(newResults, false, player, "",
                    pendingMoves.get(player), "", "Bye", 0);
        }
        while (iter.hasNext()) {
            count++;
            String firstPlayer = iter.next().getPlayer();
            if (iter.hasNext()) {
                count++;
                String secondPlayer = iter.next().getPlayer();
                int firstMove = moveValue(pendingMoves.get(firstPlayer));
                int secondMove = moveValue(pendingMoves.get(secondPlayer));
                boolean upset;
                boolean forfeit = false;
                if(secondMove == NOMOVE) {
                    upset = false;
                    forfeit = true;
                } else if(firstMove == NOMOVE) {
                    upset = true;
                    forfeit = true;
                } else {
                    upset = (secondMove == ROCKVAL && firstMove == SCISSORVAL)  ||
                            (secondMove == PAPERVAL && firstMove == ROCKVAL) ||
                            (secondMove == SCISSORVAL && firstMove == PAPERVAL);
                }
                addResultPair(newResults, !board.isOddRound(), upset, forfeit, firstMove == secondMove,
                        firstPlayer, secondPlayer, firstMove, secondMove);
                //TODO keep idle player counter
                //TODO keep stats
            } else {
                addResult(newResults, !board.isOddRound(), firstPlayer, "",
                        pendingMoves.get(firstPlayer), "", "Bye", 0);
            }
        }
        addNoobs(newResults);
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

    private void addNoob(List<RPSResult> results, String noob) {
        int count = board.getResults().size();
        addResult(results, board.isOddRound(), noob,
                "", "", "", "New", 0);
    }

    private void addNoobs(List<RPSResult> results) {
        Set<String> players = new HashSet<>();
        for(RPSResult result: board.getResults()) players.add(result.getPlayer());
        for(String noob: board.getNoobs()) {
            if (players.contains(noob)) continue;
            addNoob(results, noob);
        }
        board.setNoobs(new ArrayList<>());
    }

    synchronized public RPSBoard startGame(int time) {
        //TODO evict overly idle players
        //TODO make sure game state is startable (concurrency protection)

        addNoobs(board.getResults());
        board.setTime(time);
        board.setTimeStart(System.currentTimeMillis());
        scheduler.schedule(updater, time+1, TimeUnit.SECONDS);
        board.setState(MOVING);
        return board;
    }
}
