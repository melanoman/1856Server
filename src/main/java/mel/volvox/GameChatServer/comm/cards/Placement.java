package mel.volvox.GameChatServer.comm.cards;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@Data
public class Placement {
    public static final int NORMAL = 0;
    public static final int NO_SPLAY = 0;
    public static final int FACE_UP = 0;
    public static final int FACE_DOWN = 1;

    String id;
    List<Card> deck = new ArrayList<>();
    int gridWidth;
    int gridHeight;
    int x;
    int y;

    public static Placement drawDeck(String id, int size) {
        Placement out = new Placement();
        out.id = (id == null) ? UUID.randomUUID().toString() : id;
        out.getDeck().add(new Card(size, false, false, false));
        return out;
    }

    public boolean isEmpty() {
        for(Card c:deck) {
            if(c != null) return false;
        }
        return true;
    }
}
