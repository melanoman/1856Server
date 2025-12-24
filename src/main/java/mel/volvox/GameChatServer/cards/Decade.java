package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;
import mel.volvox.GameChatServer.comm.cards.Placement;
import mel.volvox.GameChatServer.comm.cards.Tableau;

public class Decade extends CardGame {
    Placement main = new Placement();
    int selStart = NO_SELECTION;
    int selEnd = NO_SELECTION;
    int selTotal = 0;

    @Override
    public void init() {
        super.init();
        main.setId(MAIN);
        main.setDeck(Cards.shuffle(52));
        main.setY(120);
        main.setX(30);
        main.setGridWidth(13);
        main.setGridHeight(4);
        table.getPlacements().add(main);
        for(Card c:main.getDeck()) c.setExposed(true);
        checkResult();
    }

    private void clearSelection() {
        if (selStart != NO_SELECTION) {
            for (int i=selStart; i<=selEnd; i++) main.getDeck().get(i).setHighlight(false);
        }
        selStart = NO_SELECTION;
        selEnd = NO_SELECTION;
        selTotal = 0;
    }

    private void removeSelection() {
        for (int i=selStart; i<=selEnd; i++) main.getDeck().remove(selStart);
        selStart = NO_SELECTION;
        selEnd = NO_SELECTION;
        selTotal = 0;
        checkResult();
    }

    private void checkTriplet() {
        if (selEnd - selStart == 2) {
            if(selTotal%10 == 0) removeSelection();
            else clearSelection();
        }
    }

    private static int value(Card c) {
        int rank = c.rank();
        return rank > 9 ? 10 : rank;
    }

    @Override
    public Tableau select(String id, int gridX, int gridY) {
        int index = gridX + 13*gridY;
        if(index >= main.getDeck().size()) clearSelection();
        else if(selStart == NO_SELECTION) {
            selStart = index;
            selEnd = index;
            Card c = main.getDeck().get(index);
            selTotal = value(c);
            c.setHighlight(true);
        } else if (index == selStart - 1) {
            selStart = index;
            Card c = main.getDeck().get(index);
            selTotal += value(c);
            c.setHighlight(true);
            checkTriplet();
        } else if (index == selEnd + 1) {
            selEnd = index;
            Card c = main.getDeck().get(index);
            selTotal += value(c);
            c.setHighlight(true);
            checkTriplet();
        } else {
            clearSelection();
        }
        return table;
    }

    private int valAt(int index) {
        return value(main.getDeck().get(index));
    }

    private void checkResult() {
        if(main.getDeck().size() == 1) {
            table.setResult(Tableau.WIN);
        } else {
            int tot = valAt(0) + valAt(1) + valAt(2);
            if(tot%10 == 0) return;
            int head = 3;
            int tail = 0;
            while (head < main.getDeck().size()) {
                tot -= valAt(tail);
                tail++;
                tot += valAt(head);
                head++;
                if(tot%10 == 0) return;
            }
            table.setResult(Tableau.LOSE);
        }
    }
}
