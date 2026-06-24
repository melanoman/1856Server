package mel.volvox.undo;

public interface UndoableAction<MOVE, GAME> {
    //throw exception if not allowed to prevent action from initializing
    void checkAllowed(MOVE move, GAME game);
    void init(MOVE move, GAME game);
    void exec(MOVE move, GAME game);
    void undo(MOVE move, GAME game);
}
