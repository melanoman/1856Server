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

    public static SP_RaceID NULL = new SP_RaceID("", -1, -1);
}
