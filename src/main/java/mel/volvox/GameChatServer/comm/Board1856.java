package mel.volvox.GameChatServer.comm;

import lombok.Data;
import lombok.NoArgsConstructor;
import mel.volvox.GameChatServer.model.train.TrainWallet;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
public class Board1856 {
  private List<String> players = new ArrayList<>();
  private List<TrainWallet> wallets = new ArrayList<>();


}
