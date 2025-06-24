package mel.volvox.GameChatServer.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// TEMPORARY class to play around with field names before v1
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class SP_Race {
    @EmbeddedId
    SP_RaceID id;
    String displayName;
    String trackName;
    int multiplier;
}
