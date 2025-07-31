package mel.volvox.GameChatServer.repository;

import jakarta.transaction.Transactional;
import mel.volvox.GameChatServer.model.mod18xx.SP_Season;
import mel.volvox.GameChatServer.model.mod18xx.SP_SeasonID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeasonRepo extends JpaRepository<SP_Season, SP_SeasonID> {
    int countByIdLeagueID(String leagueID);
    @Transactional
    void deleteAllByIdLeagueID(String leagueID);
    List<SP_Season> findAllByIdLeagueID(String leagueID);
}
