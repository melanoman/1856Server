package mel.volvox.GameChatServer.model.stat;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class Checkoff {
    @Id Long date; // currentTimeInMillis
    String name;
    String task;
}
