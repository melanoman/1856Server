package mel.volvox.GameChatServer.repository;

import mel.volvox.GameChatServer.model.Human;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HumanRepo extends JpaRepository<Human, String> {
}
