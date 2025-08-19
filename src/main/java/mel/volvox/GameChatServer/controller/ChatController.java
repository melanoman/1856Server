package mel.volvox.GameChatServer.controller;

import mel.volvox.GameChatServer.model.seating.Channel;
import mel.volvox.GameChatServer.model.seating.Message;
import mel.volvox.GameChatServer.repository.MessageRepo;
import mel.volvox.GameChatServer.repository.ChannelRepo;
import mel.volvox.GameChatServer.view.ChatView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin
@Controller
@Component
public class ChatController {
    private static final String CHAT_TYPE = "chat";

    @Autowired
    private ChannelRepo channelRepo;
    @Autowired
    private MessageRepo messageRepo;

    private static final Map<String, ChatView> name2view = new HashMap<>();

    private boolean chatExists(String name) {
        return channelRepo.existsByNameAndType(name, CHAT_TYPE);
    }

    synchronized private ChatView loadChat(String name) {
        ChatView tableView = name2view.get(name);
        if(tableView != null) return tableView;

        Optional<Channel> table = channelRepo.findByNameAndType(name, CHAT_TYPE);
        if(table.isEmpty()) throw new IllegalStateException("Channel Not Found");

        ChatView out = createView(name);
        name2view.put(name, out);
        return out;
    }

    private int calculateInitialChatNumber(String name) {
        Message m = messageRepo.findFirstByIdChannelOrderByIdSerialNumberDesc(name);
        return m == null ? 0 : m.getId().getSerialNumber();
    }

    synchronized private ChatView createView(String name) {
        ChatView out;
        try {
            out = new ChatView(name, calculateInitialChatNumber(name));
        } catch(Exception e) {
            System.out.println("UNEXPECTED ERROR creating "+name);
            e.printStackTrace();
            return null;
        }
        return out;
    }

    @PutMapping("/chat/create/{name}")
    @ResponseBody
    public String createChat(@PathVariable String name) {
        if(chatExists(name)) throw new IllegalStateException("Already exists");
        Channel t = new Channel(name, CHAT_TYPE);
        name2view.put(name, createView(name));
        channelRepo.save(t);
        return name;
    }

    @GetMapping("/chat/list")
    @ResponseBody
    public List<Channel> getChats() {
        return channelRepo.findByType(CHAT_TYPE);
    }


    @PutMapping("message/send/{table}/{author}")
    @ResponseBody
    public int newMessage(@PathVariable String table,
                          @PathVariable String author, //TODO check for forgery
                          @RequestBody String text) {
        ChatView tv = loadChat(table);
        if (tv == null) throw new IllegalStateException("Table not found");
        return tv.addMessage(messageRepo, text, author);
    }

    @GetMapping("message/get/{table}/{limit}")
    @ResponseBody
    public List<Message> recentMessages(@PathVariable String table,
                                        @PathVariable int limit) {
        ChatView tv = loadChat(table);
        if (tv == null) throw new IllegalStateException("Table not found");
        return messageRepo.findByIdChannelOrderByIdSerialNumberDesc(table, Limit.of(limit));
    }
}
