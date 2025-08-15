package mel.volvox.GameChatServer.comm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RPSResult {
    int position;
    boolean parity;
    String player;
    int delta;
    String type;
    String opponent;
    String choice;
    String oChoice;
}
