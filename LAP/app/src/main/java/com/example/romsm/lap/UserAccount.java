package com.example.romsm.lap;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;


public class UserAccount implements Serializable {
    private String accessToken, refreshToken, tokenType, scope, expiresIn, username;
    private Boolean isStaff;

    public UserAccount(String jsonString) throws JSONException {
        JSONObject jsonUser = new JSONObject(jsonString);
        this.accessToken = (String) jsonUser.optString("access_token");
        this.refreshToken = (String) jsonUser.optString("refresh_token");
        this.tokenType = (String) jsonUser.optString("token_type");
        this.scope = (String) jsonUser.optString("scope");
        this.expiresIn = (String) jsonUser.optString("expires_in");
    }

    public UserAccount(String accessToken, String refreshToken){
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() { return accessToken; }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokens(String jsonString) throws JSONException{
        JSONObject jsonUser = new JSONObject(jsonString);
        this.accessToken = (String) jsonUser.optString("access_token");
        this.refreshToken = (String) jsonUser.optString("refresh_token");
    }

    public String getScope() {
        return scope;
    }

    public String getExpiresIn() {
        return expiresIn;
    }

    public String getUsername(){ return username; }

    public void setUsername(String username){ this.username = username; }

    public Boolean getIsStaff(){ return isStaff; }

    public void setIsStaff(Boolean isStaff){ this.isStaff = isStaff; }

    public void setUserInfo(String jsonString) throws JSONException{
        JSONObject jsonUser = new JSONObject(jsonString);
        setUsername((String) jsonUser.optString("username"));
        setIsStaff(jsonUser.optBoolean("is_staff"));
    }
}
