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
}
