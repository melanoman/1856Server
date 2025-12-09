package mel.volvox.GameChatServer.repository;

import mel.volvox.GameChatServer.model.cards.CardRules;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRulesRepo extends JpaRepository<CardRules,String> {
}
