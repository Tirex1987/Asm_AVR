package com.tirex.avr_assembler.res_for_extensions;

import android.support.annotation.DrawableRes;

import java.util.ArrayList;
import java.util.List;

public class ListResForExtensions {

    private ArrayList<ResForExtensions> resForExtensionsList = new ArrayList<>();
    private @DrawableRes int defaultRes;

    public ListResForExtensions (ArrayList<ResForExtensions> list, @DrawableRes int defaultResource) {
        resForExtensionsList = list;
        defaultRes = defaultResource;
    }

    public ArrayList<ResForExtensions> getList() {
        return resForExtensionsList;
    }

    public @DrawableRes int getDefaultRes() {
        return defaultRes;
    }

}
