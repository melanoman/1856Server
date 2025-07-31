package mel.volvox.GameChatServer.model.mod18xx;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Embeddable
public class SP_TeamID {
    String leagueID;
    String teamID;
}
