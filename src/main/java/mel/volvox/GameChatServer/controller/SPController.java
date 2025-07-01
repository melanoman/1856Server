package mel.volvox.GameChatServer.controller;

import jakarta.transaction.Transactional;
import mel.volvox.GameChatServer.model.*;
import mel.volvox.GameChatServer.model.sp.League;
import mel.volvox.GameChatServer.repository.*;
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
    @Autowired
    private TeamRepo teamRepo;
    @Autowired
    private DriverRepo driverRepo;

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

    private int calcalculateDriverNumber(String league, String team) {
        return 1+driverRepo.countByIdLeagueIDAndIdTeamID(league, team);
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

    @GetMapping("/sp/new/team/{league}/{team}")
    @ResponseBody
    SP_Team createTeam(@PathVariable String league,
                       @PathVariable String team,
                       @RequestParam(name="display") String display) {
        SP_TeamID id = new SP_TeamID(league, team);
        //TODO make sure not to overwrite
        SP_Team out = new SP_Team(id, display);
        teamRepo.save(out);
        return out;
    }

    @GetMapping("/sp/new/driver/{league}/{team}")
    @ResponseBody
    SP_Driver createDriver(@PathVariable String league,
                           @PathVariable String team,
                           @RequestParam(name="season") int season,
                           @RequestParam(name="display") String display) {
        int driverNumber = calcalculateDriverNumber(league, team);
        SP_DriverID id = new SP_DriverID(league, team, driverNumber);
        SP_Driver out = new SP_Driver(id, display, season);
        driverRepo.save(out);
        return out;
    }

    @GetMapping("/sp/delete/league/{league}")
    @ResponseBody
    @Transactional
    String deleteLeague(@PathVariable String league) {
        leagueRepo.deleteById(league);
        teamRepo.deleteAllByIdLeagueID(league);
        seasonRepo.deleteAllByIdLeagueID(league);
        raceRepo.deleteAllByIdLeagueID(league);
        driverRepo.deleteAllByIdLeagueID(league);
        return league;
    }

    @GetMapping("/sp/delete/team/{league}/{team}")
    @ResponseBody
    @Transactional
    String deleteTeam(@PathVariable String league,
                      @PathVariable String team) {
        teamRepo.deleteAllByIdLeagueIDAndIdTeamID(league, team);
        driverRepo.deleteAllByIdLeagueIDAndIdTeamID(league, team);
        return team;
    }

    @GetMapping("/sp/update/league/{league}")
    @ResponseBody
    @Transactional
    League updateLeague(@PathVariable String league,
                        @RequestParam(name="display") String displayName) {
        League out = new League(league, displayName);
        leagueRepo.save(out);
        return out;
    }

    //TODO prevent overwrite for new
    //TODO require overwrite for update

    @GetMapping("/sp/update/team/{league}/{team}")
    @ResponseBody
    @Transactional
    SP_Team updateTeam(@PathVariable String league,
                    @PathVariable String team,
                    @RequestParam(name="display") String displayName) {
        SP_TeamID id = new SP_TeamID(league, team);
        SP_Team out = new SP_Team(id, displayName);
        teamRepo.save(out);
        return out;
    }

    @GetMapping("/sp/update/season/{league}/{num}")
    @ResponseBody
    @Transactional
    SP_Season updateSeason(@PathVariable String league,
                           @PathVariable int num,
                           @RequestParam(name="display") String name) {
        SP_SeasonID id = new SP_SeasonID(league, num);
        SP_Season out = new SP_Season(id, name);
        seasonRepo.save(out);
        return out;
    }

    @GetMapping("/sp/update/race/{league}/{seasonNumber}/{raceNumber}")
    @ResponseBody
    @Transactional
    SP_Race updateRace(@PathVariable String league,
                       @PathVariable int seasonNumber,
                       @PathVariable int raceNumber,
                       @RequestParam(name="display") String displayName,
                       @RequestParam(name="multiplier") int multiplier,
                       @RequestParam(name="track") String trackName) {
        SP_RaceID raceID = new SP_RaceID(league, seasonNumber, raceNumber);
        SP_Race race = new SP_Race(raceID, displayName, trackName, multiplier);
        raceRepo.save(race);
        return race;
    }

    @GetMapping("/sp/update/driver/{league}/{team}/{num}")
    @ResponseBody
    @Transactional
    SP_Driver updateDriver(@PathVariable String league,
                           @PathVariable String team,
                           @PathVariable int num,
                           @RequestParam(name="display")String driverName,
                           @RequestParam(name="birth") int birthday) {
        SP_DriverID id = new SP_DriverID(league, team, num);
        SP_Driver out = new SP_Driver(id, driverName, birthday);
        driverRepo.save(out);
        return out;
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

    @GetMapping("/sp/teams")
    @ResponseBody
    List<SP_Team> listTeams() {
        return teamRepo.findAll();
    }

    @GetMapping("/sp/drivers")
    @ResponseBody
    List<SP_Driver> listDrivers() {
        return driverRepo.findAll();
    }
}
