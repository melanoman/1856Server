package mel.volvox.undo;

public interface UndoableGame<MOVE> {
    void storeMove(MOVE move);
    void deleteMove(MOVE move);
    String getActionType(MOVE move);
    boolean isMovePrimary(MOVE move);
}
