package com.tirex.avr_assembler.res_for_extensions;

import android.support.annotation.DrawableRes;

public class ResForExtensions {
    String[] listExtensions;
    @DrawableRes
    int res;

    public ResForExtensions(String enumExtensions, @DrawableRes int resource) {
        listExtensions = enumExtensions.split(",");
        res = resource;
    }

    public String[] getListExtensions() {
        return listExtensions;
    }

    public int getRes() {
        return res;
    }
}
