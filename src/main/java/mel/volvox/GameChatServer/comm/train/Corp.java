package mel.volvox.GameChatServer.comm.train;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Corp {
    public Corp(String name, int startTokens) {
        this.name = name;
        this.tokensLeft = startTokens;
    }

    public Corp dup() {
        List<Priv> newPrivs = new ArrayList<>();
        for (Priv p: privates) newPrivs.add(p.dup());
        return new Corp(name, par, bankShares, price, poolShares, cash, tokensLeft,
                prez, newPrivs, portRights, bridgeRights, tunnelRights);
    }

    String name; // 2-3 letter abbreviation. not full text
    int par = 0;
    int bankShares = 10;
    int price = 0;
    int poolShares = 0;
    int cash = 0;
    int tokensLeft = 0;
    String prez = "";
    List<Priv> privates = new ArrayList<>();
    boolean portRights = false;
    boolean bridgeRights = false;
    boolean tunnelRights = false;
}
