package mel.volvox.GameChatServer.comm.train;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StockPriceTest {
    @Test
    void parRow() {
        assertEquals(0, StockPrice.makePar(100).y);
        assertEquals(1, StockPrice.makePar(90).y);
        assertEquals(2, StockPrice.makePar(80).y);
        assertEquals(3, StockPrice.makePar(75).y);
        assertEquals(4, StockPrice.makePar(70).y);
        assertEquals(5, StockPrice.makePar(65).y);
    }

    @Test
    void parPrice() {
        assertEquals(100, StockPrice.makePar(100).price);
        assertEquals(90, StockPrice.makePar(90).price);
        assertEquals(80, StockPrice.makePar(80).price);
        assertEquals(75, StockPrice.makePar(75).price);
        assertEquals(70, StockPrice.makePar(70).price);
        assertEquals(65, StockPrice.makePar(65).price);
    }

    @Test
    void parColumn() {
        assertEquals(4, StockPrice.makePar(100).x);
        assertEquals(4, StockPrice.makePar(90).x);
        assertEquals(4, StockPrice.makePar(80).x);
        assertEquals(4, StockPrice.makePar(75).x);
        assertEquals(4, StockPrice.makePar(70).x);
        assertEquals(4, StockPrice.makePar(65).x);
    }

    @Test
    void moveTest() {
        StockPrice sp = StockPrice.makePar(100);
        assertEquals(0, sp.y);
        assertEquals(4, sp.x);
        assertEquals(100, sp.price);

        sp.up();
        assertEquals(0, sp.y);
        assertEquals(4, sp.x);
        assertEquals(100, sp.price);

        sp.down();
        assertEquals(1, sp.y);
        assertEquals(4, sp.x);
        assertEquals(90, sp.price);

        sp.down();
        assertEquals(2, sp.y);
        assertEquals(4, sp.x);
        assertEquals(80, sp.price);

        sp.down();
        assertEquals(3, sp.y);
        assertEquals(4, sp.x);
        assertEquals(75, sp.price);

        sp.down();
        assertEquals(4, sp.y);
        assertEquals(4, sp.x);
        assertEquals(70, sp.price);

        sp.down();
        assertEquals(5, sp.y);
        assertEquals(4, sp.x);
        assertEquals(65, sp.price);

        sp.down();
        assertEquals(6, sp.y);
        assertEquals(4, sp.x);
        assertEquals(60, sp.price);

        sp.right();
        assertEquals(6, sp.y);
        assertEquals(5, sp.x);
        assertEquals(65, sp.price);

        sp.right();
        assertEquals(6, sp.y);
        assertEquals(6, sp.x);
        assertEquals(70, sp.price);

        sp.down();
        assertEquals(6, sp.y);
        assertEquals(6, sp.x);
        assertEquals(70, sp.price);

        sp.right();
        assertEquals(5, sp.y);
        assertEquals(6, sp.x);
        assertEquals(75, sp.price);
    }

    @Test
    void moveTest2() {
        StockPrice sp = StockPrice.makePar(100);
        assertEquals(0, sp.y);
        assertEquals(4, sp.x);
        assertEquals(100, sp.price);

        sp.right();
        assertEquals(110, sp.price);

        sp.right();
        assertEquals(125, sp.price);

        sp.right();
        assertEquals(150, sp.price);

        sp.right();
        sp.right();
        sp.right();
        sp.right();
        sp.right();
        sp.right();
        sp.right();
        sp.right();
        sp.right();
        sp.right();
        sp.right();
        assertEquals(425, sp.price);

        sp.down();
        assertEquals(400, sp.price);

        sp.down();
        assertEquals(400, sp.price);

        sp.right();
        assertEquals(425, sp.price);

        sp.right();
        assertEquals(450, sp.price);

        sp.right();
        assertEquals(450, sp.price);

        sp.up();
        assertEquals(450, sp.price);
        assertEquals(19, sp.x);
        assertEquals(0, sp.y);
    }

    @Test
    void leftEdge() {
        StockPrice sp = StockPrice.makePar(90);
        sp.left();
        sp.left();
        sp.left();
        sp.left();
        sp.left();

        assertEquals(2, sp.y);
        assertEquals(60, sp.price);
        assertEquals(0, sp.x);
    }

    @Test
    void drop() {
        StockPrice sp = StockPrice.makePar(65);
        assertEquals(0, sp.drop(4));
        assertEquals(3, sp.drop(4));

        sp = StockPrice.makePar(70);
        sp.right();
        sp.right();
        assertEquals(3, sp.drop(5));
    }
}