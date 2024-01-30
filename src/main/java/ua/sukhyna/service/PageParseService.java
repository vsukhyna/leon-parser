package ua.sukhyna.service;

import org.openqa.selenium.WebDriver;
import ua.sukhyna.vo.pages.LeaguePageVO;
import ua.sukhyna.vo.pages.MatchPageVO;
import ua.sukhyna.vo.pages.SportPageVO;

public interface PageParseService {

    SportPageVO parseSportPage(String url, WebDriver driver, String sport);

    LeaguePageVO parseTopLeaguePage(WebDriver driver, String url) throws InterruptedException;

    MatchPageVO parseMatchPage(WebDriver driver, String url) throws InterruptedException;
}
