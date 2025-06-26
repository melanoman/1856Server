package mel.volvox.GameChatServer.repository;

import jakarta.transaction.Transactional;
import mel.volvox.GameChatServer.model.SP_Team;
import mel.volvox.GameChatServer.model.SP_TeamID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepo extends JpaRepository<SP_Team, SP_TeamID> {
    @Transactional
    void deleteAllByIdLeagueID(String leagueID);
}
