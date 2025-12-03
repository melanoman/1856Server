package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Placement;
import mel.volvox.GameChatServer.comm.cards.Tableau;

import java.util.List;
import java.util.UUID;

public class Addition13 extends CardGame {
    public static final String MAIN = "main";
    Tableau show;
    List<Card> deck;
    Placement table;
    Card selection = null;
    int selectedIndex = -1;

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
        table.setId(MAIN);
        show.setId(id);
        show.getPlacements().add(table);
    }

    @Override
    public Tableau getLayout() {
        return show;
    }

    @Override
    public Tableau select(String id, int gridX, int gridY) {
        if(!MAIN.equals(id)) return show;
        int index = gridX + gridY*table.getGridWidth();

        Card c = table.getDeck().get(index);
        int value = cardValue(c);
        if(value == 13) {
            Cards.dealOver(deck, table.getDeck(), index);
            checkWin();
        } else if(selection == null) {
            selection = c;
            selectedIndex = index;
            c.setHighlight(true);
        } else if(cardValue(selection) + value == 13) {
            Cards.dealOver(deck, table.getDeck(), index);
            Cards.dealOver(deck, table.getDeck(), selectedIndex);
            selection = null;
            checkWin();
        } else {
            System.out.println(cardValue(selection) + value);
            selection.setHighlight(false);
            c.setHighlight(true);
            selection = c;
            selectedIndex = index;
        }
        return show;
    }

    private void checkWin() {
        if(deck.isEmpty() && table.isEmpty()) {
            show.setResult(Tableau.WIN);
        }
    }

    private int cardValue(Card c) {
        return c.cv1to13();
    }
}
