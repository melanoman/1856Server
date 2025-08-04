package mel.volvox.GameChatServer.repository;

import mel.volvox.GameChatServer.model.seating.Move;
import mel.volvox.GameChatServer.model.seating.MoveID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MoveRepo extends JpaRepository<Move, MoveID> {
    List<Move> findAllByIdTableNameOrderByIdSerialNumber(String tableName);
}
