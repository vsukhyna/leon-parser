package ua.sukhyna.vo.pages;

import lombok.Data;

import java.util.List;

@Data
public class LeaguePageVO {
    private String leagueName;
    private String leagueUrl;
    private List<MatchPageVO> matches;
}
