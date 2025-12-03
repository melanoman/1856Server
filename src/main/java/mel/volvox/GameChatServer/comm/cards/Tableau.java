package mel.volvox.GameChatServer.comm.cards;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
public class Tableau {
    public static final int NONE = 0;
    public static final int LOSE = -1;
    public static final int WIN = 1;

    String id;
    List<Placement> placements = new ArrayList<>();
    int result = NONE;
}
