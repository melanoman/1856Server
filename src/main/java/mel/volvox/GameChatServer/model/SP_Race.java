package mel.volvox.GameChatServer.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// TEMPORARY class to play around with field names before v1
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class SP_Race {
    @Id
    String id;
    String leagueID;
    String displayName;
    String trackName;
    int season;
    int multiplier;
}
