package mel.volvox.GameChatServer.util;

import java.util.Map;

public class RaceCounter {
    private int season;
    private int race;
    private Map<Integer, Integer> seasonLengths;

    private static int OFF_SEASON = 5;

    public RaceCounter(Map<Integer,Integer> seasonLengths) {
        season = 1;
        race = 0;
        this.seasonLengths = seasonLengths;
    }

    public int skipTo(int seasonNumber, int raceNumber) {
        if(season == seasonNumber) {
            int out = raceNumber - race;
            race = raceNumber;
            return out;
        } else {
            int restOfSeason = seasonLengths.get(seasonNumber) - race + OFF_SEASON;
            season++;
            race = 1;
            return skipTo(seasonNumber, raceNumber) + restOfSeason;
        }
    }
}
