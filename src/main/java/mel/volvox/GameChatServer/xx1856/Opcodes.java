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
    public static final String CHANGE_PREZ = "changePrez";
    public static final String CHANGE_ACTIVITY = "changeActivity";

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
    public static final String START_STOCK_ROUND = "startStock";
    public static final String STOCK_PASS = "stockPass";
    public static final String SET_PAR = "setPar";
    public static final String BANK_BUY = "bankBuy";
    public static final String POOL_BUY = "poolBuy";
    public static final String STOCK_SALE = "stockSale";
    public static final String STOCK_TURN = "stockTurn";
    public static final String END_STOCK_TURN = "stockTurnOver";
    public static final String END_STOCK_ROUND = "endStock";

    //CORP ACTIONS
    public static final String RESORT_CORP = "resort";
    public static final String START_OP_ROUND = "startOpRound";
    public static final String END_OP_ROUND = "endOpRound";
    public static final String START_OP_TURN = "startOpTurn";
    public static final String TAKE_LOAN = "takeLoan";
    public static final String LAY_TOKEN = "layToken";
    public static final String DRILL_TILE = "drillTile";
    public static final String WITHHOLD = "withhold";
    public static final String PAYDIV = "payDiv";
    public static final String PAY_INTEREST = "interest";
    public static final String DISBURSE = "disburse";
    public static final String CHANGE_RUN = "changeRun";
    public static final String DESTINATION_REACHED = "destinationReached";
    public static final String RELEASE_ESCROW = "releaseEscrow";

    public static final String RESET_TOKEN = "resetToken";
    public static final String RESET_LOAN = "resetLoan";
    public static final String FLOAT = "float";
}
