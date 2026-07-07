package mel.volvox.GameChatServer.xx1856;

public class Opcodes {
    //GATHER ACTIONS
    public static final String ADD_PLAYER = "addPlayer";
    public static final String RENAME_PLAYER = "renamePlayer";
    public static final String START_GAME = "startGame";
    public static final String SHUFFLE = "shuffle";

    //GENERIC ACTIONS
    public static final String CHANGE_PLAYER = "changePlayer";
    public static final String CHANGE_PRIORITY = "changePriority";
    public static final String CHANGE_CORP = "changeCorp";

    //AUCTION ACTIONS
    public static final String BID = "auctionBid";
    public static final String BUY = "auctionBuy";
    public static final String AUCTION_PASS = "auctionPass";
    public static final String AUCTION_PAYOUT = "auctionPayout";
    public static final String AWARD_BID = "awardBid";
    public static final String CANCEL_BID = "cancelBid";
    public static final String START_BIDOFF = "startBidoff";
    public static final String WIN_BIDOFF = "winBidoff";
    public static final String END_AUCTION = "endAuction";

    //STOCK ACTIONS
    public static final String STOCK_PASS = "stockPass";
    public static final String SET_PAR = "setPar";
}
