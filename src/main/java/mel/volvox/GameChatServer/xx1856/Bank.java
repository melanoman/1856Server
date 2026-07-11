package mel.volvox.GameChatServer.xx1856;

/**
 * This class manipulates board state for bank transactions
 */
public class Bank {
    Board board;

    public Bank(Board board) {
        this.board = board;
    }

    public void player2Corp(Player player, Corp corp, int amount) {
        player.cash -= amount;
        corp.cash += amount;
    }

    public void corp2Player(Corp corp, Player player, int amount) {
        corp.cash -= amount;
        player.cash += amount;
    }

    public void player2Escrow(Player player, Corp corp, int amount) {
        player.cash -= amount;
        corp.escrow += amount;
    }

    public void escrow2Player(Corp corp, Player player, int amount) {
        corp.escrow -= amount;
        player.cash += amount;
    }

    public void payPlayer(String player, int amount) {
        board.bank -= amount;
        findPlayer(player).cash += amount;
        //TODO implement end-of-game trigger
    }

    public void payCorp(String corp, int amount) {
        //TODO implement end-of-game trigger
        assert false;
    }

    public void debitPlayer(String player, int amount) {
        board.bank += amount;
        findPlayer(player).cash -= amount;
    }

    public void debitCorp(String corp, int amount) {
        assert false;
    }

    private Player findPlayer(String playerName) {
        for(Player p: board.getPlayers()) {
            if(p.name.equals(playerName)) return p;
        }
        throw new IllegalStateException("Cannot find playerName "+ playerName);
    }
}
