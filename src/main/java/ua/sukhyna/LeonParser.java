package ua.sukhyna;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import ua.sukhyna.service.PageParseService;
import ua.sukhyna.service.impl.LeonParseService;
import ua.sukhyna.vo.ResultVO;
import ua.sukhyna.vo.pages.LeaguePageVO;
import ua.sukhyna.vo.pages.MatchPageVO;
import ua.sukhyna.vo.pages.SportPageVO;

@Slf4j
public class LeonParser {

    private static final String START_URL = "https://leonbets.com/";
    private final PageParseService parseService = new LeonParseService();

    public void getBettingBySport(String sport, WebDriver driver) throws InterruptedException {

        log.debug("Loading sport ({}) page...", sport);

        SportPageVO sportData = getSportData(sport, driver);

        log.debug("Found {} leagues. Loading top leagues pages... ", sportData.getTopLeagues().size());

        for (LeaguePageVO topLeague : sportData.getTopLeagues()) {
            LeaguePageVO leagueData = getLeagueData(driver, topLeague);

            log.debug("Found {} matches. Loading matches pages... ", leagueData.getMatches().size());

            for (MatchPageVO match : leagueData.getMatches()) {
                driver.get(match.getMatchUrl());
                MatchPageVO matchData = getMatchData(driver, match.getMatchUrl());
                matchData.setName(match.getName());
                matchData.setMatchId(match.getMatchId());
                matchData.setDatetime(match.getDatetime());
                matchData.setSport(sportData.getSportName());
                matchData.setLeague(leagueData.getLeagueName());
                printBets(matchData);
            }
        }
    }

    private SportPageVO getSportData(String sport, WebDriver driver) {
        return parseService.parseSportPage(START_URL, driver, sport);
    }

    private LeaguePageVO getLeagueData(WebDriver driver, LeaguePageVO topLeague) throws InterruptedException {
        LeaguePageVO leaguePageVO = parseService.parseTopLeaguePage(driver, topLeague.getLeagueUrl());
        leaguePageVO.setLeagueName(topLeague.getLeagueName());
        return leaguePageVO;
    }

    private MatchPageVO getMatchData(WebDriver driver, String matchUrl) throws InterruptedException {
        return parseService.parseMatchPage(driver, matchUrl);
    }

    private void printBets(MatchPageVO matchData) {
        System.out.println(new ResultVO(matchData));
    }
}