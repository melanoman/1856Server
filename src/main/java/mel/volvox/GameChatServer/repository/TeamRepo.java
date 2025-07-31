package mel.volvox.GameChatServer.repository;

import jakarta.transaction.Transactional;
import mel.volvox.GameChatServer.model.sp.SP_Team;
import mel.volvox.GameChatServer.model.sp.SP_TeamID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepo extends JpaRepository<SP_Team, SP_TeamID> {
    @Transactional
    void deleteAllByIdLeagueID(String leagueID);
    @Transactional
    void deleteAllByIdLeagueIDAndIdTeamID(String leagueID, String teamID);
}
