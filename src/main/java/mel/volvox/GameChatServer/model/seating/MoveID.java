package mel.volvox.GameChatServer.model.seating;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Embeddable
public class MoveID {
    String tableName;
    int serialNumber;
}
