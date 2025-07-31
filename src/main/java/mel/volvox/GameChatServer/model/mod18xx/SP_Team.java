package mel.volvox.GameChatServer.model.mod18xx;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class SP_Team {
    @EmbeddedId
    SP_TeamID id;
    String displayName;
}
