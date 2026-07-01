package mel.volvox.GameChatServer.repository;

import mel.volvox.GameChatServer.model.xx1856.Move;
import mel.volvox.GameChatServer.model.xx1856.MoveID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface xx1856Repo extends JpaRepository<Move, MoveID> {
    List<Move> findByIdGameNameOrderByIdSerialNumberAsc(String channel);
}
