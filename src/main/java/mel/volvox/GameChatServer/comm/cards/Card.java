package mel.volvox.GameChatServer.comm.cards;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    public static final int NO_SUIT = -1;

    int id;
    boolean exposed = true;
    boolean inverted = false;
    boolean highlight = false;

    public int cv1to13() {
        return id%13 + 1;
    }

    public boolean isFace() {
        int rank = id%13 + 1;
        return rank > 10;
    }

    public int suit() {
        return id/13;
    }
}
