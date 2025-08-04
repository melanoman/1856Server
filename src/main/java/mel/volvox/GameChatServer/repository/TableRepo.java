package mel.volvox.GameChatServer.repository;

import mel.volvox.GameChatServer.model.seating.GameTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TableRepo extends JpaRepository<GameTable,String> {
    List<GameTable> findByType(String type);
}
