package mel.volvox.GameChatServer.comm.train;

import lombok.Data;
import lombok.NoArgsConstructor;
import mel.volvox.GameChatServer.game.Game1856;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
public class Board1856 {
   String name;
   int moveNumber;
   int undoCount;

   String phase = Game1856.Era.GATHER.name();
   String event = "";
   List<String> players = new ArrayList<>();
   List<Wallet> wallets = new ArrayList<>();

   String currentPlayer = "";
   String currentCorp = "";
   int passCount = 0;
   String priorityHolder = "";

   int auctionDiscount = 0; //$5 off per allpass
   int remainingOpRounds = 0; // count DOWN not up
}
