package mel.volvox.GameChatServer.controller;

import jakarta.transaction.Transactional;
import mel.volvox.GameChatServer.model.sp.*;
import mel.volvox.GameChatServer.repository.*;
import mel.volvox.GameChatServer.util.RaceCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
    @Autowired
    private ResultRepo resultRepo;

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

    private int calculateNewSeasonNumber(@NonNull String league) {
        //TODO make sure league exists, return -1 if not
        return 1+seasonRepo.countByIdLeagueID(league);
    }

    private int calculateNewRaceNumber(@NonNull String league, int seasonNumber) {
        //TODO make sure league and season exist, return -1 if not
        return 1+raceRepo.countByIdLeagueIDAndIdSeasonNumber(league, seasonNumber);
    }

    private int calculateNewDriverNumber(@NonNull String league, @NonNull String team) {
        return 1+driverRepo.countByIdLeagueIDAndIdTeamID(league, team);
    }

    private Optional<SP_Race> scanRace(
            int seasonNumber, int raceNumber, @NonNull List<SP_Race> races
    ) {
        for (SP_Race race: races) {
            if(race.getId().getSeasonNumber() == seasonNumber &&
                    race.getId().getRaceNumber() == raceNumber
            ) {
                return Optional.of(race);
            }
        }
        return Optional.empty();
    }

    private Optional<SP_Race> calculatePriorRace(
            @NonNull List<SP_Race> races,
            @NonNull List<SP_Result> results
    ) {
        if (results.isEmpty()) return Optional.empty();

        int raceNumber = 1;
        int seasonNumber = 1;
        for(SP_Result result: results) {
            if (result.getId().getSeasonNumber() > seasonNumber  ||
                (result.getId().getSeasonNumber() == seasonNumber &&
                 result.getId().getRaceNumber() > raceNumber)
            ) {
                raceNumber = result.getId().getRaceNumber();
                seasonNumber = result.getId().getSeasonNumber();
            }
        }
        return scanRace(seasonNumber, raceNumber, races);
    }

    private Optional<SP_Race>nextRace(
            @NonNull SP_Race race,
            @NonNull Map<Integer, Integer> season2length,
            @NonNull List<SP_Race> races) {
        int raceNumber = race.getId().getRaceNumber();
        int seasonNumber = race.getId().getSeasonNumber();
        if (raceNumber < season2length.get(seasonNumber)) {
            raceNumber++;
        } else {
            raceNumber = 1;
            seasonNumber++;
        }
        return scanRace(seasonNumber, raceNumber, races);
    }

    private Optional<SP_Race> calculateCurrentRace(
            List<SP_Race> races,
            List<SP_Result> results,
            Map<Integer, Integer> season2length
    ) {
        if (races.isEmpty()) return Optional.empty();
        Optional<SP_Race> prior = calculatePriorRace(races, results);
        if(prior.isEmpty()) return scanRace(1, 1, races);
        return nextRace(prior.get(), season2length, races);
    }

    private static final int triangle(int n) {
        return n*(n+1)/2;
    }

    private int ageLoss(SP_Race race, int birthday) {
        int age = race.getId().getSeasonNumber() - birthday;
        int loss = triangle(2*age);
        return triangle((race.getId().getRaceNumber() > 5) ? age*2+1 : age*2);
    }

    private SP_DriverStatus calculateDriverStatus(
            @NonNull SP_Driver driver,
            @NonNull SP_Race race,
            @NonNull Map<String, Integer> race2bonus,
            @NonNull Map<Integer,Integer> season2length,
            @NonNull List<SP_Result> results,
            int serialNumber
    ) {
        SP_DriverStatus out = new SP_DriverStatus();
        out.setDriver(driver);
        out.setSerialNumber(serialNumber);
        int experience = 0;
        int hospital = 0;
        RaceCounter counter = new RaceCounter(season2length);
        int totalPoints = 0;
        int seasonPoints = 0;
        for(SP_Result result: results) {
            totalPoints += result.getPoints();
            if (result.getId().getSeasonNumber() == race.getId().getSeasonNumber()) {
                seasonPoints += result.getPoints();
            }
            int lag = counter.skipTo(result.getId().getSeasonNumber(), result.getId().getRaceNumber());
            if (hospital > lag) {
                // THIS ONLY HAPPENS IF AN ELIGIBLE DRIVER WAS ENTERED INTO A RACE
                //TODO prevent results like this from being saved
                experience -= lag;
                hospital -= lag;
            } else if (hospital > 0) {
                experience -= hospital;
                hospital = 0;
            }
            experience += result.isFinished() ? 2: 1;
            hospital += result.getInjuryDuration();
        }
        if (hospital > 0) {
            int lag = counter.skipTo(race.getId().getSeasonNumber(), race.getId().getRaceNumber())-1;
            experience -= lag;
            hospital -= lag;
        }
        experience -= ageLoss(race, driver.getBirthday());
        out.setExperience(experience);
        out.setRemainingInjury(Math.max(hospital, 0));
        out.setSeasonPoints(seasonPoints);
        out.setAllTimePoints(totalPoints);
        //TODO calculate standings
        return out;
    }

    static class DriverComparator implements Comparator<SP_Driver> {
        @Override
        public int compare(SP_Driver o1, SP_Driver o2) {
            int compare = o1.getId().getTeamID().compareTo(o2.getId().getTeamID());
            if (compare != 0) return compare;
            return o1.getId().getDriverNumber() - o2.getId().getDriverNumber();
        }
    }

    static DriverComparator compareDrivers = new DriverComparator();

    static class ResultByScheduleComparator implements Comparator<SP_Result> {
        @Override
        public int compare(SP_Result o1, SP_Result o2) {
            int compare = o1.getId().getSeasonNumber() - o2.getId().getSeasonNumber();
            if (compare != 0) return compare;
            return o1.getId().getRaceNumber() - o2.getId().getRaceNumber();
        }
    }

    static Comparator<SP_Result> compareResultsBySchedule = new ResultByScheduleComparator();

    static boolean filterMatchesDriver(@NonNull SP_Result result, @NonNull SP_Driver driver) {
        return (
            result.getTeamID().equals(driver.getId().getTeamID()) &&
            result.getDriverNumber() == driver.getId().getDriverNumber()
        );
    }

    @GetMapping("/sp/preview/{league}")
    @ResponseBody
    SP_Preview getPreview(@PathVariable String league) {
        SP_Preview preview = new SP_Preview();
        List<SP_Race> races = raceRepo.findAllByIdLeagueID(league);
        if (races.isEmpty()) return SP_Preview.NULL;

        SP_Preview out = new SP_Preview();
        Map<Integer, Integer> season2length = new HashMap<>();
        for(SP_Race race: races) {
            Integer oldMax = season2length.get(race.getId().getSeasonNumber());
            if(oldMax == null || race.getId().getRaceNumber() > oldMax) {
                season2length.put(race.getId().getSeasonNumber(), race.getId().getRaceNumber());
            }
        }
        List<SP_Result> results = resultRepo.findAllByIdLeagueID(league);

        //find the next race, abort if end of schedule
        Optional<SP_Race> currentRace = calculateCurrentRace(races, results, season2length);
        out.setRace(currentRace.orElse(SP_Race.NULL));
        if (currentRace.isEmpty()) return out;

        // calculate the driver statuses for that race
        List<SP_Driver> drivers = driverRepo.findAllByIdLeagueID(league);
        List<SP_DriverStatus> statuses = new ArrayList<>(drivers.size());

        Map<SP_Driver, List<SP_Result>> driver2results = new HashMap<>();
        Map<String, SP_Driver> id2Driver = new HashMap<>();
        for(SP_Driver driver: drivers) {
            driver2results.put(driver, new ArrayList<>());
            id2Driver.put(driver.getId().getDriverNumber()+":"+driver.getId().getTeamID(),driver);
        }
        for(SP_Result result: results) {
            SP_Driver driver = id2Driver.get(result.getDriverNumber() + ":" + result.getTeamID());
            driver2results.get(driver).add(result);
        }
        Map<String,Integer> race2bonus = new HashMap<>();
        for(SP_Race race: races) {
            race2bonus.put(
                    race.getId().getSeasonNumber()+":"+race.getId().getRaceNumber(),
                    race.getMultiplier()
            );
        }
        int serialNumber = 0;
        drivers.sort(compareDrivers);
        for(SP_Driver driver: drivers) {
            serialNumber++;
            List<SP_Result> nut = results.stream()
                    .filter(p -> filterMatchesDriver(p, driver))
                    .sorted(compareResultsBySchedule)
                    .toList();
            statuses.add(calculateDriverStatus(
                driver, currentRace.get(), race2bonus, season2length, nut, serialNumber
            ));
        }
        out.setDrivers(statuses);
        return out;
    }

    @GetMapping("/sp/new/season/{league}")
    @ResponseBody
    SP_Season createSeason(@PathVariable String league,
                           @RequestParam(name="display") String displayName) {
        int seasonNumber = calculateNewSeasonNumber(league);
        if (seasonNumber == -1) return null; //TODO better error
        SP_SeasonID spSeasonID = new SP_SeasonID(league, seasonNumber);
        SP_Season spSeason = new SP_Season(spSeasonID, displayName);
        seasonRepo.save(spSeason);
        return spSeason;
    }

    @GetMapping("/sp/clone/schedule/{from}/{to}")
    @ResponseBody
    String clone(@PathVariable String from,
                 @PathVariable String to) {
        for(SP_Season season:seasonRepo.findAllByIdLeagueID(from)) {
            SP_SeasonID cloneID = new SP_SeasonID(to, season.getId().getSeasonNumber());
            SP_Season clone = new SP_Season(cloneID, season.getDisplayName());
            seasonRepo.save(clone);
        }
        int count = 0;
        for(SP_Race race:raceRepo.findAllByIdLeagueID(from)) {
            count++;
            SP_RaceID cloneID = new SP_RaceID(
                    to,
                    race.getId().getSeasonNumber(),
                    race.getId().getRaceNumber()
            );
            SP_Race clone = new SP_Race(
                    cloneID,
                    race.getDisplayName(),
                    race.getTrackName(),
                    race.getMultiplier()
            );
            raceRepo.save(clone);
        }
        String msg = "copied "+count+" races";
        return msg;
    }

    @GetMapping("/sp/new/race/{league}/{seasonNumber}")
    @ResponseBody
    SP_Race createRace(@PathVariable String league,
                       @PathVariable int seasonNumber,
                       @RequestParam(name="display") String displayName,
                       @RequestParam(name="multiplier") int multiplier,
                       @RequestParam(name="track") String trackName) {

        int raceNumber = calculateNewRaceNumber(league, seasonNumber);
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
        int driverNumber = calculateNewDriverNumber(league, team);
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

    static int points8[] = { 100, 75, 55, 40, 25, 15, 10, 5 };
    static int points9[] = { 100, 80, 60, 45, 30, 20, 15, 10, 5 };
    static int points10[] = { 100, 80, 65, 50, 35, 25, 20, 15, 10, 5 };

    static int points(int size, int place) {
        try {
            switch (size) {
                case 8: return points8[place-1];
                case 9: return points9[place-1];
                case 10: return points10[place-1];
                default: return 0;
            }
        } catch(ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }

    @PostMapping("/sp/replace/results/{league}/{season}/{race}")
    @ResponseBody
    @Transactional
    String replaceOrCreateRaceResults(
        @PathVariable String league,
        @PathVariable int season,
        @PathVariable int race,
        @RequestBody List<SP_Result> newResults
    ) {
        List<SP_Result> oldResults = resultRepo.findAllByIdLeagueIDAndIdSeasonNumberAndIdRaceNumber(
                league, season, race
        );
        Optional<SP_Race> raceOp = raceRepo.findByIdLeagueIDAndIdSeasonNumberAndIdRaceNumber(
          league, season, race
        );
        int bonus = raceOp.orElseThrow(() -> new IllegalStateException("No such race")).getMultiplier();
        for(SP_Result result: oldResults) {
            resultRepo.delete(result);
        }
        int size = newResults.size();
        for(SP_Result result: newResults) {
            result.setPoints(bonus * points(size, result.getId().getPlace()));
            resultRepo.save(result);
        }
        return "done";
    }

    static String resultKey(SP_Result result, boolean isTeam) {
        if(isTeam) {
            return result.getTeamID();
        } else {
            return result.getTeamID()+":$FKDJ)"+result.getDriverName();
        }
    }

    static class PointsComp implements Comparator<SP_Standing> {
        @Override
        public int compare(SP_Standing o1, SP_Standing o2) {
            if(o1==null && o2==null) return 0;
            if(o1==null) return 1;
            if(o2==null) return -1;
            return o2.getPoints() - o1.getPoints();
        }
    }

    static PointsComp pointsComp = new PointsComp();

    List<SP_Result> getCurrentSeasonResults(String league) {
        List<SP_Result> all = resultRepo.findAllByIdLeagueID(league);
        int s = 0;
        for(SP_Result result: all) {
            if(result.getId().getSeasonNumber() > s) s = result.getId().getSeasonNumber();
        }
        final int season = s;
        return all.stream().filter((x)->(x.getId().getSeasonNumber()==season)).toList();
    }

    List<SP_Result> getOldSeasonResults(String league, String scope) {
        int seasonNumber;
        try {
            seasonNumber = Integer.parseInt(scope);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unknown scope:" +scope);
        }
        return resultRepo.findAllByIdLeagueIDAndIdSeasonNumber(league, seasonNumber);
    }

    @GetMapping("/sp/standings/{league}/{type}/{scope}")
    @ResponseBody
    SP_Standings getDriverStandings(
            @PathVariable String league,
            @PathVariable String type,
            @PathVariable String scope // all=All TIME, season=current season, number=prior season
    ) {
        SP_Standings out = new SP_Standings();
        Map<String, SP_Standing> map = new HashMap<>();
        List<SP_Result> results;
        boolean isTeam = "team".equals(type);

        if("all".equals(scope)) results = resultRepo.findAllByIdLeagueID(league);
        else if("season".equals(scope)) {
            results = getCurrentSeasonResults(league);
        } else results = getOldSeasonResults(league, scope);

        for(SP_Result result: results) {
            String key = resultKey(result, isTeam);
            if(map.containsKey(key)) {
                SP_Standing standing = map.get(key);
                standing.setPoints(standing.getPoints()+result.getPoints());
            } else {
                map.put(key, new SP_Standing(
                    0, result.getPoints(), result.getTeamID(), result.getDriverName()
                ));
            }
        }

        //TODO sort and set places
        //TODO populate standings from map
        List<SP_Standing> standings = map.values().stream().sorted(pointsComp).toList();
        int tiePoints = 0;
        int nextPlace = 0;
        int rowCount = 0;
        for(SP_Standing standing: standings) {
            rowCount++;
            if(standing.getPoints() != tiePoints) {
                tiePoints = standing.getPoints();
                nextPlace = rowCount;
            }
            standing.setPlace(nextPlace);
        }

        out.setStandings(standings);
        out.setScope(scope);
        out.setType(type);
        return out;
    }

    @GetMapping("/sp/results/{league}/{season}/{race}")
    @ResponseBody
    List<SP_Result> replaceOrCreateRaceResults(
            @PathVariable String league,
            @PathVariable int season,
            @PathVariable int race
    ) {
        return resultRepo.findAllByIdLeagueIDAndIdSeasonNumberAndIdRaceNumber(league, season, race);
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
