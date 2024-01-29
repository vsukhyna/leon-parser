package ua.sukhyna;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import ua.sukhyna.service.PageParseService;
import ua.sukhyna.service.impl.LeonParseService;
import ua.sukhyna.vo.ResultVO;
import ua.sukhyna.vo.pages.LeaguePageVO;
import ua.sukhyna.vo.pages.MatchPageVO;
import ua.sukhyna.vo.pages.SportPageVO;

import java.util.List;

import static ua.sukhyna.configuration.WebDriverConfig.initDriver;

@Slf4j
public class LeonParser {

    private static final String START_URL = "https://leonbets.com/";
    private static final List<String> SPORTS = List.of("soccer", "tennis", "hockey", "basketball");
    private final PageParseService parseService = new LeonParseService();

    public void getBetting() throws InterruptedException {
        for (String sport : SPORTS) {
            getBettingBySport(sport);
        }
    }

    private void getBettingBySport(String sport) throws InterruptedException {

        log.info("Loading webdriver...");

        WebDriver driver = initDriver();

        log.info("Loading sport ({}) page...", sport);

        SportPageVO sportData = getSportData(sport, driver);

        log.info("Loading top leagues pages...");

        for (LeaguePageVO topLeague : sportData.getTopLeagues()) {
            driver.get(topLeague.getLeagueUrl());
            LeaguePageVO leagueData = getLeagueData(driver, topLeague);

            for (MatchPageVO match : leagueData.getMatches()) {
                driver.get(match.getMatchUrl());
                MatchPageVO matchData = getMatchData(driver);
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
        LeaguePageVO leaguePageVO = parseService.parseTopLeaguePage(driver);
        leaguePageVO.setLeagueName(topLeague.getLeagueName());
        return leaguePageVO;
    }

    private MatchPageVO getMatchData(WebDriver driver) throws InterruptedException {
        return parseService.parseMatchPage(driver);
    }

    private void printBets(MatchPageVO matchData) {
        System.out.println(new ResultVO(matchData));
    }
}