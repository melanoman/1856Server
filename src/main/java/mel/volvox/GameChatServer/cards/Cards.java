package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.service.DiceService;

import java.util.ArrayList;
import java.util.List;

public class Cards {
    public static List<Card> shuffle(int size) {
        List<Integer> cards = new ArrayList<>(size);
        for(int i=0; i<size; i++) cards.add(i);
        for(int i=size-1; i>0; i--) {
            int slot = DiceService.Roll(i)-1;
            if(slot<i) {
                int swap = cards.get(i);
                cards.set(i, cards.get(slot));
                cards.set(slot, swap);
            }
        }
        return asDeck(cards);
    }

    public static void deal(List<Card> from, List<Card> to, int count, boolean expose) {
        for(int i=0; i< count; i++) {
            if (from.isEmpty()) return;
            Card c = from.remove(0);
            if (expose) c.setExposed(true);
            to.add(c);
        }
    }

    public static List<Card> asDeck(List<Integer> cards) {
        List<Card> deck = new ArrayList<>();
        for(Integer i: cards) deck.add(new Card(i, false, false, false));
        return deck;
    }

    public static void dealOver(List<Card> drawDeck, List<Card> target, int index, boolean expose) {
        if (drawDeck.isEmpty()) {
            target.set(index, null);
        } else {
            Card c = drawDeck.remove(0);
            if (expose) c.setExposed(true);
            target.set(index, c);
        }
    }

    public static void dealOver(List<Card> drawDeck, List<Card> target, int index) {
        dealOver(drawDeck, target, index, true);
    }

    public static void dealOver(List<Card> drawDeck, List<Card> target, Card c) {
        dealOver(drawDeck, target, target.indexOf(c), true);
    }

    public static void dealOver(List<Card> drawDeck, List<Card> target, Card c, boolean expose) {
        dealOver(drawDeck, target, target.indexOf(c), expose);
    }
}
