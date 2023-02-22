package com.tirex.avr_assembler;

import android.app.ActionBar;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TextView;

public class NumberedEditTextInTab {
    private String tag;
    private String label;
    private NumberedEditText codeEditor;
    private String fileName;
    private TabHost tabHost;
    private TabHost.TabSpec tabSpec;
    private TextView tabTextView;
    private Button tabBtnClose;

    public void createTab (TabHost tabHost,String tag, String label){
        this.tag = tag;
        this.label = label;
        this.tabHost = tabHost;
        tabHost.setup();
        tabSpec = tabHost.newTabSpec(tag);
        //tabSpec.setContent(R.id.scroll);
        tabSpec.setContent(tabFactory);
        //tabSpec.setIndicator(label);
        View rootView1 = (LinearLayout) LayoutInflater.from(tabHost.getContext()).inflate(R.layout.custom_tab, null);
        tabTextView = rootView1.findViewById(R.id.tabTextView);
        tabTextView.setText(label);
        tabSpec.setIndicator(rootView1);
        tabBtnClose = rootView1.findViewById(R.id.btnClose);
        tabHost.addTab(tabSpec);
    }

    TabHost.TabContentFactory tabFactory = new TabHost.TabContentFactory() {
        @Override
        public View createTabContent(String tag) {
            codeEditor = new NumberedEditText(tabHost.getContext());
            return codeEditor;
        }
    };

    public NumberedEditText getNumberedEditText(){
        return codeEditor;
    }

    public TabHost.TabSpec getTabSpec (){
        return tabSpec;
    }

    public Button getTabBtnClose (){
        return tabBtnClose;
    }
}
