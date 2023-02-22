package com.tirex.avr_assembler;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewDebug;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

import static android.os.SystemClock.sleep;

public class CustomTabView extends LinearLayout {
    private View tabDividerUp, tabDividerDown, tabDividerDownDown;
    private HorizontalScrollView horizontalScrollView;
    private LinearLayout tabWidget;
    private LinearLayout tabContent;
    private ArrayList<TabComponent> listOfTabs = new ArrayList<>();
    private int currentFocus=-1;
    private String colorTab = "#EFEFEF";//"#E6E6E6";//"#DFFFDF";
    private String colorTabFocused = "#A0D37E";//"#ADFAAD";//"#C5FB8E"; // int = 12974990
    private String colorTabBackground = "#EFEFEF";

    public class TabComponent {
        private LinearLayout tabWidgetColumn;
        private Button tab;
        private LinearLayout content;
        private View underline;
        public LinearLayout getContent (){ return content; }
        public void setContent (View view){ content.addView(view);}
        public Button getTab (){ return tab;};
    }

    public CustomTabView (Context context){
        super (context);
        init (this.getContext());
        /*Resources res = getResources();
        drawableButtonTabShape = ResourcesCompat.getDrawable(res, R.drawable.tab_shape, null);
        drawableButtonTabShapeFocused = ResourcesCompat.getDrawable(res, R.drawable.button_tab_shape_focused, null);*/
    }

    private void init (Context context){
        tabDividerUp = new View(context);
        settingsTabDividerUp (tabDividerUp);
        horizontalScrollView = new HorizontalScrollView(context);
        settingsHorizontalScrollView ();
        tabWidget = new LinearLayout(context);
        settingsTabWidget ();
        horizontalScrollView.addView(tabWidget);
        tabDividerDown = new View(context);
        settingsTabDividerDown(tabDividerDown);
        LinearLayout linearLayoutForContent = new LinearLayout(context);
        settingsLinearLayout(linearLayoutForContent);
        tabContent = new LinearLayout(context);
        settingsTabContent();
        settingsLinearLayout (this);
        this.addView(tabDividerUp);
        this.addView(horizontalScrollView);
        this.addView(tabDividerDown);
        this.addView(tabContent);
    }

    private void settingsTabDividerUp (View tabDivider){
        tabDivider.setLayoutParams(new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, getPx(1) ));
        tabDivider.setBackgroundColor(Color.DKGRAY);
    }

    private void settingsHorizontalScrollView (){
        horizontalScrollView.setLayoutParams(new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT));
        horizontalScrollView.setHorizontalScrollBarEnabled(false);
        horizontalScrollView.setBackgroundColor(Color.parseColor(colorTabBackground));
    }

    private void settingsTabWidget (){
        tabWidget.setLayoutParams(new LinearLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT));
        tabWidget.setOrientation(LinearLayout.HORIZONTAL);
        tabWidget.setBackgroundColor(Color.parseColor(colorTabBackground));
    }

    private void settingsTabDividerDown (View tabDivider){
        tabDivider.setLayoutParams(new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, getPx(2) ));
        tabDivider.setBackgroundColor(Color.parseColor(colorTabFocused));
    }

    private void settingsTabContent (){
        tabContent.setLayoutParams(new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
        tabContent.setOrientation(LinearLayout.VERTICAL);
    }

    private void settingsLinearLayout (LinearLayout linearLayout){
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);
    }

    public TabComponent addTab (String label){
        final TabComponent newTabComponent = new TabComponent();
       /* if (listOfTabs.size() > 0){
            View tabVerticalDivider = new View(this.getContext());
            tabVerticalDivider.setLayoutParams(new LinearLayout.LayoutParams(1, ActionBar.LayoutParams.MATCH_PARENT));
            tabVerticalDivider.setBackgroundColor(Color.DKGRAY);
            tabWidget.addView(tabVerticalDivider);
        }*/
        newTabComponent.tab = createTab(label);
        newTabComponent.content = createTabContent();
        newTabComponent.underline = createUnderline(newTabComponent.tab);
        newTabComponent.tabWidgetColumn = createTabWidgetColumn();
        newTabComponent.tabWidgetColumn.addView(newTabComponent.tab);
        newTabComponent.tabWidgetColumn.addView(newTabComponent.underline);
        tabWidget.addView(newTabComponent.tabWidgetColumn);
        listOfTabs.add(newTabComponent);
        setFocus(newTabComponent);
        newTabComponent.tab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setFocus(newTabComponent);
            }
        });
        measureDimension(newTabComponent.getTab());
        return newTabComponent;
    }

    private void measureDimension (View v) {
        v.post(new Runnable() {
            @Override
            public void run() {
                horizontalScrollView.smoothScrollTo(horizontalScrollView.getMeasuredWidth(), 0);;
            }
        });
    }

    private Button createTab (String label){
        Button newTab = new Button(tabWidget.getContext());
        /*newTab.setBackgroundColor(Color.rgb(0,100,0));
        newTab.setPadding(10, 0, 10, 0);*/
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            newTab.setBackground(drawableButtonTabShape);
        }*/
        newTab.getBackground().setColorFilter(Color.parseColor(colorTab), PorterDuff.Mode.MULTIPLY);
        /*FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, 0);
        newTab.setLayoutParams(layoutParams);*/
        newTab.setAllCaps(false);
        newTab.setText(label);
        //tabWidget.addView(newTab);
        return newTab;
    }

    private LinearLayout createTabContent (){
        LinearLayout newTabContent = new LinearLayout(tabContent.getContext());
        newTabContent.setLayoutParams(new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
        newTabContent.setOrientation(LinearLayout.VERTICAL);
        return newTabContent;
    }

    private View createUnderline (Button buttonTab){
        View view = new View(tabWidget.getContext());
        view.setLayoutParams(new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, getPx(5) ));
        view.setBackgroundColor(horizontalScrollView.getSolidColor());
        return view;
    }

    private LinearLayout createTabWidgetColumn (){
        LinearLayout linearLayout = new LinearLayout(tabWidget.getContext());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        return linearLayout;
    }

    void setFocus(TabComponent tab){
        int numTab = listOfTabs.indexOf(tab);
        if (numTab != currentFocus){
            tabContent.removeAllViews();
            tabContent.addView(tab.getContent());
            if (currentFocus != -1){
                listOfTabs.get(currentFocus).getTab().getBackground().setColorFilter(Color.parseColor(colorTab), PorterDuff.Mode.MULTIPLY);
                listOfTabs.get(currentFocus).underline.setBackgroundColor(horizontalScrollView.getSolidColor());
            }
            tab.getTab().getBackground().setColorFilter(Color.parseColor(colorTabFocused), PorterDuff.Mode.MULTIPLY);
            tab.underline.setBackgroundColor(Color.parseColor(colorTabFocused));
            currentFocus = numTab;
            tab.getContent().requestFocus();
        }
        int coordXForTab = 0;
        for (int i =0; i<numTab; i++){
            coordXForTab += listOfTabs.get(i).getTab().getMeasuredWidth();
        }
        int subWidth = horizontalScrollView.getScrollX() - coordXForTab;
        //Toast.makeText(getContext(), Integer.toString(subWidth), Toast.LENGTH_SHORT).show();
        if (subWidth >= 0) {
            if (numTab == 0) {
                horizontalScrollView.scrollTo(0, 0);
            } else {
                horizontalScrollView.scrollBy(-subWidth, 0);
            }
        }else {
            coordXForTab += tab.getTab().getMeasuredWidth();
            subWidth = coordXForTab - horizontalScrollView.getScrollX() - horizontalScrollView.getWidth();
            if (subWidth>0) {
                if (numTab == listOfTabs.size()-1){
                    horizontalScrollView.scrollTo(horizontalScrollView.getMeasuredWidth(), 0);
                }else {
                    horizontalScrollView.scrollBy(subWidth, 0);
                }
            }
        }
    }

    public void deleteTab (TabComponent tabComponent){
        int index = listOfTabs.indexOf(tabComponent);
        if (index>=0){
            deleteTab(index);
        }
    }

    public void deleteTab (int index){
        if ((index>=0)&&(index<listOfTabs.size())){
            //listOfTabs.get(index).getTab().setVisibility(View.GONE);
            listOfTabs.get(index).tabWidgetColumn.removeAllViews();
            listOfTabs.get(index).tabWidgetColumn.setVisibility(View.GONE);
            listOfTabs.get(index).content.removeAllViews();
            listOfTabs.get(index).content.setVisibility(View.GONE);
            //listOfTabs.get(index).underline.setVisibility(View.GONE);
            listOfTabs.remove(index);
            currentFocus = -1;
            if (listOfTabs.size()>0) {
                if (index == listOfTabs.size()) {
                    setFocus(listOfTabs.get(index-1));;
                }else{
                    setFocus(listOfTabs.get(index));
                }
            }
        }
    }

    public int getPx(int dp){
        float scale = getResources().getDisplayMetrics().density;
        return((int) (dp * scale + 0.5f));
    }

    public int countTabs() {
        return listOfTabs.size();
    }

    public int getCurrentTabIndex (){  return currentFocus;  }

}
