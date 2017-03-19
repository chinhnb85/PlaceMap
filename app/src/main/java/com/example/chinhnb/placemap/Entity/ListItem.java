package com.example.chinhnb.placemap.Entity;

/**
 * Created by CHINHNB on 3/19/2017.
 */

public abstract class ListItem {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_LOCALTION = 1;

    abstract public int getType();
}
