package mel.volvox.GameChatServer.model.xx1856;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Embeddable
public class MoveID {
    String gameName;
    int serialNumber;
}
