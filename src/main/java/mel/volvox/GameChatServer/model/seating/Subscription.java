package mel.volvox.GameChatServer.model.seating;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class Subscription {
    @Id
    String tableName;
    String accountName;
}
