package com.example.chinhnb.placemap.Entity;

import android.support.annotation.NonNull;

/**
 * Created by CHINHNB on 3/19/2017.
 */

public class LocaltionItem extends ListItem {

    @NonNull
    private Localtion localtion;

    public LocaltionItem(@NonNull Localtion localtion) {
        this.localtion = localtion;
    }

    @NonNull
    public Localtion getLocaltion() {
        return localtion;
    }

    // here getters and setters
    // for title and so on, built
    // using event

    @Override
    public int getType() {
        return TYPE_LOCALTION;
    }

}