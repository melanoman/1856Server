package mel.volvox.GameChatServer.comm.train;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Bid {
    String corp; // I know, it's a company.  Corp is short for company.
    int amount; // 0 = owned
}
