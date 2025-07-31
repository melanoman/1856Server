package mel.volvox.GameChatServer.model.sp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SP_Standings {
    String type; // {driver, team}
    String scope; // {all, season, season1, season2, ...} 'season' without a number is for current season
    int seasonNumber;  // TODO figure out how to communicate current vs old season
    List<SP_Standing> standings;
}
