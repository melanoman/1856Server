package mel.volvox.GameChatServer.comm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mel.volvox.GameChatServer.model.Human;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class HumanComm {
    public static final HumanComm NOBODY = new HumanComm("", "", "");
    String accountName;
    String displayName;
    String contact;
    // Like a human, but password is excluded for security

    public HumanComm(Human h) {
        accountName = h.getAccountName();
        displayName = h.getDisplayName();
        contact = h.getContact();
    }
}
