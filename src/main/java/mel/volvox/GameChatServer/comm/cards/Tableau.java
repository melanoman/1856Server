package mel.volvox.GameChatServer.comm.cards;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
public class Tableau {
    String id;
    List<Placement> placements = new ArrayList<>();
}
