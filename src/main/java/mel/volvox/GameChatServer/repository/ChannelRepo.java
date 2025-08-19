package mel.volvox.GameChatServer.repository;

import mel.volvox.GameChatServer.model.seating.Channel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChannelRepo extends JpaRepository<Channel,String> {
    List<Channel> findByType(String type);
    Optional<Channel> findByNameAndType(String name, String type);
    boolean existsByNameAndType(String name, String type);
}
