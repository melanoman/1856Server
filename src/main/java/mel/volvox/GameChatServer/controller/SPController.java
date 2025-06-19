package mel.volvox.GameChatServer.controller;

import mel.volvox.GameChatServer.model.SP_Race;
import mel.volvox.GameChatServer.model.SP_RaceID;
import mel.volvox.GameChatServer.model.SP_Season;
import mel.volvox.GameChatServer.model.SP_SeasonID;
import mel.volvox.GameChatServer.model.sp.League;
import mel.volvox.GameChatServer.repository.LeagueRepo;
import mel.volvox.GameChatServer.repository.RaceRepo;
import mel.volvox.GameChatServer.repository.SeasonRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
    @Autowired
    private RaceRepo raceRepo;

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

    private int calculateSeasonNumber(String league) {
        //TODO make sure league exists, return -1 if not
        return 1+seasonRepo.countByIdLeagueID(league);
    }

    private int calculateRaceNumber(String league, int seasonNumber) {
        //TODO make sure league and season exist, return -1 if not
        return 1+raceRepo.countByIdLeagueIDAndIdSeasonNumber(league, seasonNumber);
    }

    @GetMapping("/sp/new/season/{league}")
    @ResponseBody
    SP_Season createSeason(@PathVariable String league,
                           @RequestParam(name="display") String displayName) {
        int seasonNumber = calculateSeasonNumber(league);
        if (seasonNumber == -1) return null; //TODO better error
        SP_SeasonID spSeasonID = new SP_SeasonID(league, seasonNumber);
        SP_Season spSeason = new SP_Season(spSeasonID, displayName);
        seasonRepo.save(spSeason);
        return spSeason;
    }

    @GetMapping("/sp/new/race/{league}/{seasonNumber}")
    @ResponseBody
    SP_Race createRace(@PathVariable String league,
                       @PathVariable int seasonNumber,
                       @RequestParam(name="display") String displayName,
                       @RequestParam(name="multiplier") int multiplier,
                       @RequestParam(name="track") String trackName) {

        int raceNumber = calculateRaceNumber(league, seasonNumber);
        if (raceNumber == -1) return null; //TODO better error
        SP_RaceID raceID = new SP_RaceID(league, seasonNumber, raceNumber);
        SP_Race race = new SP_Race(raceID, displayName, trackName, multiplier);
        raceRepo.save(race);
        return race;
    }

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

    @GetMapping("/sp/races")
    @ResponseBody
    List<SP_Race> listRaces() { return raceRepo.findAll(); }
}
