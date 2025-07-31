package mel.volvox.GameChatServer.repository;

import jakarta.transaction.Transactional;
import mel.volvox.GameChatServer.model.sp.SP_Driver;
import mel.volvox.GameChatServer.model.sp.SP_DriverID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverRepo extends JpaRepository<SP_Driver, SP_DriverID> {

    int countByIdLeagueIDAndIdTeamID(String leagueID, String teamID);
    @Transactional
    void deleteAllByIdLeagueID(String leagueID);
    @Transactional
    void deleteAllByIdLeagueIDAndIdTeamID(String leagueID, String teamID);

    List<SP_Driver> findAllByIdLeagueID(String league);
}
