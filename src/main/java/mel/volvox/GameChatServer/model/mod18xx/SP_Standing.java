package mel.volvox.GameChatServer.model.mod18xx;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SP_Standing {
    int place;
    int points;
    String teamID;
    String driverName; //empty string for team standings
}
