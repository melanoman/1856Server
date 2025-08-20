package mel.volvox.GameChatServer.model.train;


import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Embeddable
public class TrainMoveID {
    String channel;
    int serialNumber;
}
