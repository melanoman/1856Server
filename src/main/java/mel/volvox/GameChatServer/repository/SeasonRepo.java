package mel.volvox.GameChatServer.repository;

import mel.volvox.GameChatServer.model.SP_Season;
import mel.volvox.GameChatServer.model.SP_SeasonID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeasonRepo extends JpaRepository<SP_Season, SP_SeasonID> {
    int countByIdLeagueID(String leagueID);
}
