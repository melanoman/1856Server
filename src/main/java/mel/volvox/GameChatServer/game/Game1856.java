package mel.volvox.GameChatServer.game;

import lombok.Setter;
import mel.volvox.GameChatServer.repository.TrainRepo;

public class Game1856 extends AbstractGame {
    private enum Era { GATHER, AUCTION, STOCK, OP, DONE };
    private Era era = Era.GATHER;
    @Setter TrainRepo repo; // set by controller
}
