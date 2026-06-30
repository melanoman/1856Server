package mel.volvox.GameChatServer.xx1856;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Player {
    String name;
    int cash;
    List<Stock> shares = new ArrayList<>();
    List<String> privs = new ArrayList<>();
    List<String> blocks = new ArrayList<>();
}
