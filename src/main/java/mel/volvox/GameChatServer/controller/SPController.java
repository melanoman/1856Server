package mel.volvox.GameChatServer.controller;

import mel.volvox.GameChatServer.model.SP_Race;
import mel.volvox.GameChatServer.model.SP_Season;
import mel.volvox.GameChatServer.model.SP_SeasonID;
import mel.volvox.GameChatServer.model.sp.League;
import mel.volvox.GameChatServer.repository.LeagueRepo;
import mel.volvox.GameChatServer.repository.SeasonRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@CrossOrigin
@Controller
@Component
public class SPController {
    @Autowired
    private LeagueRepo leagueRepo;
    @Autowired
    private SeasonRepo seasonRepo;

    @GetMapping("/sp/new/league/{league}")
    @ResponseBody
    League createLeague(@PathVariable String league,
                        @RequestParam(name="display") String displayName)  {
        Optional<League> old = leagueRepo.findById(league);
        if (old.isPresent()) {
            return old.get();
        }
        League newLeague = new League(league, displayName);
        leagueRepo.save(newLeague);
        return newLeague;
    }

    private int calculateId(String league) {
        return 1+seasonRepo.countByIdLeagueID(league);
    }

    @GetMapping("/sp/new/season/{league}")
    @ResponseBody
    SP_Season createSeason(@PathVariable String league,
                           @RequestParam(name="display") String displayName) {
        //TODO make sure league exists
        int seasonNumber = calculateId(league);
        SP_SeasonID spSeasonID = new SP_SeasonID(league, seasonNumber);
        SP_Season spSeason = new SP_Season(spSeasonID, displayName);
        seasonRepo.save(spSeason);
        return spSeason;
    }  // TODO make UI and test

    @GetMapping("/sp/leagues")
    @ResponseBody
    List<League> listLeagues() {
        return leagueRepo.findAll();
    }

    @GetMapping("/sp/seasons")
    @ResponseBody
    List<SP_Season> listSeasons() {
        return seasonRepo.findAll();
    }
}
