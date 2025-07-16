package mel.volvox.GameChatServer.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class SP_Result {
    @EmbeddedId
    SP_ResultID id;
    String teamID;
    int driverNumber;
    String driverName;
    boolean finished;
    int injuryDuration;
}
