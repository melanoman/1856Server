package mel.volvox.GameChatServer.math;

public class RootCube {
    public static void main(String[] argv) {
        System.out.println("SQRT(SUM(x^3, 1..n))");
        long totCube = 0;
        long totLinear = 0;
        for(int i=1; i<100; i++) {
            long cube = i*i*i;
            totCube = cube + totCube;
            totLinear = i + totLinear;
            System.out.println("i = "+i+"\ttotCube = "+totCube+"\tSQRT(totCube) = "+Math.sqrt(totCube)+"\ttotLinear = "+totLinear);
        }
    }
}
