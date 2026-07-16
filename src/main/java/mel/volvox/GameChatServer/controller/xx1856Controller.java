package mel.volvox.GameChatServer.controller;

import mel.volvox.GameChatServer.comm.train.StockSale;
import mel.volvox.GameChatServer.model.seating.Channel;
import mel.volvox.GameChatServer.repository.ChannelRepo;
import mel.volvox.GameChatServer.repository.xx1856Repo;

import mel.volvox.GameChatServer.xx1856.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static mel.volvox.GameChatServer.xx1856.AuctionActions.calculateBuyPrice;
import static mel.volvox.GameChatServer.xx1856.Opcodes.*;

@CrossOrigin
@Controller
@Component
public class xx1856Controller {
    public static final String xx1856_TYPE = "xx1856";
    public Map<String, Game> name2game = new HashMap<>();

    @Autowired
    private xx1856Repo repo;
    @Autowired
    ChannelRepo channelRepo;

    @GetMapping("/18xx/list")
    @ResponseBody
    List<Channel> listGames() {
        return channelRepo.findByType(xx1856_TYPE);
    }

    @PutMapping("/18xx/create/{name}")
    @ResponseBody
    Board create1856(@PathVariable String name) {
        if(channelRepo.findByNameAndType(name, xx1856_TYPE).isEmpty()) {
            try {
                channelRepo.save(new Channel(name, xx1856_TYPE));
                Game game = new Game(name, repo);
                name2game.put(name, game);
                return game.getBoard();
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage());
            }
        } else {
            throw new IllegalStateException("Game already exists");
        }
    }

    synchronized private Game findGame(String name) {
        if(name2game.containsKey(name)) return name2game.get(name);
        if(channelRepo.findByNameAndType(name, xx1856_TYPE).isEmpty()) {
            throw new IllegalStateException("Game not Found");
        } else {
            Game game = new Game(name, repo);
            game.load(repo.findByIdGameNameOrderByIdSerialNumberAsc(name));
            name2game.put(name, game);
            return game;
        }
    }

    @GetMapping("18xx/board/{name}")
    @ResponseBody
    Board load1856(@PathVariable String name) {
        return findGame(name).getBoard();
    }

    @PutMapping("18xx/addPlayer/{game}/{player}")
    @ResponseBody
    Board addPlayer(@PathVariable String game, @PathVariable String player) {
        return findGame(game).addMove(ADD_PLAYER, player, "", 0, "");
    }

    @PutMapping("18xx/renamePlayer/{game}/{player}/{newName}")
    @ResponseBody
    Board renamePlayer(@PathVariable String game, @PathVariable String player, @PathVariable String newName) {
        return findGame(game).addMove(RENAME_PLAYER, player, "", 0, newName);
    }

    @PutMapping("18xx/undo/{game}")
    @ResponseBody
    Board undo(@PathVariable String game) {
        return findGame(game).undo();
    }

    @PutMapping("18xx/redo/{game}")
    @ResponseBody
    Board redo(@PathVariable String game) {
        return findGame(game).redo();
    }

    @PutMapping("18xx/redoAll/{game}")
    @ResponseBody
    Board redoAll(@PathVariable String game) {
        return findGame(game).redoAll();
    }

    @PutMapping("18xx/startGame/{game}/{shuffle}")
    @ResponseBody
    Board startGame(@PathVariable String game, @PathVariable boolean shuffle) {
        return findGame(game).addMove(START_GAME, "", "", shuffle ? 1 : 0, "");
    }

    @PutMapping("18xx/buyPriv/{game}/{priv}/{player}")
    @ResponseBody
    Board buyPriv(@PathVariable String game, @PathVariable String priv, @PathVariable String player) {
        Game g = findGame(game);
        return g.addMove(BUY, player, priv, calculateBuyPrice(g), "");
    }

    @PutMapping("18xx/bid/{game}/{priv}/{player}/{amount}")
    @ResponseBody
    Board bidPriv(@PathVariable String game, @PathVariable String priv,
                  @PathVariable String player, @PathVariable int amount) {
        return findGame(game).addMove(BID, player, priv, amount, "");
    }

    @PutMapping("18xx/winBidoff/{game}/{priv}/{player}/{amount}")
    @ResponseBody
    Board winBidoff(@PathVariable String game, @PathVariable String priv,
                    @PathVariable String player, @PathVariable int amount) {
        return findGame(game).addMove(WIN_BIDOFF, player, priv, amount, "");
    }

    @PutMapping("18xx/auctionPass/{game}/{player}")
    @ResponseBody
    Board auctionPass(@PathVariable String game, @PathVariable String player) {
        return findGame(game).addMove(AUCTION_PASS, player, "", 0, "");
    }

    @PutMapping("18xx/stockPass/{game}/{player}")
    @ResponseBody
    Board stockPass(@PathVariable String game, @PathVariable String player) {
        return findGame(game).addMove(STOCK_PASS, player, "", 0, "");
    }

    @PutMapping("18xx/stockTurn/{game}/{player}")
    @ResponseBody
    Board stockTurn(@PathVariable String game, @PathVariable String player,
                    @RequestBody StockTurn turn) {
        return StockActions.processStockTurn(turn, player, findGame(game));
    }

    @PutMapping("18xx/takeLoan/{game}/{corp}")
    @ResponseBody
    Board takeLoan(@PathVariable String game, @PathVariable String corp) {
        return findGame(game).addMove(TAKE_LOAN, "", corp, 100, "");
    }

    @PutMapping("18xx/layToken/{game}/{corp}")
    @ResponseBody
    Board layToken(@PathVariable String game, @PathVariable String corp) {
        return findGame(game).addMove(LAY_TOKEN, "", corp, 0, "");
    }

    @PutMapping("18xx/drillTile/{game}/{corp}")
    @ResponseBody
    Board drillTile(@PathVariable String game, @PathVariable String corp) {
        return findGame(game).addMove(DRILL_TILE, "", corp, 40, "");
    }
}
