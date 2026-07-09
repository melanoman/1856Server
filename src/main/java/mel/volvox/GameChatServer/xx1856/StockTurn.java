package mel.volvox.GameChatServer.xx1856;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StockTurn {
    boolean buyFirst;
    String buyType; // bank or pool
    String buyCorp;
    int buyPar;
    List<Stock> salesList;
}
