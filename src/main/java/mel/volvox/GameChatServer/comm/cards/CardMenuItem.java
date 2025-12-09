package mel.volvox.GameChatServer.comm.cards;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardMenuItem {
    String name;
    List<String> sub;
}
