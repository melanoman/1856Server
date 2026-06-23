package mel.volvox.GameChatServer.xx1856;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import mel.volvox.GameChatServer.xx1856.xx1856Game.Era;

@Data
@NoArgsConstructor
public class xx1856Board {
    String name;
    int undoCount;

    int bank;
    List<xxPlayer> players = new ArrayList<>();
    List<xxCorp> corps = new ArrayList<>();
    List<xxBid> bids = new ArrayList<>(); //only valid during auction

    String phase = Era.GATHER.name();

}
