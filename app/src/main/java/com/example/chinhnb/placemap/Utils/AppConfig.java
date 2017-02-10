package com.example.chinhnb.placemap.Utils;

/**
 * Created by CHINHNB on 11/19/2016.
 */

public class AppConfig {
    // API
    public static String URL_ROOT = "http://103.47.192.100:8888/";
    public static String URL_LOGIN = "http://103.47.192.100:8888/api/accountApi/CheckLogin";
    public static String URL_LIST_LOCALTION = "http://103.47.192.100:8888/api/localtionApi/GetListLocaltionByAccountId";
    public static String URL_CHECK_LOCALTION = "http://103.47.192.100:8888/api/localtionApi/CheckedLocaltion";
    public static String URL_GET_LOCALTION = "http://103.47.192.100:8888/api/localtionApi/ViewDetailLocaltion";
    public static String URL_ADD_LOCALTION = "http://103.47.192.100:8888/api/localtionApi/AddNewLocaltion";
    public static String URL_EDIT_LOCALTION = "http://103.47.192.100:8888/api/localtionApi/EditLocaltion";
    public static String URL_UPLOAD_IMAGE = "http://103.47.192.100:8888/Handler/HandlerUpload.ashx";
    public static String URL_AUTO_LOCALTION_USER = "http://103.47.192.100:8888/api/localtionApi/AddNewAccountPlace";
}
