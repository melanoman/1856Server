package mel.volvox.GameChatServer.repository;

import mel.volvox.GameChatServer.model.train.TrainMove;
import mel.volvox.GameChatServer.model.train.TrainMoveID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainRepo extends JpaRepository<TrainMove, TrainMoveID> {
    List<TrainMove> findByIdChannelOrderByIdSerialNumberAsc(String channel);
}
