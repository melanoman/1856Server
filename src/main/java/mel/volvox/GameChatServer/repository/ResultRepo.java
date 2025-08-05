package mel.volvox.GameChatServer.repository;

import mel.volvox.GameChatServer.model.sp.SP_Result;
import mel.volvox.GameChatServer.model.sp.SP_ResultID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResultRepo extends JpaRepository<SP_Result, SP_ResultID> {
    List<SP_Result> findAllByIdLeagueIDAndIdSeasonNumberAndIdRaceNumber(
        String leagueID, int seasonNumber, int raceNumber
    );
    List<SP_Result> findAllByIdLeagueID(String league);
    List<SP_Result> findAllByIdLeagueIDAndIdSeasonNumber(String league, int seasonNumber);
    void deleteAllByIdLeagueID(String league);
}

