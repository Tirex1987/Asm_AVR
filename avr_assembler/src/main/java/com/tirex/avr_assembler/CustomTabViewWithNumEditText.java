package com.tirex.avr_assembler;

import android.content.Context;
import android.view.View;

import java.util.ArrayList;

public class CustomTabViewWithNumEditText extends CustomTabView {
    private ArrayList <TabComponentWithNumEditText> listOfTabsNET = new ArrayList<>();


    public CustomTabViewWithNumEditText (Context context){
        super (context);
    }

    public class TabComponentWithNumEditText {
        private TabComponent tabComponent;
        private String filePath; //путь к файлу, в котором хранится текст из данного NumberedEditText
        private String fileName;
        private boolean isMain; // содержит true, если в данной вкладке находится основной листинг программы, с которого начинается компиляция
        private NumberedEditText numberedEditText;
        private void setTabComponent(TabComponent tabComponent) { this.tabComponent = tabComponent;  }
        public TabComponent getTabComponent() {  return tabComponent;    }
        private void setFilePath (String filePath) {   this.filePath = filePath;   }
        public String getFilePath () {  return filePath;   }
        private void setFileName (String fileName) {  this.fileName = fileName;  }
        public String getFileName () {  return fileName;  }
        public NumberedEditText getNumberedEditText() {  return numberedEditText;    }
    }

    public TabComponent addTab (String fileName, String filePath){
        TabComponentWithNumEditText tab = new TabComponentWithNumEditText();
        tab.setTabComponent(super.addTab(fileName));
        tab.setFileName(fileName);
        tab.setFilePath(filePath);
        if (countTabs()>0){
            tab.isMain = false;
        }else{
            tab.isMain = true;
        }
        tab.numberedEditText = new NumberedEditText(this.getContext());
        tab.getTabComponent().setContent(tab.getNumberedEditText());
        listOfTabsNET.add(tab);
        return tab.getTabComponent();
    }

    public NumberedEditText getNumberedEditText(int index) {
        return listOfTabsNET.get(index).getNumberedEditText();
    }

    public void closeCurrentTab (){
        closeTab(this.getCurrentTabIndex());
    }

    public void closeTab (int index){
        if ((index>=0)&&(index<listOfTabsNET.size())){
            //listOfTabsNET.get(index).getNumberedEditText().setVisibility(View.GONE);
            this.deleteTab(listOfTabsNET.get(index).getTabComponent());
            listOfTabsNET.remove(index);
        }
    }

    public int countTabs (){
        return listOfTabsNET.size();
    }

    public void setFocus (int index) { setFocus(listOfTabsNET.get(index).tabComponent); }

    //Возвращает имя файла по номеру вкладки
    public String getFileName (int index){
        String fileName=null;
        if (index>=0 && index<countTabs()){
            fileName = listOfTabsNET.get(index).getFileName();
        }
        return fileName;
    }

    //Возвращает путь к файлу по номеру вкладки
    public String getFilePath (int index){
        String filePath=null;
        if (index>=0 && index<countTabs()){
            filePath = listOfTabsNET.get(index).getFilePath();
        }
        return filePath;
    }
}
