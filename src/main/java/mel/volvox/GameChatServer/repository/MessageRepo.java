package mel.volvox.GameChatServer.repository;

import mel.volvox.GameChatServer.model.seating.Message;
import mel.volvox.GameChatServer.model.seating.MessageID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepo extends JpaRepository<Message, MessageID> {
    Message findFirstByOrderByIdSerialNumberDesc();
    List<Message> findByIdTableNameOrderByIdSerialNumberDesc(String name, Limit limit);
}
