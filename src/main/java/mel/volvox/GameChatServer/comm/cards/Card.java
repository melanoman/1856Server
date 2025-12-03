package mel.volvox.GameChatServer.comm.cards;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    int id;
    boolean exposed = true;
    boolean inverted = false;
    boolean highlight = false;

    public int cv1to13() {
        return id%13 + 1;
    }
}
