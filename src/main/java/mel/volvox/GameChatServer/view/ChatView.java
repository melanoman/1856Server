package mel.volvox.GameChatServer.view;

import mel.volvox.GameChatServer.model.seating.*;
import mel.volvox.GameChatServer.repository.MessageRepo;

public class ChatView {
    private String name;
    int currentMessageId;
    public ChatView(String name, int serialNumber) {
        this.name = name;
        this.currentMessageId = serialNumber;
    }

    private MessageID nextMessageID() {
        currentMessageId++;
        return new MessageID(name, currentMessageId);
    }

    public synchronized int addMessage(MessageRepo messageRepo, String text, String author) {
        MessageID id = nextMessageID();
        messageRepo.save(new Message(id, text, author));
        return id.getSerialNumber();
    }
}
