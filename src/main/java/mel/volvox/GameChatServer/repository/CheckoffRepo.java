package mel.volvox.GameChatServer.repository;

import mel.volvox.GameChatServer.model.stat.Checkoff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CheckoffRepo extends JpaRepository<Checkoff, Integer> {
    List<Checkoff> findByName(String name);
}
