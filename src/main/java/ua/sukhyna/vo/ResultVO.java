package ua.sukhyna.vo;

import lombok.Data;
import ua.sukhyna.vo.pages.MatchPageVO;

import java.util.Map;

@Data
public class ResultVO {

    private String sport;
    private String league;
    private String matchName;
    private String datetime;
    private String matchId;
    private Map<String, String> marketOdds;

    public ResultVO(MatchPageVO match) {
        this.sport = match.getSport();
        this.league = match.getLeague();
        this.matchName = match.getName();
        this.datetime = match.getDatetime();
        this.matchId = match.getMatchId();
        this.marketOdds = match.getMarketOdds();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(sport.toUpperCase()).append(", ").append(league).append("\n");
        result.append(matchName).append(", ").append(datetime).append(", ").append(matchId).append("\n");
        for (Map.Entry<String, String> entry : marketOdds.entrySet()) {
            result.append(entry.getKey()).append("\n ").append(removeOddLineBreaks(entry.getValue())).append("\n");
        }
        return result.toString();
    }

    private String removeOddLineBreaks(String input) {
        String regex = "(?<=\\s)(\\d+\\.\\d+)(?=\\s)"; //шукаємо коєфіцієнти, щоб після них робити перенос на новий рядок
        return input.replaceAll("\n", " ").replaceAll(regex, "$1\n");
    }
}
