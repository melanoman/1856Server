package mel.volvox.GameChatServer.cards;

import lombok.Getter;
import lombok.Setter;
import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Placement;

import java.util.List;

/**
 * This class ties a deck of cards with a placement show one face down card that vanishes
 * when the last card is drawn.
 */
public class DrawDeck {
    private final List<Card> deck;
    @Getter
    private final Placement placement = new Placement();
    @Setter
    private boolean readealAllowed = false;

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
        if(placement.getDeck().size() < 2) return;
        placement.getDeck().remove(0);
        placement.setX(placement.getX() - SPLAY_WIDTH);
        placement.setY(placement.getY() - SPLAY_HEIGHT);
    }

    private void showIfEmpty() {
        if (deck.isEmpty()) {
            while (!placement.isEmpty()) placement.getDeck().remove(0);
            placement.getDeck().add(new Card(readealAllowed ? -1 : -2, true, false, false));
        }
    }

    public Card draw() {
        if (deck.isEmpty()) return null;
        Card out = deck.remove(0);
        unsplay();
        showIfEmpty();
        return out;
    }

    public void deal(List<Card> target, int count, boolean expose) {
        unsplay();
        Cards.deal(deck, target, count, expose);
        showIfEmpty();
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

    /**
     * look at top card WITHOUT removing it
     */
    public Card peek() {
        return deck.isEmpty() ? null : deck.get(0);
    }

    public void redealFrom(List<Card> from, boolean expose) {
        for(Card c: from) {
            deck.add(0, c);
            c.setExposed(expose);
        }
        from.clear();
        placement.getDeck().clear();
        placement.getDeck().add(new Card(0, false, false, false));
    }

    /**
     * Silde a card under the deck.  Does not flip the card.
     */
    public void add(Card c) {
        deck.add(c);
    }
}
