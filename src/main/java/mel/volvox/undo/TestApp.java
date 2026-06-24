package mel.volvox.undo;

import java.util.List;

public class TestApp {

    final Action action = new Action();

    static class Action implements UndoableAction<String,Game> {

        @Override
        public void checkAllowed(String s, Game game) {
            if("boom".equals(s)) throw new IllegalStateException("BOOM");
            System.out.println("allowed");
        }

        @Override
        public void init(String s, Game game) {
            System.out.println("init "+s);
        }

        @Override
        public void exec(String s, Game game) {
            System.out.println("exec "+s);
        }

        @Override
        public void undo(String s, Game game) {
            System.out.println("undo "+s);
        }
    }

    static class Game implements UndoableGame<String> {
        final UndoManager<String, Game, Action> mgr;

        Game() {
            mgr = new UndoManager<>(this);
        }

        Game(List<String> starter) {
            mgr = new UndoManager<>(this, starter);
        }

        @Override
        public void storeMove(String s) {
            System.out.println("store " + s);

        }

        @Override
        public void deleteMove(String s) {
            System.out.println("delete " + s);

        }

        @Override
        public String getActionType(String s) {
            if (s == null) return "boom";
            if (s.equals("babe")) return "babe";
            return "dude";
        }

        @Override
        public boolean isMovePrimary(String s) {
            return true;
        }
    }

    static void top(String s, Game g) {
        System.out.println("===Add top "+s);
        g.mgr.newTopMove(s);
    }

    static void sub(String s, Game g) {
        System.out.println("===Add sub "+s);
        g.mgr.newSubMove(s);
    }

    static void undo(Game g) {
        System.out.println("===Undo");
        g.mgr.undo();
    }

    static void redo(Game g) {
        System.out.println("===Redo");
        g.mgr.redo();
    }

    public static void main(String[] arg) {
        Game g = new Game();

        try {
            g.mgr.registerActionType("dude", new TestApp.Action());
            top("a", g);
            top("b", g);
            top("c", g);
            undo(g);
            undo(g);
            redo(g);
            undo(g);
            top("d", g);
            undo(g);
            undo(g);
            undo(g);
            undo(g);
            undo(g);
            top("x", g);
            top("y", g);
            top("z", g);
            undo(g);
            top("boom", g);
        } catch(Exception e) {
            System.out.println("Exception "+e.toString());
        }
        try {
            undo(g);
            redo(g);
            redo(g);
            redo(g);
        }catch(Exception e) {
            System.out.println("Exception "+e.toString());
        }
        try {
            top("abbie",g);
        }catch(Exception e) {
            System.out.println("Exception "+e.toString());
        }
        try {
            top("babe",g);
        }catch(Exception e) {
            System.out.println("Exception "+e.toString());
        }
        try {
            top("chuck",g);
        }catch(Exception e) {
            System.out.println("Exception "+e.toString());
        }
        try {
            undo(g);
            undo(g);
            undo(g);
            undo(g);
        }catch(Exception e) {
            System.out.println("Exception "+e.toString());
        }
    }
}
