package mel.volvox.GameChatServer.repository;

import jakarta.transaction.Transactional;
import mel.volvox.GameChatServer.model.SP_Race;
import mel.volvox.GameChatServer.model.SP_RaceID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RaceRepo extends JpaRepository<SP_Race, SP_RaceID> {
    int countByIdLeagueIDAndIdSeasonNumber(String leagueID, int seasonNumber);
    @Transactional
    void deleteAllByIdLeagueID(String leagueID);
}
