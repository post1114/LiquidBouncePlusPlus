package com.thealtening.api.data;

public class AccountData {

    private final String username;
    private final String token;

    public AccountData(String username, String token) {
        this.username = username;
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }
}
