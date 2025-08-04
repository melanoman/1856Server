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
public class Seat {
    @EmbeddedId
    SeatID id;
    String account; //account of the player
}
