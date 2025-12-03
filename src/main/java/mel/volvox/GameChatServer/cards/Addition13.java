package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Placement;
import mel.volvox.GameChatServer.comm.cards.Tableau;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.UUID;

public class Addition13 extends CardGame {
    Tableau show;
    List<Card> deck;
    Placement table;

    @Override
    public void init() {
        String id = UUID.randomUUID().toString();
        show = new Tableau();
        deck = Cards.shuffle(52);
        table = new Placement();
        Cards.deal(deck, table.getDeck(), 10, true);
        table.setGridHeight(2);
        table.setGridWidth(5);
        table.setX(265);
        table.setY(165);
        table.setId("main");
        show.setId(id);
        show.getPlacements().add(table);
    }

    @Override
    public Tableau getLayout() {
        return show;
    }

    @Override
    public Tableau select(String id, int gridX, int gridY) {
        throw new IllegalStateException("TODO click card");
    }
}
