package mel.volvox.GameChatServer.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Embeddable
public class SP_RaceID implements Serializable {
    String leagueID;
    int seasonNumber;
    int raceNumber;
}
