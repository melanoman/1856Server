package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Placement;
import mel.volvox.GameChatServer.comm.cards.Tableau;

import java.util.List;

public class Block11 extends CardGame {
    List<Card> deck;
    Placement main = new Placement();
    boolean seeded = false;

    @Override
    public void init() {
        super.init();
        deck = Cards.shuffle(52);
        while(main.getDeck().size() < 12) {
            Card c = deck.remove(0);
            if(c.isFace()) deck.add(c);
            else main.getDeck().add(c);
        }
        main.setGridHeight(3);
        main.setGridWidth(4);
        main.setX(300);
        main.setY(140);
        main.setId(MAIN);
        table.getPlacements().add(main);
    }

    @Override
    public Tableau select(String id, int gridX, int gridY) {
        return table;
    }
}
