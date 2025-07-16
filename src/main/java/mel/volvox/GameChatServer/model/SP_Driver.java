package mel.volvox.GameChatServer.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An entity class represents a table in a relational database
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class SP_Driver {
    @EmbeddedId
    SP_DriverID id;
    String displayName;
    int birthday;
    boolean lateBirth;
}
