package ua.sukhyna.service.impl;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ua.sukhyna.service.PageParseService;
import ua.sukhyna.vo.pages.LeaguePageVO;
import ua.sukhyna.vo.pages.MatchPageVO;
import ua.sukhyna.vo.pages.SportPageVO;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LeonParseService implements PageParseService {

    @Override
    public SportPageVO parseSportPage(String url, WebDriver startPage, String sport) {
        String sportUrl = url + "bets/" + sport;
        startPage.get(sportUrl);

        List<WebElement> topLeagues = getElementOnPageWithWaiting(
                startPage,
                By.cssSelector(".undefined.leagues-list--top_Q3nAI"),
                sportUrl)
                .findElements(By.cssSelector("a[href]"));

        List<LeaguePageVO> topLeaguesVO = new ArrayList<>();

        for (WebElement topLeague : topLeagues) {
            String leaguePageLink = getLink(topLeague);
            String leagueName = topLeague.getText().split("\n")[0];
            LeaguePageVO leagueData = new LeaguePageVO();
            leagueData.setLeagueName(leagueName);
            leagueData.setLeagueUrl(leaguePageLink);
            topLeaguesVO.add(leagueData);
        }

        SportPageVO sportVO = new SportPageVO();
        sportVO.setSportName(correctSportName(sport));
        sportVO.setTopLeagues(topLeaguesVO);
        return sportVO;
    }

    @Override
    public LeaguePageVO parseTopLeaguePage(WebDriver driver, String leagueUrl) throws InterruptedException {
        driver.get(leagueUrl);
        TimeUnit.SECONDS.sleep(2);
        LeaguePageVO leagueVO = new LeaguePageVO();

        List<MatchPageVO> matchesVO = new ArrayList<>();

        MatchPageVO matchPageVO = new MatchPageVO();

        try {
            matchPageVO = getMatchWithData(driver, leagueUrl);
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            parseTopLeaguePage(driver, leagueUrl);
        }

        matchesVO.add(matchPageVO);
        leagueVO.setMatches(matchesVO);
        return leagueVO;
    }

    private MatchPageVO getMatchWithData(WebDriver driver, String leagueUrl) throws InterruptedException {
        WebElement firstMatch =
                getElementOnPageWithWaiting(driver,
                        By.cssSelector(".sportline-events-list_bf1nX"),
                        leagueUrl).findElement(By.cssSelector(".sportline-event-block_isrbB"));

        TimeUnit.SECONDS.sleep(2);
        String matchName = parseMatchName(firstMatch);
        String matchDate = parseMatchDate(firstMatch);
        String matchLink = getLink(firstMatch.findElement(By.cssSelector("a[href]")));
        String matchId = parseMatchId(matchLink);

        MatchPageVO matchPageVO = new MatchPageVO();
        matchPageVO.setName(matchName);
        matchPageVO.setDatetime(matchDate);
        matchPageVO.setMatchUrl(matchLink);
        matchPageVO.setMatchId(matchId);
        return matchPageVO;
    }

    @Override
    public MatchPageVO parseMatchPage(WebDriver driver, String matchUrl) throws InterruptedException {
        driver.get(matchUrl);
        TimeUnit.SECONDS.sleep(2);

        WebElement allMarketsButton = getElementOnPageWithWaiting(driver,
                By.xpath("//*/text()[contains(.,'" + "All markets" + "')]/parent::*"),
                matchUrl); //кнопка для переходу на всі маркети

        allMarketsButton.click();

        Map<String, String> matchMarkets = parseMatchMarkets(driver);

        MatchPageVO matchVO = new MatchPageVO();
        matchVO.setMarketOdds(matchMarkets);

        return matchVO;
    }

    private Map<String, String> parseMatchMarkets(WebDriver driver) {
        List<WebElement> markets = driver.findElements(By.cssSelector(".sport-event-details-market-group__wrapper"));

        Map<String, String> matchMarkets = new LinkedHashMap<>();

        for (WebElement market : markets) {
            String marketName = market.findElement(By.cssSelector(".sport-event-details-market-group__header")).getText();
            String marketOdds = market.findElement(By.cssSelector(".sport-event-details-market-group__content")).getText();
            matchMarkets.put(marketName, marketOdds);
        }
        return matchMarkets;
    }

    private String parseMatchId(String matchUrl) {
        String[] parts = matchUrl.split("/");
        String lastPart = parts[parts.length - 1];
        return lastPart.split("-")[0];
    }

    private String parseMatchName(WebElement match) {
        String[] parts = match.getText().split("\n");
        if (parts.length > 1)
            return parts[0] + " - " + parts[1];
        else return parts[0];
    }

    private String parseMatchDate(WebElement match) {
        String[] parts = match.getText().split("\n");

        try {
            int currentYear = LocalDateTime.now().getYear();
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy.dd.MMHH:mm");
            LocalDateTime dateTime = LocalDateTime.parse(currentYear + "." + parts[2], inputFormatter);
            LocalDateTime utcDateTime = dateTime.atOffset(ZoneOffset.UTC).toLocalDateTime();
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'");
            return utcDateTime.format(outputFormatter);
        } catch (DateTimeParseException e) {
            return parts[2];
        }
    }

    private String correctSportName(String sport) {
        if (sport.equals("soccer")) {
            return "Football";
        } else {
            return sport.substring(0, 1).toUpperCase() + sport.substring(1);
        }
    }

    private WebElement getElementOnPageWithWaiting(WebDriver driver, By locator, String url) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (TimeoutException e) {
            driver.get(driver.getCurrentUrl());
            return getElementOnPageWithWaiting(driver, locator, url);
        }
    }

    private String getLink(WebElement element) {
        return element.getAttribute("href");
    }
}