package mel.volvox.GameChatServer.xx1856;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import mel.volvox.GameChatServer.xx1856.Game.Era;

@Data
@NoArgsConstructor
public class Board {
    String name;
    int moveNumber = 0;
    int undoCount = 0;

    int bank;
    List<Player> players = new ArrayList<>();
    List<Corp> corps = new ArrayList<>();
    List<Bid> bids = new ArrayList<>(); //only valid during auction

    String phase = Era.GATHER.name();
    String activity = ""; // for interactive events during phases, basically subPhase
    String currentPlayer;
    String priorityPlayer;
    String currentCorp;  // used for priv during auction only

    int OR;
    int maxOR;

    // TODO reconsider obscure items
    int flosDiscount = 0;
}
