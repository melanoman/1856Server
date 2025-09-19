package mel.volvox.GameChatServer.controller;

import mel.volvox.GameChatServer.comm.train.Board1856;
import mel.volvox.GameChatServer.comm.train.Stock;
import mel.volvox.GameChatServer.comm.train.StockSale;
import mel.volvox.GameChatServer.game.Game1856;
import mel.volvox.GameChatServer.model.seating.Channel;
import mel.volvox.GameChatServer.repository.ChannelRepo;
import mel.volvox.GameChatServer.repository.TrainRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@Controller
@Component
public class TrainController {
    @Autowired
    ChannelRepo channelRepo;
    @Autowired
    TrainRepo trainRepo;

    static public final String TRAIN_TYPE="1856";
    Map<String, Game1856> name2game = new HashMap<>();

    private Game1856 makeGame(String name) {
        Game1856 out = new Game1856();
        out.setRepo(trainRepo);
        out.getBoard().setName(name);
        return out;
    }

    synchronized Game1856 loadGame(String table) {
        if(name2game.containsKey(table)) return name2game.get(table);
        Game1856 out = makeGame(table);
        out.loadMoves(trainRepo.findByIdChannelOrderByIdSerialNumberAsc(table));
        name2game.put(table, out);
        return out;
    }

    @GetMapping("/1856/list")
    @ResponseBody
    List<Channel> listTables() {
        return channelRepo.findByType(TRAIN_TYPE);
    }

    @PutMapping("/1856/create/{table}")
    @ResponseBody
    synchronized String create1856(@PathVariable String table) {
        if (!channelRepo.existsByNameAndType(table, TRAIN_TYPE)) {
            name2game.put(table, makeGame(table));
            channelRepo.save(new Channel(table, TRAIN_TYPE));
        }
        return table;
    }

    @PutMapping("/1856/player/new/{table}/{name}")
    @ResponseBody
    synchronized boolean addPlayer(@PathVariable String table,
                                   @PathVariable String name) {
        return loadGame(table).addPlayer(name);
    }

    @PutMapping("/1856/player/rename/{table}/{oldName}/{newName}")
    @ResponseBody
    synchronized Board1856 renamePlayer(@PathVariable String table,
                                        @PathVariable String oldName,
                                        @PathVariable String newName) {
        return loadGame(table).renamePlayer(oldName, newName);
    }

    @GetMapping("/1856/status/{table}")
    @ResponseBody
    synchronized Board1856 getStatus(@PathVariable String table) {
        return loadGame(table).getBoard();
    }

    @PutMapping("/1856/undo/{table}")
    @ResponseBody
    synchronized Board1856 undo(@PathVariable String table) {
        return loadGame(table).undo();
    }

    @PutMapping("/1856/redo/{table}")
    @ResponseBody
    synchronized Board1856 redo(@PathVariable String table) {
        return loadGame(table).redo();
    }

    @PutMapping("/1856/redoAll/{table}")
    @ResponseBody
    synchronized Board1856 redoAll(@PathVariable String table) {
        return loadGame(table).redoAll();
    }

    @PutMapping("1856/start/{table}")
    @ResponseBody
    synchronized Board1856 startGame(@PathVariable String table,
                                     @RequestParam("shuffle") boolean shuffle) {
        return loadGame(table).startGame(shuffle);
    }

    @PutMapping("1856/auction/buy/{table}")
    @ResponseBody
    synchronized Board1856 auctionBuy(@PathVariable String table) {
        return loadGame(table).auctionBuy();
    }

    @PutMapping("1856/pass/{table}")
    @ResponseBody
    synchronized Board1856 pass(@PathVariable String table) {
        return loadGame(table).pass();
    }

    @PutMapping("1856/auction/bid/{table}/{corp}/{amount}")
    @ResponseBody
    synchronized Board1856 auctionBid(@PathVariable String table,
                                      @PathVariable String corp,
                                      @PathVariable int amount) {
        return loadGame(table).bid(corp, amount);
    }

    @PutMapping("1856/auction/bidoff/{table}/{bidder}/{amount}")
    @ResponseBody
    synchronized Board1856 auctionBidoff(@PathVariable String table,
                                         @PathVariable String bidder,
                                         @PathVariable int amount) {
        return loadGame(table).bidoff(bidder, amount);
    }

    @PutMapping("1856/par/{table}/{corp}/{amount}")
    @ResponseBody
    synchronized Board1856 setPar(@PathVariable String table,
                                  @PathVariable String corp,
                                  @PathVariable int amount) {
        return loadGame(table).setPar(corp, amount);
    }

    @PutMapping("/1856/buy/{table}/bank/{corp}")
    @ResponseBody
    synchronized Board1856 buyBankShare(@PathVariable String table,
                                        @PathVariable String corp) {
        return loadGame(table).buyBank(corp);
    }

    @PutMapping("1856/buy/{table}/pool/{corp}")
    @ResponseBody
    synchronized Board1856 buyPoolShare(@PathVariable String table,
                                        @PathVariable String corp) {
        return loadGame(table).buyPool(corp);
    }

    @PutMapping("1856/sell/{table}")
    @ResponseBody
    synchronized Board1856 sellShares(@PathVariable String table,
                                      @RequestBody List<StockSale> stocks) {
        return loadGame(table).makeSales(stocks);
    }

    @PutMapping("1856/loan/{table}")
    @ResponseBody
    synchronized Board1856 takeLoan(@PathVariable String table) {
        return loadGame(table).takeLoan();
    }

    @PutMapping("1856/buypriv/{table}/{privName}/{price}")
    @ResponseBody
    synchronized Board1856 buyPriv(@PathVariable String table,
                                   @PathVariable String privName,
                                   @PathVariable int price) {
        return loadGame(table).buyPriv(privName, price);
    }

    @PutMapping("1856/token/{table}")
    @ResponseBody
    synchronized Board1856 payToken(@PathVariable String table) {
        return loadGame(table).payToken();
    }

    @PutMapping("1856/tile/{table}")
    @ResponseBody
    synchronized Board1856 payTile(@PathVariable String table) {
        return loadGame(table).payTile();
    }

    @PutMapping("1856/withhold/{table}/{amount}")
    @ResponseBody
    synchronized Board1856 withhold(@PathVariable String table,
                                    @PathVariable int amount) {
        return loadGame(table).withhold(amount);
    }

    @PutMapping("1856/payout/{table}/{amount}")
    @ResponseBody
    synchronized Board1856 payout(@PathVariable String table,
                                  @PathVariable int amount) {
        return loadGame(table).payout(amount);
    }

    @PutMapping("1856/destination/{table}")
    @ResponseBody
    synchronized Board1856 destination(@PathVariable String table) {
        return loadGame(table).destination();
    }

    // for face value purchases from the bank (not pool, not tradein)
    @PutMapping("1856/banktrain/{table}")
    @ResponseBody
    synchronized Board1856 bankTrain(@PathVariable String table) {
        return loadGame(table).buyBankTrain();
    }
}
