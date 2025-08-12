package mel.volvox.GameChatServer.comm;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RPSBoard implements Board {
    // STATE int-as-enum
    public static final int PAUSED = 0;
    public static final int MOVING = 1;
    public static final int ANNOUNCING = 2;

    private int state = PAUSED;
    private int time = 90; // TODO make this part of game start

    List<String> noobs = new ArrayList<>();
    List<String> leaving = new ArrayList<>();
    List<String> ladder = new ArrayList<>();
}
