package mel.volvox.GameChatServer.xx1856;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Priv {
    String name;
    int price;
    int dividend;

    public static List<Priv> PRIVS = List.of(
            new Priv("FLOS", 20, 5),
            new Priv("WS", 40, 10),
            new Priv("CAN", 50, 10),
            new Priv("GLS", 70, 15),
            new Priv("NIAG", 100, 20),
            new Priv("STC", 100, 20)
    );

    public static Priv findPriv(String priv) {
        for(Priv p:PRIVS) {
            if(p.name.equals(priv)) return p;
        }
        throw new IllegalStateException("Private Company not found: " +priv);
    }
}