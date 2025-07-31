package mel.volvox.GameChatServer.model.mod18xx;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An entity class represents a table in a relational database
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SP_DriverStatus {
    int serialNumber;
    SP_Driver driver;
    int remainingInjury; // number of race left in hospital
    int seasonPoints;
    int allTimePoints;
    int experience;
}
