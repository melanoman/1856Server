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

    public boolean leftEdge() {
        return x==0;
    }

    public boolean rightEdge() {
        return depth[x+1] <= y;
    }

    public boolean floor() {
        return depth[x] == y+1;
    }

    public boolean ceiling() {
        return y==0;
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

    /**
     * @return number of drop spaces prevents by floor
     */
    public int drop(int distance) {
        if (y+distance > depth[x]-1) {
            int out = y+distance-depth[x]+1;
            y=depth[x]-1;
            return out;
        }
        y+=distance;
        return 0;
    }

    public void up() {
        if(ceiling()) return;
        y--;
        incrementPrice();
    }

    public void down() {
        if(floor()) return;
        y++;
        decrementPrice();
    }

    public void right() {
        if(rightEdge()) up();
        else { x++; incrementPrice(); }
    }

    public void left() {
        if(leftEdge()) down();
        else { x--; decrementPrice(); }
    }
}
