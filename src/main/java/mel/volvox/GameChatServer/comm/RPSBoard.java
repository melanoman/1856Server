package mel.volvox.GameChatServer.comm;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RPSBoard {
    // STATE int-as-enum
    public static final int PAUSED = 0;
    public static final int MOVING = 1;
    public static final int ANNOUNCING = 2;
    public static final int STOPPED = 3;

    public static final String ROCK = "rock";
    public static final String PAPER = "paper";
    public static final String SCISSORS = "scissors";

    private int state = MOVING;
    private int time = 2000; //TODO get from admin
    private long timeStart = System.currentTimeMillis();

    List<String> noobs = new ArrayList<>();
    List<String> leaving = new ArrayList<>();
    List<String> ladder = new ArrayList<>();
}
