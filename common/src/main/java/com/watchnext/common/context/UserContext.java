package com.watchnext.common.context;

public record UserContext(String country, String region) {
    public static UserContext defaultContext() {
        return new UserContext("US", "DEFAULT");
    }
}
