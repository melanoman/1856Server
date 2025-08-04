package mel.volvox.GameChatServer.model.seating;

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
    String player; // empty string (not null) for robot events
    String data; // game-specific gunk
}
