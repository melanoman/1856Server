package mel.volvox.GameChatServer.repository;

import mel.volvox.GameChatServer.model.sp.League;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeagueRepo extends JpaRepository<League, String> {
}
