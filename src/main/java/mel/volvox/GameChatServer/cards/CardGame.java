package mel.volvox.GameChatServer.cards;

import mel.volvox.GameChatServer.comm.cards.Tableau;

public abstract class CardGame {
    public abstract void init();
    public abstract Tableau getLayout();
    public abstract Tableau select(String id, int gridX, int gridY);
}
