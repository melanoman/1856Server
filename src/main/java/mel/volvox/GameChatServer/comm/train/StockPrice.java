package mel.volvox.GameChatServer.comm.train;

public class StockPrice {
    int price;
    int x; // steps from left (start column = 0)
    int y; // steps from top (top = 0)

    public static final int PAR_COLUMN = 4;
    public static final int[] depth = { 11, 11, 11, 11, 11, 8, 7, 6, 6, 5, 5, 4, 4, 3, 3, 2, 2, 2, 2, 2, 0};
    public static final int width = depth.length - 1;

    public static StockPrice makePar(int amount) {
        StockPrice out = new StockPrice();
        out.price = amount;
        out.x = PAR_COLUMN;
        out.y = (amount > 75) ? (100 - amount)/10 : 3 + (75 - amount)/5;
        return out;
    }

    private void incrementPrice() {
        if(price > 110) price+= 25;
        else if(price == 110) price += 15;
        else if(price > 75) price += 10;
        else price += 5;
    }

    private void decrementPrice() {
        if(price < 90) price -= 5;
        else if (price < 125) price -= 10;
        else if (price == 125) price -= 15;
        else price -= 25;
    }

    public void up() {
        if(y == 0) return;
        y--;
        incrementPrice();
    }

    public void down() {
        if(depth[x] == y+1) return;
        y++;
        decrementPrice();
    }

    public void right() {
        if(depth[x+1] <= y) up();
        else { x++; incrementPrice(); }
    }

    public void left() {
        if(x==0) down();
        else { x--; decrementPrice(); }
    }
}
