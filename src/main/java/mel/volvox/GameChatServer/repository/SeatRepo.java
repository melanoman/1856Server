package mel.volvox.GameChatServer.repository;

import mel.volvox.GameChatServer.model.seating.Seat;
import mel.volvox.GameChatServer.model.seating.SeatID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepo extends JpaRepository<Seat, SeatID>  {
    List<Seat> findByIdTableName(String name);
}
