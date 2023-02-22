package com.tirex.avr_assembler;

import android.view.View;
import android.widget.TabHost;

import java.util.ArrayList;

public class ListOfNumberedEditTextInTab {
    private TabHost tabHost;
    private ArrayList<NumberedEditTextInTab> listOfTabs = new ArrayList<>();
    private int indexOfTag = 0;

    public ListOfNumberedEditTextInTab (TabHost tabHost){
        this.tabHost = tabHost;
    }

    public void addTab (String label){
        final NumberedEditTextInTab codeEd = new NumberedEditTextInTab();
        codeEd.createTab(tabHost, "Tag "+ indexOfTag, label);
        listOfTabs.add(codeEd);
        codeEd.getTabBtnClose().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeTab(listOfTabs.indexOf(codeEd));
            }
        });
        indexOfTag++;
    }

    public void removeTab (int index){
        if (index<listOfTabs.size()){
            listOfTabs.remove(index);
            tabHost.clearAllTabs();
            for (int i=0; i<listOfTabs.size(); i++){
                tabHost.addTab(listOfTabs.get(i).getTabSpec());
            }
        }
    }

    public NumberedEditText getNumberedEditText (Integer index){
        return listOfTabs.get(index).getNumberedEditText();
    }

    public void clear (){
        listOfTabs.clear();
    }
}
