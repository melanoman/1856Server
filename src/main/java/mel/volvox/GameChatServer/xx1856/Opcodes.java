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
    public static final String BLOCK_SALE = "blockSale";
    public static final String CLEAR_BLOCK = "clearBlock";
    public static final String STOCK_TURN = "stockTurn";
    public static final String FORCED_SALE = "forcedSale";
    public static final String ADD_SHARES = "addShares";
    public static final String PURGE_SHARES = "purgeShares";
    public static final String END_STOCK_TURN = "stockTurnOver";
    public static final String END_STOCK_ROUND = "endStock";

    //MARKET MOVES
    public static final String RESORT_CORP = "resort";
    public static final String PRICE_UP = "priceUp";
    public static final String PRICE_DOWN = "priceDown";
    public static final String PRICE_LEFT = "priceLeft";
    public static final String PRICE_RIGHT = "priceRight";
    public static final String CLOSE_CORP = "closeCorp";

    //BANK ACTIONS
    public static final String TAKE_LOAN = "takeLoan";
    public static final String REPAY_LOAN = "repayLoan";
    public static final String PREZ_PAYS = "prezPays";
    public static final String START_FORCED_SALE = "startForcedSale";
    public static final String CGR_SHELL = "cgrShell";
    public static final String CGR_FILL = "cgrFill";
    public static final String REPO_CASH = "repoCash";
    public static final String ASK_CGR_TOKENS = "askCGRTokens";
    public static final String ANSWER_CGR_TOKENS = "setCGRTokens";
    public static final String ASK_CGR_TRAINS = "askCGRTrainDrop";
    public static final String DROP_TRAIN = "dropTrain";
    public static final String DONE_DROP = "doneDrop";

    //OP ACTIONS
    public static final String START_OP_ROUND = "startOpRound";
    public static final String END_OP_ROUND = "endOpRound";
    public static final String START_OP_TURN = "startOpTurn";
    public static final String END_OP_TURN = "endOpTurn";
    public static final String NO_ROUTE = "noRoute";

    public static final String DRILL_TILE = "drillTile";
    public static final String LAY_TOKEN = "layToken";
    public static final String WS_TOKEN = "wsToken";
    public static final String PLACE_PORT = "placePort";
    public static final String BUY_BRIDGE = "buyBridge";
    public static final String BUY_TUNNEL = "buyTunnel";

    public static final String WITHHOLD = "withhold";
    public static final String PAYDIV = "payDiv";
    public static final String PAY_INTEREST = "interest";
    public static final String DISBURSE = "disburse";
    public static final String CHANGE_RUN = "changeRun";

    public static final String DESTINATION_REACHED = "destinationReached";
    public static final String RELEASE_ESCROW = "releaseEscrow";

    //TRAIN ACTIONS
    public static final String BUY_BANK_TRAIN = "buyBankTrain";
    public static final String BUY_BANK_DIESEL = "buyBankDiesel";
    public static final String TRADE_IN_TRAIN = "tradeIn";
    public static final String BUY_CORP_TRAIN = "buyCorpTrain";
    public static final String BUY_POOL_TRAIN = "buyPoolTrain";
    public static final String FORCED_TRAIN = "forcedTrain";
    public static final String BUY_PRIV = "buyPriv";
    public static final String RUST = "rust";
    public static final String RUST_PRIV = "rustPriv";
    public static final String RUST_PORT = "rustPort";
    public static final String FORM_CGR = "formCGR";
    public static final String RETIRE_LOANER = "retireLoaner";
    public static final String START_TRAIN_DROP = "rustDrop";
    public static final String TRAIN_DROP = "trainDrop";

    //RESET ACTIONS
    public static final String RESET_TOKEN = "resetToken";
    public static final String RESET_LOAN = "resetLoan";
    public static final String FLOAT = "float";
    public static final String BANK_BREAK = "bankBreak";
    public static final String GAME_OVER = "gameOver";
    public static final String CALL_LOANS = "callLoans";
    public static final String SAVE_CORP =  "saveCorp";
    public static final String ABANDON_CORP = "abandonCorp";
    public static final String BANKRUPTCY_SALE = "swanDiveSale";
}
