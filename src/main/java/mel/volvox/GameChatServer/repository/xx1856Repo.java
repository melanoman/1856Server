package mel.volvox.GameChatServer.repository;

import mel.volvox.GameChatServer.model.xx1856.xxMove;
import mel.volvox.GameChatServer.model.xx1856.xxMoveID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface xx1856Repo extends JpaRepository<xxMove, xxMoveID> {
    List<xxMove> findByIdGameNameOrderByIdSerialNumberAsc(String channel);
}
