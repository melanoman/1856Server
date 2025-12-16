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

    private static final int SPLAY_HEIGHT = 30;
    private static final int SPLAY_WIDTH = 5;
    /**
     * @param size number of cards in the deck
     */
    public DrawDeck(int size) {
        deck = Cards.shuffle(size);
        placement.getDeck().add(new Card(0, false, false, false));
        placement.setId("draw");
        placement.setSplay(Placement.SPLAY_DOWN);
    }

    private void unsplay() {
        placement.getDeck().remove(0);
        placement.setX(placement.getX() - SPLAY_WIDTH);
        placement.setY(placement.getY() - SPLAY_HEIGHT);
    }

    public Card draw() {
        if (deck.isEmpty()) return null;
        Card out = deck.remove(0);
        if (placement.getDeck().size() > 1) unsplay();
        if (deck.isEmpty()) {
            placement.getDeck().remove(0);
        }
        return out;
    }

    public boolean isTopExposed() {
        if(deck.isEmpty()) return false;
        return deck.get(0).isExposed();
    }

    public Card exposeTop() {
        if(deck.isEmpty()) return null;
        Card c = deck.get(0);
        c.setExposed(true);
        placement.getDeck().add(0, c);
        placement.setX(placement.getX() + SPLAY_WIDTH);
        placement.setY(placement.getY() + SPLAY_HEIGHT);
        return c;
    }

    public void dealOnto(Placement p, boolean expose) {
        dealOnto(p.getDeck(), expose);
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

    public boolean isEmpty() {
        return deck.isEmpty();
    }
}
