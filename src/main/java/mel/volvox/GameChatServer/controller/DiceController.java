package mel.volvox.GameChatServer.controller;

import jakarta.websocket.server.PathParam;
import mel.volvox.GameChatServer.service.DiceService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@CrossOrigin
@Controller
@Component
public class DiceController {
    @GetMapping("/roll/{die}")
    @ResponseBody
    String getRoll(@PathVariable String die) {
        die = die.toLowerCase().strip();
        return "The roll config is " + die + "\tThe roll is " + DiceService.RollDice(die);
    }

    static int GEAR2[] = { 2, 3, 3, 4, 4, 4 };
    static int GEAR3[] = { 4, 5, 6, 6, 7, 7, 8, 8 };

    @GetMapping("/gear/{gear}")
    @ResponseBody
    String getGear(@PathVariable int gear) {
        switch(gear) {
            case 1: return "Formula De Gear 1\t\tThe roll is "+DiceService.Roll(2);
            case 2: return "Formula De Gear 2\t\tThe roll is "+GEAR2[DiceService.Roll(6)-1];
            case 3: return "Formula De Gear 3\t\tThe roll is "+GEAR3[DiceService.Roll(8)-1];
            case 4: return "Formula De Gear 4\t\tThe roll is "+(6+DiceService.Roll(6));
            case 5: return "Formula De Gear 5\t\tThe roll is "+(10+DiceService.Roll(10));
            case 6: return "Formula De Gear 6\t\tThe roll is "+(20+DiceService.Roll(10));
            default: return "Unknown Formula De Gear number";
        }
    }
}
