package mel.volvox.GameChatServer.model.xx1856;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Embeddable
public class xxMoveID {
    String gameName;
    int serialNumber;
}
