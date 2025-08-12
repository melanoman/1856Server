package mel.volvox.GameChatServer.controller;

import mel.volvox.GameChatServer.comm.RPSBoard;
import mel.volvox.GameChatServer.game.Chat;
import mel.volvox.GameChatServer.game.Game;
import mel.volvox.GameChatServer.game.Game1856;
import mel.volvox.GameChatServer.game.GameRPS;
import mel.volvox.GameChatServer.model.seating.GameTable;
import mel.volvox.GameChatServer.model.seating.Message;
import mel.volvox.GameChatServer.repository.MessageRepo;
import mel.volvox.GameChatServer.repository.MoveRepo;
import mel.volvox.GameChatServer.repository.SeatRepo;
import mel.volvox.GameChatServer.repository.TableRepo;
import mel.volvox.GameChatServer.seating.TableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin
@Controller
@Component
public class TableController {
    @Autowired
    private TableRepo tableRepo;
    @Autowired
    private SeatRepo seatRepo;
    @Autowired
    private MoveRepo moveRepo;
    @Autowired
    private MessageRepo messageRepo;

    private static final Map<String, TableView> name2view = new HashMap<>();
    private static final Map<String,Class<? extends Game>> type2class = new HashMap<>();
    static {
        type2class.put("rps", GameRPS.class);
        type2class.put("1856", Game1856.class);
        type2class.put("chat", Chat.class);
    }

    private boolean tableExists(String name) {
        return tableRepo.existsById(name);
    }

    private boolean isUnknownType(String type) {
        return null == type2class.get(type);
    }

    synchronized private TableView loadTable(String name) {
        TableView tableView = name2view.get(name);
        if(tableView != null) return tableView;

        Optional<GameTable> table = tableRepo.findById(name);
        if(table.isEmpty()) throw new IllegalStateException("GameTable Not Found");

        TableView out;
        try {
            out = new TableView(name, type2class.get(table.get().getType()));
            out.initSeats(seatRepo.findByIdTableName(name));
            out.initMoves(moveRepo.findAllByIdTableNameOrderByIdSerialNumber(name));
            out.setChatNumber(calculateInitialChatNumber(name));
        } catch(Exception e) {
            System.out.println("UNEXPECTED ERROR loading "+name);
            e.printStackTrace();
            return null;
        }
        name2view.put(name, out);
        return out;
    }

    int calculateInitialChatNumber(String name) {
        Message m = messageRepo.findFirstByOrderByIdSerialNumberDesc();
        return m == null ? 0 : m.getId().getSerialNumber();
    }

    synchronized private TableView createView(String name, String type) {
        TableView out;
        try {
            out = new TableView(name, type2class.get(type));
        } catch(Exception e) {
            System.out.println("UNEXPECTED ERROR creating "+name);
            e.printStackTrace();
            return null;
        }
        return out;
    }

    @PutMapping("/table/create/{name}/{type}")
    @ResponseBody
    public String createTable(@PathVariable String name,
                              @PathVariable String type) {
        if(tableExists(name)) throw new IllegalStateException("Already exists");
        if(isUnknownType(type)) throw new IllegalArgumentException("Unknown Type");
        GameTable t = new GameTable(name, type);
        name2view.put(name, createView(name, type));
        tableRepo.save(t);
        return name;
    }

    @GetMapping("/tables")
    @ResponseBody
    public List<GameTable> getTables() {
        return tableRepo.findAll();
    }

    @GetMapping("/tables/{type}")
    @ResponseBody
    public List<GameTable> getTableForType(@PathVariable String type) {
        return tableRepo.findByType(type);
    }

    @GetMapping("/table/options/{table}")
    @ResponseBody
    public List<String> getSeatingOptions(@PathVariable String table) {
        TableView tv = loadTable(table);
        return tv == null ? new ArrayList<>() : tv.getSeatOptions();
    }

    @PutMapping("/table/sit/{table}/{seat}")
    @ResponseBody
    public String requestSeat(@PathVariable String table,
                              @PathVariable String seat,
                              @CookieValue("user") String accountName) {
        TableView tv = loadTable(table);
        return tv == null ? "" : tv.requestSeat(seat, accountName);
    }

    @PutMapping("/table/resit/{table}/{seat}")
    @ResponseBody
    public String changeSeats(@PathVariable String table,
                              @PathVariable String seat,
                              @CookieValue("user") String accountName) {
        TableView tv = loadTable(table);
        return tv == null ? "" : tv.changeSeats(accountName, seat);
    }

    @PutMapping("/table/unsit/{table}")
    @ResponseBody
    public void abandonSeat(@PathVariable String table,
                              @CookieValue("user") String accountName) {
        TableView tv = loadTable(table);
        if (tv != null) tv.abandonSeat(accountName);
    }

    @PutMapping("message/send/{table}/{author}")
    @ResponseBody
    public int newMessage(@PathVariable String table,
                          @PathVariable String author, //TODO check for forgery
                          @RequestBody String text) {
        TableView tv = loadTable(table);
        if (tv == null) throw new IllegalStateException("Table not found");
        return tv.addMessage(messageRepo, text, author);
    }

    @GetMapping("message/get/{table}/{limit}")
    @ResponseBody
    public List<Message> recentMessages(@PathVariable String table,
                                        @PathVariable int limit) {
        TableView tv = loadTable(table);
        if (tv == null) throw new IllegalStateException("Table not found");
        return messageRepo.findByIdTableNameOrderByIdSerialNumberDesc(table, Limit.of(limit));
    }

    //TODO move this to a utility file
    @GetMapping("rps/status/{table}")
    @ResponseBody
    public RPSBoard getRPSstatus(@PathVariable String table) {
        TableView tv = loadTable(table);
        if (tv == null) throw new IllegalStateException("Table not found");
        return (RPSBoard)tv.getBoard();
    }
}
