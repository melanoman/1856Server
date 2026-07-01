package mel.volvox.GameChatServer.model.xx1856;


import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class Move {
    @EmbeddedId
    MoveID id;
    String action;
    String player;
    String corp;
    int amount;
    String detail;
    boolean isTop;
}
