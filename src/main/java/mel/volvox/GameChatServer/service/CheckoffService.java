package mel.volvox.GameChatServer.service;

import mel.volvox.GameChatServer.model.stat.Checkoff;
import mel.volvox.GameChatServer.repository.CheckoffRepo;

import java.util.List;

public class CheckoffService {
    private final CheckoffRepo repo;
    public CheckoffService(CheckoffRepo repo) {
        this.repo = repo;
    }

    public void check(String user, String task) {
        Checkoff co = new Checkoff(System.currentTimeMillis(), user, task);
        repo.save(co);
    }

    public List<Checkoff> stats(String name) {
        return repo.findByName(name);
    }
}
