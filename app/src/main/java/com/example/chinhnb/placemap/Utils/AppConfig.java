package com.example.chinhnb.placemap.Utils;

/**
 * Created by CHINHNB on 11/19/2016.
 */

public class AppConfig {
    // API
    public static String URL_ROOT = "http://45.125.239.76:9999";
    public static String URL_LOGIN = URL_ROOT+"/api/accountApi/CheckLogin";
    public static String URL_LIST_LOCALTION = URL_ROOT+"/api/localtionApi/GetListLocaltionByAccountId";
    public static String URL_LIST_LOCALTION_BY_STATUS = URL_ROOT+"/api/localtionApi/GetListLocaltionByAccountIdAndStatus";
    public static String URL_CHECK_LOCALTION = URL_ROOT+"/api/localtionApi/CheckedLocaltion";
    public static String URL_GET_LOCALTION = URL_ROOT+"/api/localtionApi/ViewDetailLocaltion";
    public static String URL_ADD_LOCALTION = URL_ROOT+"/api/localtionApi/AddNewLocaltion";
    public static String URL_EDIT_LOCALTION = URL_ROOT+"/api/localtionApi/EditLocaltion";
    public static String URL_UPLOAD_IMAGE = URL_ROOT+"/Handler/HandlerUpload.ashx";
    public static String URL_AUTO_LOCALTION_USER = URL_ROOT+"/api/localtionApi/AddNewAccountPlace";
}
