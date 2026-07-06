package mel.volvox.GameChatServer.xx1856;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mel.volvox.GameChatServer.comm.train.StockPrice;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Corp {
    String name;
    int par = 0;
    int bankShares = 0; // zero until set par
    int poolShares = 0;
    StockPrice price = new StockPrice(83, 0, 0);
    int tokensMax;
    int tokensUsed = 0;
    List <String> privs = new ArrayList<>();
    List <String> trains = new ArrayList<>();
    int loans;

    Corp(String name, int tokens) {
        this.name = name;
        this.tokensMax = tokens;
    }

    public static final List<Corp> INIT = List.of(
            new Corp("BBG", 3),
            new Corp("CA", 3),
            new Corp("CPR", 4),
            new Corp("CV", 3),
            new Corp("GT", 4),
            new Corp("GW", 4),
            new Corp("LPS", 2),
            new Corp("TGB", 2),
            new Corp("THB", 2),
            new Corp("WGB", 2),
            new Corp("WR", 3)
    );
}
