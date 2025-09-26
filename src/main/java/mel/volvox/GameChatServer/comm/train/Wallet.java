package mel.volvox.GameChatServer.comm.train;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
public class Wallet {
    String name;
    int cash;
    List<Priv> privates = new ArrayList<>();
    List<Stock> stocks = new ArrayList<>();
    List<String> blocks = new ArrayList<>();

    public int countCerts() {
        int count = privates.size();
        for(Stock stock:getStocks()) {
            count += stock.getAmount();
            if (stock.isPresident) count--;
        }
        return count;
    }
}
