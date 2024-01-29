package ua.sukhyna.vo.pages;

import lombok.Data;

import java.util.List;

@Data
public class SportPageVO {
    private String sportName;
    private List<LeaguePageVO> topLeagues;
}
