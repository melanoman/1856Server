package mel.volvox.GameChatServer.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Game1856Test {

    @Test
    void makeNullShuffle() {
        assertEquals("123", Game1856.makeShuffle(false, 3));
        assertEquals("1234", Game1856.makeShuffle(false, 4));
        assertEquals("12345", Game1856.makeShuffle(false, 5));
        System.out.println(Game1856.makeShuffle(true, 3));
        System.out.println(Game1856.makeShuffle(true, 4));
        System.out.println(Game1856.makeShuffle(true, 5));
        System.out.println(Game1856.makeShuffle(true, 6));
    }
}