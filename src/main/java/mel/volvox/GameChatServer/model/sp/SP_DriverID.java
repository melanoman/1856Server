package mel.volvox.GameChatServer.model.sp;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Embeddable
public class SP_DriverID implements Serializable {
    String leagueID;
    String teamID;
    int driverNumber;
}
