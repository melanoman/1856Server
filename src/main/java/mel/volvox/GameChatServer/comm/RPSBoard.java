package mel.volvox.GameChatServer.comm;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RPSBoard {
    // STATE int-as-enum
    public static final int VIRGIN = -1;
    public static final int PAUSED = 0;
    public static final int MOVING = 1;
    public static final int ANNOUNCING = 2;
    public static final int STOPPED = 3;

    public static final String ROCK = "rock";
    public static final String PAPER = "paper";
    public static final String SCISSORS = "scissors";

    private int state = VIRGIN;
    private int time = 0;
    private long timeStart = System.currentTimeMillis();
    private boolean oddRound;

    List<String> noobs = new ArrayList<>();
    List<String> leaving = new ArrayList<>();
    List<String> ladder = new ArrayList<>();
}
