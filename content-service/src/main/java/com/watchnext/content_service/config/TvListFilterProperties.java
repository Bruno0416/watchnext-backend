package com.watchnext.content_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.HashSet;

// umbrales calibrables de las listas curadas de tv (popular/top-rated/on-the-air) sin recompilar
@Component
@ConfigurationProperties(prefix = "watchnext.tv-filters")
public class TvListFilterProperties {

    private int voteCountPopular = 100;
    private int voteCountTopRated = 200;
    private int voteCountOnTheAir = 20;
    private int onAirPastDays = 14;
    private int onAirFutureDays = 7;
    private Set<Integer> excludedShowIds = new HashSet<>();

    public int getVoteCountPopular() {
        return voteCountPopular;
    }

    public void setVoteCountPopular(int voteCountPopular) {
        this.voteCountPopular = voteCountPopular;
    }

    public int getVoteCountTopRated() {
        return voteCountTopRated;
    }

    public void setVoteCountTopRated(int voteCountTopRated) {
        this.voteCountTopRated = voteCountTopRated;
    }

    public int getVoteCountOnTheAir() {
        return voteCountOnTheAir;
    }

    public void setVoteCountOnTheAir(int voteCountOnTheAir) {
        this.voteCountOnTheAir = voteCountOnTheAir;
    }

    public int getOnAirPastDays() {
        return onAirPastDays;
    }

    public void setOnAirPastDays(int onAirPastDays) {
        this.onAirPastDays = onAirPastDays;
    }

    public int getOnAirFutureDays() {
        return onAirFutureDays;
    }

    public void setOnAirFutureDays(int onAirFutureDays) {
        this.onAirFutureDays = onAirFutureDays;
    }

    public Set<Integer> getExcludedShowIds() {
        return excludedShowIds;
    }

    public void setExcludedShowIds(Set<Integer> excludedShowIds) {
        this.excludedShowIds = excludedShowIds;
    }
}
