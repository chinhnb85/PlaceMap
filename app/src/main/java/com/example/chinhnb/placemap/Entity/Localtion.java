package com.example.chinhnb.placemap.Entity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by CHINHNB on 11/16/2016.
 */

public class Localtion {
    private int Id, AccountId;
    private boolean IsCheck;
    private String Name, Address, Email, Phone, Avatar;
    private Double Lag, Lng;

    public Localtion() {
    }

    public Localtion(int id,int accountId,boolean isCheck,String name, String address, String email, String phone, String avatar,Double lag,Double lng) {
        this.Id=id;
        this.AccountId=accountId;
        this.IsCheck=isCheck;
        this.Name = name;
        this.Address = address;
        this.Email = email;
        this.Phone = phone;
        this.Avatar = avatar;
        this.Lag=lag;
        this.Lng=lng;
    }

    public Localtion NewInstance(JSONObject obj){
        Localtion localtion=new Localtion();
        if(obj!=null){
            try {
                localtion.setId(obj.getInt("Id"));
                localtion.setIscheck(obj.getBoolean("IsCheck"));
                localtion.setName(obj.getString("Name"));
                localtion.setAddress(obj.getString("Address"));
                localtion.setEmail(obj.getString("Email"));
                localtion.setPhone(obj.getString("Phone"));
                localtion.setAvatar(obj.getString("Avatar"));
                localtion.setName(obj.getString("Lag"));
                localtion.setName(obj.getString("Lng"));
                localtion.setAccountId(obj.getInt("AccountId"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return localtion;
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

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        this.Address = address;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        this.Email = email;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        this.Phone = phone;
    }

    public String getAvatar() {
        return Avatar;
    }

    public void setAvatar(String avatar) {
        this.Avatar = avatar;
    }

    public boolean getIsCheck() {
        return IsCheck;
    }

    public void setIscheck(boolean ischeck) {
        this.IsCheck = ischeck;
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
