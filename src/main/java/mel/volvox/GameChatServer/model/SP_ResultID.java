package mel.volvox.GameChatServer.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Embeddable
public class SP_ResultID {
    String leagueID;
    int seasonNumber;
    int raceNumber;
    int place;
}
