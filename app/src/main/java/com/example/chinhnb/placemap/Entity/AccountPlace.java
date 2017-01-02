package com.example.chinhnb.placemap.Entity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by CHINHNB on 11/16/2016.
 */

public class AccountPlace {
    private int Id, AccountId;
    private Double Lag, Lng;

    public AccountPlace() {
    }

    public AccountPlace(int id, int accountId, Double lag, Double lng) {
        this.Id=id;
        this.AccountId=accountId;
        this.Lag=lag;
        this.Lng=lng;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        this.Id = id;
    }

    public int getAccountId() {
        return AccountId;
    }

    public void setAccountId(int accountId) {
        this.AccountId = accountId;
    }

    public Double getLag() {
        return Lag;
    }

    public void setLag(Double lag) {
        this.Lag = lag;
    }

    public Double getLng() {
        return Lng;
    }

    public void setLng(Double lng) {
        this.Lag = lng;
    }
}
