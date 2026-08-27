package com.thealtening.api;

import com.thealtening.api.data.AccountData;
import java.util.concurrent.CompletableFuture;

public class TheAltening {

    private final String apiKey;

    public TheAltening(String apiKey) {
        this.apiKey = apiKey;
    }

    public AccountData getAccountData() {
        return new AccountData("stub_user", "stub_token");
    }

    public static class Asynchronous {

        private final TheAltening theAltening;

        public Asynchronous(TheAltening theAltening) {
            this.theAltening = theAltening;
        }

        public CompletableFuture<AccountData> getAccountData() {
            return CompletableFuture.completedFuture(new AccountData("stub_user", "stub_token"));
        }
    }
}
