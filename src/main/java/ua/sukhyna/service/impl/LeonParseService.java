package ua.sukhyna.service.impl;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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
        startPage.get(url + "bets/" + sport);
        List<WebElement> topLeagues = getElementWithWaiting(startPage, By.cssSelector(".undefined.leagues-list--top_Q3nAI"))
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
    public LeaguePageVO parseTopLeaguePage(WebDriver driver) throws InterruptedException {

        LeaguePageVO leagueVO = new LeaguePageVO();

        List<WebElement> matches =
                getElementWithWaiting(driver, By.cssSelector(".sportline-events-list_bf1nX"))
                        .findElements(By.cssSelector(".sportline-event-block_isrbB"));

        List<MatchPageVO> matchesVO = new ArrayList<>();

        for (WebElement match : matches) {
            TimeUnit.SECONDS.sleep(3);
            String matchName = parseMatchName(match);
            String matchDate = parseMatchDate(match);
            String matchLink = getLink(match.findElement(By.cssSelector("a[href]")));
            String matchId = parseMatchId(matchLink);

            MatchPageVO matchPageVO = new MatchPageVO();
            matchPageVO.setName(matchName);
            matchPageVO.setDatetime(matchDate);
            matchPageVO.setMatchUrl(matchLink);
            matchPageVO.setMatchId(matchId);

            matchesVO.add(matchPageVO);
        }
        leagueVO.setMatches(matchesVO);
        return leagueVO;
    }

    @Override
    public MatchPageVO parseMatchPage(WebDriver driver) throws InterruptedException {
        TimeUnit.SECONDS.sleep(2);
        WebElement allMarketsButton = getElementWithWaiting(driver,
                By.xpath("//*/text()[contains(.,'" + "All markets" + "')]/parent::*")); //кнопка для переходу на всі маркети
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
        return parts[0] + " - " + parts[1];
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

    private WebElement getElementWithWaiting(WebDriver driver, By locator) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    private String getLink(WebElement element) {
        return element.getAttribute("href");
    }
}