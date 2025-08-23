package mel.volvox.GameChatServer.model.train;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class TrainMove {
    @EmbeddedId
    TrainMoveID id;
    String action;
    String player;
    String corp;
    int amount;
    int oldPassCount;
    boolean isFollow; // true if undo should continue past this move
}
