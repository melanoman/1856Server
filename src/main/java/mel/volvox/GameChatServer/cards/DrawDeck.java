package mel.volvox.GameChatServer.cards;

import lombok.Getter;
import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Placement;

import java.util.ArrayList;
import java.util.List;

/**
 * This class ties a deck of cards with a placement show one face down card that vanishes
 * when the last card is drawn.
 */
public class DrawDeck {
    @Getter
    private final List<Card> deck;
    @Getter
    private final Placement placement = new Placement();

    /**
     * @param size number of cards in the deck
     */
    public DrawDeck(int size) {
        deck = Cards.shuffle(size);
        placement.getDeck().add(new Card(0, false, false, false));
        placement.setId("draw");
    }

    public Card draw() {
        if (deck.isEmpty()) return null;
        Card out = deck.remove(0);
        if (deck.isEmpty()) {
            placement.getDeck().remove(0);
        }
        return out;
    }

    public void dealOnto(List<Card> target, boolean expose) {
        Card c = draw();
        if(c == null) return;
        if(expose) c.setExposed(true); //does not un-expose
        target.add(0, c);
    }

    public int size() {
        return deck.size();
    }
}
