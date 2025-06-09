package mel.volvox.GameChatServer.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DiceServiceTest {
    @Test
    void rollConstant() {
        assertEquals(3, DiceService.RollDie("3"));
        assertEquals(4, DiceService.RollDice("4"));
        assertEquals(7, DiceService.RollDice("3+4"));
    }

    @Test
    void rollSixer() {
        boolean[] check = new boolean[6];
        for (int i = 0; i < 50; i++) {
            int roll = DiceService.RollDice("d6");
            assertTrue(roll > 0 && roll < 7);
            check[roll-1] = true;
        }
        for (int i=0; i<6; i++) {
            assertTrue(check[i]);
        }
        check = new boolean[6];
        for (int i = 0; i < 50; i++) {
            int roll = DiceService.RollDice("1d6");
            assertTrue(roll > 0 && roll < 7);
            check[roll - 1] = true;
        }
        for (int i=0; i<6; i++) {
            assertTrue(check[i]);
        }
    }

    @Test
    void roll2d6() {
        for (int i=0; i<50; i++) {
            int roll = DiceService.RollDice("2d6");
            assertTrue(roll > 1 && roll < 13);
        }
    }

    @Test
    void roll3d2Plus5() {
        for (int i=0; i<50; i++) {
            int roll = DiceService.RollDice("3d2+5");
            assertTrue(roll > 7 && roll < 12);
        }
    }
}