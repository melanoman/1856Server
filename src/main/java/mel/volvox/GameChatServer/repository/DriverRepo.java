package mel.volvox.GameChatServer.repository;

import jakarta.transaction.Transactional;
import mel.volvox.GameChatServer.model.SP_Driver;
import mel.volvox.GameChatServer.model.SP_DriverID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverRepo extends JpaRepository<SP_Driver, SP_DriverID> {
    int countByIdLeagueIDAndIdTeamID(String leagueID, String teamID);
    @Transactional
    void deleteAllByIdLeagueID(String leagueID);
}
