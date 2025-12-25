package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Card;

public abstract class SingleSelectionGame extends CardGame {
    protected Card selection = null;
    protected int selectedIndex = NO_SELECTION;


    protected void clearSelection() {
        if(selection != null) selection.setHighlight(false);
        selection = null;
        selectedIndex = NO_SELECTION;
    }

    protected void shiftSelection(int index, Card c) {
        clearSelection();
        selection = c;
        selectedIndex = index;
        c.setHighlight(true);
    }
}
