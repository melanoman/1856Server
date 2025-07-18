package mel.volvox.GameChatServer.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SP_Preview {
    SP_Race race;
    List<SP_DriverStatus> drivers;

    public static SP_Preview NULL = new SP_Preview(SP_Race.NULL, Collections.emptyList());
}
