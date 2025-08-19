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
public class Message {
    @EmbeddedId
    MessageID id;
    String text;
    String author;
}
