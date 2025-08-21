package mel.volvox.GameChatServer.comm.train;

import lombok.Data;
import lombok.NoArgsConstructor;
import mel.volvox.GameChatServer.game.Game1856;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
public class Board1856 {
   List<String> players = new ArrayList<>();
   List<Wallet> wallets = new ArrayList<>();
   String name;
   String phase = Game1856.Era.GATHER.name();
   int moveNumber;
   int undoCount;
}
