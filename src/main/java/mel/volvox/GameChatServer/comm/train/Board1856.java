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
   List<Corp> corps = new ArrayList<>();
   List<Integer> trains = new ArrayList<>();

   int bankCash = 10500; // 12k minus 1500 starting cash

   String currentPlayer = "";
   String currentCorp = "";
   int passCount = 0;
   String priorityHolder = "";

   int auctionDiscount = 0; //$5 off per allpass
   int currentOpRound = 0; // count DOWN not up
   int maxOpRounds = 0;

   //operating scratchpad
   boolean loanTaken = false; // clear after each corp, prevent 2xloan same turn
   boolean tilePlayed = false; // clear after revenue, prevent extra tile
   boolean tokenPlayed = false; // clear after revenue, prevent extra token
}
