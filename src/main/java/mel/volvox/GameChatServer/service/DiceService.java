package mel.volvox.GameChatServer.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class DiceService {
    public static Random kaos = new Random();

    public static int Roll(int sides) {
        return kaos.nextInt(sides) + 1;
    }

    public static int RollDice(String dice) {
        int nut = 0;
        int result = 0;
        do {
            int bolt = dice.indexOf('+', nut);
            if (bolt > 0) result += RollDie(dice.substring(nut, bolt));
            else result += RollDie(dice.substring(nut));
            nut = bolt+1;
        } while (nut > 0);
        return result;
    }

    public static int RollDie(String die) {
        int split = die.indexOf('d');
        if (split < 0) return Integer.parseInt(die); // constant, as in the 3 in 4d6+3
        if (split == 0) return Roll(Integer.parseInt(die.substring(split+1)));
        else {
            int result = 0;
            int max = Integer.parseInt(die.substring(0,split));
            int sides = Integer.parseInt(die.substring(split+1));
            for (int i=0; i<max; i++) {
                result += Roll(sides);
            }
            return result;
        }
    }
}
