package mel.volvox.GameChatServer.comm.train;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Priv {
    String corp; // I know, it's a company.  Corp is short for company.
    int amount; // >5 = bid, 0 = power used, 1-3 = power abvailable (tokens left for tunnel/bridge)
}
