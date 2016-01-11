package com.example.romsm.lap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class UserAccount implements Serializable {
    String accessToken, refreshToken, tokenType, scope, expiresIn;

    public UserAccount(String jsonString) throws JSONException {
        JSONObject jsonUser = new JSONObject(jsonString);
        this.accessToken = (String) jsonUser.optString("access_token");
        this.refreshToken = (String) jsonUser.optString("refresh_token");
        this.tokenType = (String) jsonUser.optString("token_type");
        this.scope = (String) jsonUser.optString("scope");
        this.expiresIn = (String) jsonUser.optString("expires_in");
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getScope() {
        return scope;
    }

    public String getExpiresIn() {
        return expiresIn;
    }
}
