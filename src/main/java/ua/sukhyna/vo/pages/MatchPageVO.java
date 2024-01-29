package ua.sukhyna.vo.pages;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class MatchPageVO {

    private String sport;
    private String league;
    private String name;
    private String datetime;
    private String matchId;
    private String matchUrl;
    private Map<String, String> marketOdds = new LinkedHashMap<>();

}
