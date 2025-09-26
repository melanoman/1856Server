package mel.volvox.GameChatServer.comm.train;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Stock {
    String corp;
    int amount;
    boolean isPresident;
}
